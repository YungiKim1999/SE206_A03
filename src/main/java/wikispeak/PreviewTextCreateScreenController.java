package wikispeak;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller for CreateScreen
 */
public class PreviewTextCreateScreenController extends Controller{

    private String _currentSearch;

    @FXML private BorderPane rootBorderPane;
    @FXML private TextField searchField;
    @FXML private TextArea textOutput;
    @FXML private ComboBox voiceSelection;
    @FXML private Button createAudioButton;
    @FXML private Text infoText;
    @FXML private TextField audioFileNameField;

    public void initialize(){
        //listens to changes in the audio file name field
        audioFileNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            //createAudioButton is disabled if: file name field is empty, or no voice is selected, or no text is selected in the text area
            createAudioButton.setDisable(newValue.isEmpty() || voiceSelection.getValue() == null || textOutput.getSelectedText().isEmpty());
        });
        //listens to changes in the selected text
        textOutput.selectedTextProperty().addListener(((observable, oldValue, newValue) -> {
            createAudioButton.setDisable(newValue.isEmpty() || voiceSelection.getValue() == null || audioFileNameField.getText().isEmpty());
        }));
        populateVoiceSelectionBox();
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
            //while searching, WHAT SHOULD BE DISABLED?
            createAudioButton.setDisable(true);

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
            //term found on wikipedia, show text in textArea
            command = new Command("cat .temp_text.txt");
            command.execute();
            textOutput.setText(command.getStream());
            //textOutput becomes enabled because search was successful,
            textOutput.setDisable(false);
        }
        infoText.setText("");
    }

    @FXML
    private void handleVoiceSelection(){ createAudioButton.setDisable(audioFileNameField.getText().isEmpty()); }

    @FXML
    private void handlePreview(){
        String textSelection = textOutput.getSelectedText();
        if(correctTextSelection(textSelection)){
            //play the audio
        }
        else{
            //play read out some sample audio
        }
    }

    @FXML
    private void handleCreateAudio(){

        String audioFileName = audioFileNameField.getText();
        String textSelection = textOutput.getSelectedText();

        if(correctTextSelection(textSelection) && nameIsValid(audioFileName) && canOverwrite(audioFileName)){

            infoText.setText("Creating Audio...");
            String selectedVoice = (String)voiceSelection.getValue();

            Thread creationThread = new Thread(new Task<Void>(){
                @Override
                protected Void call() throws Exception {
                    Command command = new Command("echo " + textSelection + " | text2wave -eval \"(voice_" + selectedVoice + ")\" -o audio" + System.getProperty("file.separator") + audioFileName + ".wav");
                    command.execute();
                    return null;
                }

                @Override
                protected void done(){
                    Platform.runLater(() -> infoText.setText("Audio Created!"));
                }

            });
            creationThread.start();
        }
    }

    /**
     * Checks the user has selected the right amount of text to synthesise
     * @return false if no words are selected, or if the selection is larger than 40 words (too many to synthesise)
     */
    private boolean correctTextSelection(String selection){
        if (selection == null || selection.isEmpty()){
            return false;
        }
        //Splits the selection into a countable array of words
        String[] words = selection.split("\\s+");
        //More than 40 words is too many to synthesize, return false
        if(words.length > 40) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Too many words selected");
            alert.showAndWait();
            return false;
        }
        else{
            return true;
        }
    }

    /**
     * Adds Festival voices to the VoiceSelectionComboBox
     */
    private void populateVoiceSelectionBox(){
        String[] voiceNameArray = {"kal_diphone", "akl_nz_jdt_diphone", "akl_nz_cw_cg_cg"};
        for(int i = 0; i < voiceNameArray.length; i++){
            voiceSelection.getItems().add(voiceNameArray[i]);
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

    /**
     * Checks if specified file already exists and, if so, prompts for overwrite
     * @param fileName the name of the creation trying to be made
     */
    private boolean canOverwrite(String fileName){
        File file = new File("audio" + System.getProperty("file.separator") + fileName + ".mp4");
        if(file.exists()){

            //create a show an alert
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Audiofile with name \"" + fileName + "\" already exists. Overwrite?");
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

    /*
    Old method for creating creations on this screen
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
    */

}
