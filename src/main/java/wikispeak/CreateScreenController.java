package wikispeak;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.io.*;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller for CreateScreen
 */
public class CreateScreenController extends Controller{

    private String _currentSearch;

    @FXML private BorderPane rootBorderPane;
    @FXML private TextField searchField;
    @FXML private TextArea textOutput;
    @FXML private ComboBox lineSelection;
    @FXML private Button createButton;
    @FXML private Text infoText;
    @FXML private TextField creationNameField;

    public void initialize(){
        creationNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(newValue.isEmpty() || lineSelection.getValue() == null);
        });
    }

    @FXML
    private void handleMainMenu() throws IOException {
        switchScenes(rootBorderPane, "MainMenu.fxml");
    }

    @FXML
    private void handleSearch(){

        _currentSearch = searchField.getText();

        if(!_currentSearch.isEmpty()) {
            infoText.setText("Searching...");
            lineSelection.setDisable(true);
            createButton.setDisable(true);
            //TextOutput is not editable while searching
            textOutput.setEditable(false);

            Thread searchThread = new Thread(new Task<Void>(){
                @Override
                protected Void call() throws Exception {
                    Command wikitCommand = new Command("wikit " + _currentSearch + " | sed 's/\\([.!?]\\) \\([[:upper:]]\\)/\\1\\n\\2/g' > .temp_text.txt");
                    wikitCommand.execute();
                    return null;
                }
                @Override
                protected void done(){
                    Platform.runLater(() -> postSearchUpdateGUI());
                }
            });

            searchThread.start();
        }
    }

    /**
     * Updates the GUI appropriately based on the outcome of a Wikit Search
     */
    private void postSearchUpdateGUI() {
        Command command = new Command("cat .temp_text.txt | grep -Fwq \":^(\"");
        if(command.execute() == 0){
            //nothing found on Wikipedia, update GUI to inform user
            command = new Command("cat .temp_text.txt");
            command.execute();
            textOutput.setText(command.getStream());
        }
        else{
            //term found on wikipedia, update GUI to allow line selection
            command = new Command("cat .temp_text.txt");
            command.execute();
            textOutput.setText(command.getStream());
            //textOutput becomes editable when search was successful,
            textOutput.setEditable(true);

            int numberOfLines = command.getStream().split(System.getProperty("line.separator")).length;
            populateComboBox(numberOfLines);
            lineSelection.setDisable(false);
        }
        infoText.setText("");
    }

    @FXML
    private void handleLineSelection(){ createButton.setDisable(creationNameField.getText().isEmpty()); }

    @FXML
    private void handleCreate(){
        String creationName = creationNameField.getText();
        if(nameIsValid(creationName) && canOverwrite(creationName)){
            infoText.setText("Creating Creation...");
            int selectedValue = (Integer)lineSelection.getValue();

            Thread creationThread = new Thread(new Task<Void>(){
                @Override
                protected Void call() throws Exception {
                    Command command = new Command("head -n" + selectedValue + " .temp_text.txt | text2wave -o .temp_audio.wav");
                    command.execute();
                    command = new Command("soxi -D .temp_audio.wav");
                    command.execute();
                    Double audioLength = Double.parseDouble(command.getStream());
                    command = new Command("ffmpeg -y -f lavfi -i color=c=pink:s=320x240:d=" + audioLength + " -vf \"drawtext=fontfile=fonts/myfont.ttf:fontsize=30: fontcolor=black:x=(w-text_w)/2:y=(h-text_h)/2:text=" + _currentSearch + "\" .temp_video.mp4");
                    command.execute();
                    command = new Command("ffmpeg -y -i .temp_audio.wav -i .temp_video.mp4 -c:v copy -c:a aac -strict experimental creations" + System.getProperty("file.separator") + creationName + ".mp4");
                    command.execute();
                    return null;
                }

                @Override
                protected void done(){
                    Platform.runLater(() -> infoText.setText("Creation Created!"));
                }

            });

            creationThread.start();
        }
    }

    /**
     * Adds a range of integers as selection options for the ComboBox
     * @param number
     */
    private void populateComboBox(int number){
        lineSelection.getItems().clear();
        for(int i = 1; i <= number; i++){
            lineSelection.getItems().add(i);
        }
    }

    /**
     * Checks if the given filename doesn't contain forbidden characters
     * @param fileName
     * @return true or false
     */
    private boolean nameIsValid(String fileName){
        Pattern p = Pattern.compile("[<>:\"/\\\\|?*]");
        Matcher m = p.matcher(fileName);
        if (m.find()){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid creation name");
            alert.showAndWait();
            return false;
        }
        return true;
    }

    /**
     * Checks if specified file already exists and, if so, prompts for overwrite
     * @param fileName the name of the creation trying to be made
     */
    private boolean canOverwrite(String fileName){
        File file = new File("creations" + System.getProperty("file.separator") + fileName + ".mp4");
        if(file.exists()){

            //create a show an alert
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Creation with name \"" + fileName + "\" already exists. Overwrite?");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                //allowed to overwrite, can create
                return true;
            }
            else{
                //can't create
                return false;
            }
        }
        else{
            //file doesn't already exist, can create
            return true;
        }
    }

}
