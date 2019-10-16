package wikispeak;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import wikispeak.components.DeleteAndMoveCell;
import wikispeak.helpers.Command;
import wikispeak.tasks.createFullAudioJob;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import wikispeak.tasks.previewJob;

/**
 * Controller for CreateScreen
 */
public class CreateAudioScreenController extends ListController{

    @FXML private BorderPane rootBorderPane;
    @FXML private TextArea textOutput;
    @FXML private ComboBox voiceSelection;
    @FXML private Button createAudioSnippetButton;
    @FXML private TextField audioFileNameField;
    @FXML private Button previewButton;
    @FXML private VBox audioListBox;
    @FXML private ProgressIndicator progressIndicator;

    private ExecutorService worker = Executors.newSingleThreadExecutor();

    private final ObservableList<String> audioFiles = FXCollections.observableArrayList();

    public void initialize() throws IOException {
        //listens to changes in the audio file name field
        audioFileNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateCreateAudioSnippetButtonAccess();
        });
        //listens to changes in the selected text
        textOutput.selectedTextProperty().addListener(((observable, oldValue, newValue) -> {
            updateCreateAudioSnippetButtonAccess();
        }));
        populateVoiceSelectionBox();
        populateTextArea();
        populateAudioList();
    }

    @FXML
    private void handleVoiceSelection(){
        updateCreateAudioSnippetButtonAccess();
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
    private void handleCreateAudioSnippet(){

        final String audioFileName = audioFileNameField.getText();
        final String textSelection = textOutput.getSelectedText();

        if(correctTextSelection(textSelection) && nameIsValid(audioFileName) && canOverwrite(audioFileName)){

            progressIndicator.setVisible(true);
            final String selectedVoice = (String)voiceSelection.getValue();

            Thread audioThread = new Thread(new Task<Void>(){
                @Override
                protected Void call() throws Exception {
                    Command command = new Command("echo \"" + textSelection + "\" | text2wave -eval \"(voice_" + selectedVoice + ")\" -o .temp" + System.getProperty("file.separator") + "audio" + System.getProperty("file.separator") + audioFileName + ".wav");
                    command.execute();
                    return null;
                }

                @Override
                protected void done(){
                    Platform.runLater(() -> postCreateUpdateGUI(audioFileName));
                }

            });
            audioThread.start();
        }
    }

    /**
     * Adds the file to the list (if it doesn't already exist)
     */
    private void postCreateUpdateGUI(String audioFileName){
        if(!audioFiles.contains(audioFileName)){
            audioFiles.add(audioFileName);
        }
        progressIndicator.setVisible(false);
    }

    @FXML
    /**
     * Takes the user back to the search screen
     */
    private void handleBackToSearch() throws IOException {
        switchScenes(rootBorderPane, "EditText.fxml");
    }

    @FXML
    /**
     * Takes the user to the next screen where they combine audio files
     * Saves the text area so any edits persist if the user wants to go back
     */
    private void handleCreateFullAudio() throws IOException {
        //start the createFullAudioJob
        final List<String> chosenAudioFiles = new ArrayList<String>(audioFiles);
        createFullAudioJob job = new createFullAudioJob(chosenAudioFiles);
        worker.submit(job);
        switchScenes(rootBorderPane, "ImageSelectionScreen.fxml");
    }

    /**
     * Adds Festival voices to the VoiceSelectionComboBox
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
        Command command = new Command("cat .temp" + System.getProperty("file.separator") + "temp_text.txt");
        command.execute();
        textOutput.setText(command.getStream());
    }

    /**
     * Adds any created audio files to the audiofile list
     * Mainly useful when user comes back to this screen after already creating audio
     */
    private void populateAudioList(){
        audioFiles.addAll(populateList(".temp" + System.getProperty("file.separator") + "audio", ".wav"));

        ListView<String> audioListView = new ListView<>(audioFiles);
        Text text = new Text("No Audio Snippets\nhave been Created");
        text.setTextAlignment(TextAlignment.CENTER);
        audioListView.setPlaceholder(text);
        audioListView.setCellFactory(param -> new DeleteAndMoveCell());

        audioListBox.getChildren().add(audioListView);
    }

    /**
     * Checks if all requirements are met to enable the create button
     * A voice must be selected, some text must be highlighted and a name must be provided for the audio file
     * @return
     */
    private void updateCreateAudioSnippetButtonAccess(){
        createAudioSnippetButton.setDisable(voiceSelection.getValue() == null || textOutput.getSelectedText().trim().isEmpty() || audioFileNameField.getText().trim().isEmpty());
    }

    /**
     * Checks the user has selected the right amount of text to synthesise
     * @return false if no words are selected, or if the selection is larger than 40 words (too many to synthesise)
     */
    private boolean correctTextSelection(String selection){
        if (selection == null || selection.trim().isEmpty()){
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
        File file = new File(".temp" + System.getProperty("file.separator") + "audio" + System.getProperty("file.separator") + fileName + ".wav");
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
