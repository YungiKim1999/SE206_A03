package wikispeak;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import wikispeak.helpers.Command;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Controller for CreateScreen
 */
public class CreateAudioScreenController extends Controller{

    @FXML private BorderPane rootBorderPane;
    @FXML private TextArea textOutput;
    @FXML private ComboBox voiceSelection;
    @FXML private Button createAudioButton;
    @FXML private Text infoText;
    @FXML private TextField audioFileNameField;
    @FXML private Button previewButton;

    public void initialize() throws IOException {
        //listens to changes in the audio file name field
        audioFileNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateCreateButtonAccess();
        });
        //listens to changes in the selected text
        textOutput.selectedTextProperty().addListener(((observable, oldValue, newValue) -> {
            updateCreateButtonAccess();
        }));
        populateVoiceSelectionBox();
        populateTextArea();
    }

    @FXML
    private void handleVoiceSelection(){
        updateCreateButtonAccess();
        previewButton.setDisable(false);
    }

    @FXML
    private void handlePreview() throws InterruptedException, IOException {

        String selectedVoice = (String)voiceSelection.getValue();
        String textSelection = textOutput.getSelectedText();
        if(!correctTextSelection(textSelection)){
            textSelection = "This is my voice";
        }
        String finalTextSelection = textSelection;

        //read out the selected text in the selected voice
        Thread previewVoiceThread = new Thread(new Task<Void>(){
            @Override
            protected Void call() throws Exception {
                Process p = Runtime.getRuntime().exec("festival");
                Writer w = new OutputStreamWriter(p.getOutputStream());
                w.append("(voice_" + selectedVoice + ")");
                w.append("(SayText \"" + finalTextSelection + "\")");
                w.close();
                return null;
            }
        });

        previewVoiceThread.start();
    }

    @FXML
    private void handleCreateAudio(){

        String audioFileName = audioFileNameField.getText();
        String textSelection = textOutput.getSelectedText();

        if(correctTextSelection(textSelection) && nameIsValid(audioFileName) && canOverwrite(audioFileName)){

            infoText.setText("Creating Audio...");
            String selectedVoice = (String)voiceSelection.getValue();

            Thread audioThread = new Thread(new Task<Void>(){
                @Override
                protected Void call() throws Exception {
                    Command command = new Command("echo \"" + textSelection + "\" | text2wave -eval \"(voice_" + selectedVoice + ")\" -o audio" + System.getProperty("file.separator") + audioFileName + ".wav");
                    command.execute();
                    System.out.println(command.getStream());
                    return null;
                }

                @Override
                protected void done(){
                    Platform.runLater(() -> setInfoText());
                }

            });
            audioThread.start();
        }
    }

    /**
     * Sets the little info text to tell the user how many audio files they created
     */
    private void setInfoText(){
        int numberOfFiles = new File("audio").listFiles().length;
        if(numberOfFiles == 1){
            infoText.setText(numberOfFiles + " Audio File Created");
        }
        else{
            infoText.setText(numberOfFiles + " Audio Files Created");
        }
    }

    @FXML
    /**
     * Takes the user back to the search screen. Confirms they are happy to delete any audio files they have made
     */
    private void handleBackToSearch() throws IOException {
        if(new File("audio").listFiles().length > 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Any audio files you have created will be deleted.\nAre you sure you want to go back?");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                //only switch scene after confirmation
                switchScenes(rootBorderPane, "SearchScreen.fxml");
            }
        }
        else{
            switchScenes(rootBorderPane, "SearchScreen.fxml");
        }
    }

    @FXML
    /**
     * Takes the user to the next screen where they combine audio files
     * Saves the text area so any edits persist if the user wants to go back
     */
    private void handleNext() throws IOException {
        //save the current text area status to the text file
        String text = textOutput.getText();
        Command command = new Command("echo \"" + text + "\" > .temp_text.txt");
        command.execute();
       // switchScenes(rootBorderPane, "CombineAudioScreen.fxml");
        switchScenes(rootBorderPane, "SelectAudioScreen.fxml");
    }

    /**
     * Adds Festival voices to the VoiceSelectionComboBox
     * TODO: make this dynamically search for the voices available
     */
    private void populateVoiceSelectionBox() throws IOException {

        Command command = new Command("ls " +  System.getProperty("file.separator") + "usr" + System.getProperty("file.separator") + "share" + System.getProperty("file.separator") + "festival" + System.getProperty("file.separator") + "voices" + System.getProperty("file.separator") + "english");
        command.execute();

        String[] voiceNameArray = command.getStream().split("\\s+");
        for(int i = 0; i < voiceNameArray.length; i++){
            voiceSelection.getItems().add(voiceNameArray[i]);
        }
    }

    /**
     * Adds the search result text to the text area
     */
    private void populateTextArea(){
        Command command = new Command("cat .temp_text.txt");
        command.execute();
        textOutput.setText(command.getStream());
    }

    /**
     * Checks if all requirements are met to enable the create button
     * A voice must be selected, some text must be highlighted and a name must be provided for the audio file
     * @return
     */
    private void updateCreateButtonAccess(){
        createAudioButton.setDisable(voiceSelection.getValue() == null || textOutput.getSelectedText().isEmpty() || audioFileNameField.getText().isEmpty());
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
     * Checks if the given filename doesn't contain forbidden characters
     * @param fileName
     * @return true or false
     */
    private boolean nameIsValid(String fileName){
        Pattern p = Pattern.compile("[\\s<>:\"/\\\\|?*]");
        Matcher m = p.matcher(fileName);
        if (m.find()){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid audio file name");
            alert.showAndWait();
            return false;
        }
        return true;
    }

    /**
     * Checks if specified audio file already exists and, if so, prompts for overwrite
     * @param fileName the name of the creation trying to be made
     */
    private boolean canOverwrite(String fileName){
        File file = new File("audio" + System.getProperty("file.separator") + fileName + ".wav");
        if(file.exists()){

            //show an alert
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Audio file with name \"" + fileName + "\" already exists. Overwrite?");
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
