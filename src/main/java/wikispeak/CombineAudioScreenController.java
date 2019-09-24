package wikispeak;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CombineAudioScreenController extends ListController {

    @FXML private BorderPane rootBorderPane;
    @FXML private VBox audioListBox;
    @FXML private ComboBox numberSelection;
    @FXML private Text infoText;
    @FXML private Text audioFileInfoText;
    @FXML private Button creationButton;
    @FXML private TextField creationNameField;

    //Hashset to ensure no duplicates
    private LinkedHashSet<SelectableFile> _selectedFiles = new LinkedHashSet<>();

    public void initialize() {

        //Turning a list of String audio files into SelectableFile objects that have a "selected" property
        List<String> audioFileList = populateList("audio", ".wav");
        ListView<SelectableFile> listView = new ListView<>();

        for (String fileName : audioFileList) {
            SelectableFile selectableFile = new SelectableFile(fileName, false);
            listView.getItems().add(selectableFile);

            //Observer Selected property and add/remove the file from _selectedFiles appropriately
            selectableFile.selectedProperty().addListener((obs, previouslySelected, currentlySelected) -> {
                if(currentlySelected){
                    _selectedFiles.add(selectableFile);
                }
                else if(_selectedFiles.contains(selectableFile)){
                    _selectedFiles.remove(selectableFile);
                }
                updateCreateButtonAccess();
            });
        }

        listView.setCellFactory(CheckBoxListCell.forListView(new Callback<SelectableFile, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(SelectableFile selectableFile) {
                return selectableFile.selectedProperty();
            }
        }));

        if(audioFileList.size() == 0){
            audioFileInfoText.setText("No audio files found");
        }
        else{
            audioListBox.getChildren().add(listView);
        }
        populateImageNumberSelectionBox();

        //listens to changes in the creation file name field
        creationNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateCreateButtonAccess();
        });
    }

    @FXML
    private void handleBackToCreateAudio() throws IOException {
        switchScenes(rootBorderPane, "CreateAudioScreen.fxml");
    }

    @FXML
    private void handleNumberSelection(){
        updateCreateButtonAccess();
    }

    @FXML
    private void handleCreateCreation(){

        String creationName = creationNameField.getText();

        if(nameIsValid(creationName)){

            //Set GUI infoText
            infoText.setText("Creating...");

            //get current search term and number of images
            Command command = new Command("cat .temp_searchterm.txt");
            command.execute();
            String searchTerm = command.getStream();
            int number = (Integer)numberSelection.getValue();

            //Threadsafe list for the creationThread to access
            //TODO: check this is actually threadsafe
            final LinkedHashSet<SelectableFile> selectedFiles = new LinkedHashSet<>(_selectedFiles);

            Thread creationThread = new Thread(new Task<Void>(){
                @Override
                protected Void call() throws Exception {
                    //TODO: this needs to complete before anything else can start
                    //delete any existing images
                    for (File file : new File("downloads").listFiles()){
                        file.delete();
                    }
                    //download the images
                    FlickreImageCreator imageCreator = new FlickreImageCreator(searchTerm, number);
                    imageCreator.start();

                    //merge the selected audiofiles
                    String audioFileList = "";
                    for(SelectableFile file : selectedFiles){
                        audioFileList = audioFileList + " audio" + System.getProperty("file.separator") + file.toString() + ".wav";
                    }
                    File file = new File(".combined.wav"); //delete the file if it already exists
                    file.delete();
                    Command command = new Command("sox " + audioFileList + " .temp_audio.wav");
                    command.execute();

                    //calculate duration for each image in slideshow, given audio duration
                    command = new Command("soxi -D .temp_audio.wav");
                    command.execute();
                    double duration = Double.parseDouble(command.getStream());
                    Double framerate = (number/duration);

                    //make the video
                    //TODO: check the output resolution is the same as the resolution of the video player. Remove delay
                    TimeUnit.SECONDS.sleep(5); //stupid artificial delay to make sure all the pictures are downloaded
                    command = new Command("ffmpeg -framerate " + framerate + " -pattern_type glob -i 'downloads/*.jpg' -vf \"drawtext=fontfile=fonts/myfont.ttf:fontsize=100: fontcolor=black:x=(w-text_w)/2:y=(h-text_h)/2:text=" + searchTerm + ", scale=320:240\" .temp_video.mp4");
                    command.execute();

                    //make the creation, combine audio and video
                    return null;
                }

                @Override
                //TODO: make the GUI do something more useful when the creation is made
                protected void done(){
                    Platform.runLater(() -> infoText.setText("Done!"));
                }

            });
            creationThread.start();


        }
    }

    private static class SelectableFile {
        private final StringProperty _name = new SimpleStringProperty();
        private final BooleanProperty _selected = new SimpleBooleanProperty();

        public SelectableFile(String name, boolean selected) {
            _name.set(name);
            _selected.set(selected);
        }

        public final BooleanProperty selectedProperty() {
            return _selected;
        }

        public final boolean isSelected() {
            return selectedProperty().get();
        }

        @Override
        public String toString() {
            return _name.get();
        }

    }

    /**
     * Button is disabled if there are no selected audio files OR no number has been selected OR no creation name has been entered
     */
    private void updateCreateButtonAccess(){
        creationButton.setDisable(_selectedFiles.isEmpty() || numberSelection.getValue() == null || creationNameField.getText().isEmpty());
    }

    /**
     * Adds numbers 1 to 10 to the combobox
     */
    private void populateImageNumberSelectionBox(){
        for(int i = 1; i <= 10; i++){
            numberSelection.getItems().add(i);
        }
    }

    /**
     * Checks if the given filename doesn't contain forbidden characters
     * @param fileName
     * @return true or false
     */
    private boolean nameIsValid(String fileName){
        Pattern p = Pattern.compile("[\\s<>:\"/\\\\|?*]");
        Matcher m = p.matcher(fileName);
        if (m.find()){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid creation name");
            alert.showAndWait();
            return false;
        }
        return true;
    }
}
