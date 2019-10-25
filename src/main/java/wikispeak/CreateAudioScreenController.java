package wikispeak;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
import java.util.HashMap;
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

    private static final int MAX_WORDS = 40;
    private static final int MIN_WORDS = 1;

    @FXML private BorderPane rootBorderPane;
    @FXML private TextArea textOutput;
    @FXML private ComboBox voiceSelection;
    @FXML private Button createAudioSnippetButton;
    @FXML private TextField audioFileNameField;
    @FXML private Button previewButton;
    @FXML private VBox audioListBox;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Button createFullAudioButton;
    @FXML private Text promptText;
    @FXML private Text selectionInfoText;

    private ExecutorService worker = Executors.newSingleThreadExecutor();

    private HashMap<String, String> voices = new HashMap<>();

    private final ObservableList<String> audioFiles = FXCollections.observableArrayList();

    public void initialize() throws IOException {
        //listens to changes in the audio file name field
        audioFileNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateCreateAndPreviewButtonAccess();
        });
        //listens to changes in the selected text
        textOutput.selectedTextProperty().addListener(((observable, oldValue, newValue) -> {
            updateSelectionGUI();
        }));
        //listens to the amount of audio files created
        audioFiles.addListener((ListChangeListener<String>) change -> {
            if(audioFiles.size() == 0){
                createFullAudioButton.setDisable(true);
                promptText.setText("Create Some Audio Snippets");
            }
            else{
                createFullAudioButton.setDisable(false);
                promptText.setText("Click and Drag to Reorder");
            }
        });
        populateVoiceSelectionBox();
        populateTextArea();
        populateAudioList();
    }

    @FXML
    private void handlePreview() throws InterruptedException, IOException {
        String selectedKey = (String)voiceSelection.getValue();
        final String selectedVoice = voices.get(selectedKey);
        String textSelection = textOutput.getSelectedText();
        final String finalTextSelection = textSelection;

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

        if(nameIsValid(audioFileName) && canOverwrite(audioFileName)){

            final String textSelection = textOutput.getSelectedText().trim();
            progressIndicator.setVisible(true);
            String selectedKey = (String)voiceSelection.getValue();
            final String selectedVoice = voices.get(selectedKey);

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

    /**
     * Takes the user back to the search screen
     */
    @FXML
    private void handleBackToSearch() throws IOException {
        switchScenes(rootBorderPane, "EditText.fxml");
    }


    /**
     * Takes the user back to the main menu. Confirms they are happy to abandon any progress
     */
    @FXML
    private void handleMainMenu() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to go to Main Menu?\nAny progress will be lost.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            //only switch scene after confirmation
            switchScenes(rootBorderPane, "MainMenu.fxml");
        }
    }

    /**
     * Takes the user to the next screen where they combine audio files
     * Saves the text area so any edits persist if the user wants to go back
     */
    @FXML
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

        voices.put("Default Male", "kal_diphone");
        voices.put("NZ Female", "akl_nz_cw_cg_cg");
        voices.put("NZ Male", "akl_nz_jdt_diphone");

        for(String voice : voices.keySet()){
            voiceSelection.getItems().add(voice);
        }

        voiceSelection.setValue("Default Male");
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
        audioListView.setCellFactory(param -> new DeleteAndMoveCell(".temp" + System.getProperty("file.separator") + "audio", ".wav"));

        audioListBox.getChildren().add(audioListView);
    }

    /**
     * Changes GUI dynamically as the user changes the amount of text highlighted
     * Provides an auto-generated name suggestion
     * Updates to prompt text to indicate how many words are selected
     */
    private void updateSelectionGUI(){
        int wordsSelected = getNumberOfWordsSelected();

        if((wordsSelected >= MIN_WORDS) && (wordsSelected <= MAX_WORDS)){
            for(int i = 1; i <= audioFiles.size() + 1; i++) {
                File file = new File(".temp" + System.getProperty("file.separator") + "audio" + System.getProperty("file.separator") + "audio_" + i + ".wav");
                if (!file.exists()) {
                    audioFileNameField.setText("audio_" + i);
                    break;
                }
            }
            selectionInfoText.setText("" + wordsSelected + " words selected.");
            selectionInfoText.setFill(Color.BLUE);
        }
        else if(wordsSelected < MIN_WORDS){
            selectionInfoText.setText("No words selected.");
            selectionInfoText.setFill(Color.RED);
        }
        else{
            selectionInfoText.setText("Too many words selected.");
            selectionInfoText.setFill(Color.RED);
        }
        updateCreateAndPreviewButtonAccess();
    }

    /**
     * Updates access to the create button and preview button
     */
    private void updateCreateAndPreviewButtonAccess(){
        int wordsSelected = getNumberOfWordsSelected();
        //For the creation button: a voice must be selected, the correct number of words must be selected, there must be a name in the audio name field
        createAudioSnippetButton.setDisable(voiceSelection.getValue() == null || wordsSelected < MIN_WORDS || wordsSelected > MAX_WORDS || audioFileNameField.getText().trim().isEmpty());
        //For the preview button: a voice must be selected and the correct number of words must be selected
        previewButton.setDisable(voiceSelection.getValue() == null || wordsSelected < MIN_WORDS || wordsSelected > MAX_WORDS);
    }

    /**
     * Finds the number of words selected by the user
     */
    private int getNumberOfWordsSelected(){
        String selection = textOutput.getSelectedText();
        if (selection == null || selection.trim().isEmpty()){
            return 0;
        }
        //Splits the selection into a countable array of words
        String[] words = selection.trim().split("\\s+");
        return words.length;
    }

    /**
     * Checks if the given filename doesn't contain forbidden characters
     * @param fileName the name of the file entered
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
