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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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

    public void initialize(){
        //listens to changes in the audio file name field
        audioFileNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            //createAudioButton is disabled if: file name field is empty, or no voice is selected, or no text is selected in the text area
            createAudioButton.setDisable(newValue.isEmpty() || voiceSelection.getValue() == null || textOutput.getSelectedText().isEmpty());
        });
        //listens to changes in the selected text
        textOutput.selectedTextProperty().addListener(((observable, oldValue, newValue) -> {
            //createAudioButton is disabled if: file name field is empty, or no voice is selected, or no text is selected in the text area
            createAudioButton.setDisable(newValue.isEmpty() || voiceSelection.getValue() == null || audioFileNameField.getText().isEmpty());
        }));
        populateVoiceSelectionBox();
        populateTextArea();
    }

    @FXML
    private void handleVoiceSelection(){
        createAudioButton.setDisable(audioFileNameField.getText().isEmpty());
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

    @FXML
    private void handleBackToSearch() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Any audio files you have created will be deleted.\nAre you sure you want to go back?");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            //only switch scene after confirmation
            switchScenes(rootBorderPane, "SearchScreen.fxml");
        }
    }

    @FXML
    private void handleNext(){
        //save the current text area status to the text file
        String text = textOutput.getText();
        System.out.println(text);
        Command command = new Command("echo \"" + text + "\" > .temp_text.txt");
        command.execute();
        System.out.println(command.getStream());
        System.out.println("Next Pressed");
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
     * Adds the search result text to the text area
     */
    private void populateTextArea(){
        Command command = new Command("cat .temp_text.txt");
        command.execute();
        textOutput.setText(command.getStream());
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid creation name");
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
    /**
     * Deletes specified creation after asking for confirmation
     * @param creation
     * @throws IOException
    private void deleteCreation(String creation) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete \"" + creation + "\"?");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            File file = new File("creations" + System.getProperty("file.separator") + creation + ".mp4");
            file.delete();

            //refresh the scene
            switchScenes(rootBorderPane, "DeleteScreen.fxml");
        }
    }

    */

}
