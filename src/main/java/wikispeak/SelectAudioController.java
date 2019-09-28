package wikispeak;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;
import wikispeak.helpers.Command;
import wikispeak.tasks.mergeAudioFileJob;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectAudioController extends ListController {

    @FXML private BorderPane rootBorderPane;
    @FXML private VBox audioListBox;
    @FXML private Text audioFileInfoText;
    @FXML private Button creationButton;
    @FXML private TextField creationNameField;

    private ExecutorService team = Executors.newCachedThreadPool();

    //Hashset to ensure no duplicates
    private LinkedHashSet<SelectableFile> _selectedFiles = new LinkedHashSet<>();

    public void initialize() {

        //Turning a list of String audio files into SelectableFile objects that have a "selected" property
        List<String> audioFileList = populateList("audio", ".wav");
        ListView<SelectableFile> listView = new ListView<>();

        for (String fileName : audioFileList) {
            SelectableFile selectableFile = new SelectableFile(fileName, false);
            listView.getItems().add(selectableFile);

            //Observe Selected property and add/remove the file from _selectedFiles appropriately
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
    private void handleMainMenu() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to go to main menu?");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            //only switch scene after confirmation
            switchScenes(rootBorderPane, "MainMenu.fxml");
        }
    }

    @FXML
    private void handleNext(){

        String creationName = creationNameField.getText();

        if(nameIsValid(creationName)){

            Command command = new Command("echo \"" + creationName + "\" > .temp_creationName.txt");
            command.execute();

            //make a String of audiofile names
            String audioFileList = "";
            for(SelectAudioController.SelectableFile file : _selectedFiles){
                audioFileList = audioFileList + " audio" + System.getProperty("file.separator") + file.toString() + ".wav";
            }

            //start the task
            mergeAudioFileJob getFullAudio = new mergeAudioFileJob(audioFileList);
            //creationJob createCreation = new creationJob(searchTerm, number, audioFileList, creationName);
            //progressBar.progressProperty().bind(createCreation.progressProperty());
            getFullAudio.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    try {
                        switchScenes(rootBorderPane, "ImageSelectionScreen.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            team.submit(getFullAudio);

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
        creationButton.setDisable(_selectedFiles.isEmpty()|| creationNameField.getText().isEmpty());
    }

    /**
     * Adds numbers 1 to 10 to the combobox
     */

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
