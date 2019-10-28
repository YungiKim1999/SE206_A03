package wikispeak;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;
import wikispeak.helpers.Command;
import wikispeak.tasks.addMusicJob;
import wikispeak.tasks.removeMusicJob;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FinalPreviewScreenController extends ListController {
    File fileURL;

    ObservableList<String> creationsStrings = FXCollections.observableArrayList(populateList("creations", ""));
    private boolean play = false;
    private boolean firsTime;
    private boolean addedMusic = false;
    private  MediaPlayer creationPlayingThing = null;
    private FileChooser musicSelector = new FileChooser();
    private Window musicSelectorWindow;
    private ExecutorService team = Executors.newSingleThreadExecutor();

    @FXML private MediaView creationViewer;
    @FXML private BorderPane rootBorderPane;
    @FXML private Slider volumeSlider;
    @FXML private Label timeLabel;
    @FXML private Label finishTime;
    @FXML private Button playPauseButton;
    @FXML private Slider videoBuffer;
    @FXML private TextField creationNameInput;
    @FXML private ListView previousCreations;
    @FXML private Button createButton;
    @FXML private Button addMusicButton;
    @FXML private Button removeMusicButton;


    @FXML
    public void initialize(){

        musicSelector.setInitialDirectory(new File("music"));
        musicSelector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Mp3 Files", "*.mp3"));
        setMusicButtons();
        //create button is only available if some text is entered in the field
        creationNameInput.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(creationNameInput.getText().trim().isEmpty());
        });
        Command command = new Command("cat .temp" + System.getProperty("file.separator") + "temp_searchterm.txt");
        command.execute();
        creationNameInput.setText(command.getStream() + "Creation");

        previousCreations.setItems(creationsStrings);
        firsTime = true;
        timeLabel.setText("00:00");
        finishTime.setText("--:--");
        setMediaForPlay();
        addVideoListener();
        addVolumeListener();
        playMedia();

        volumeSlider.setValue(100);

    }

    /**
     * adds the volume slides listener to the preview window
     */
    private void addVolumeListener(){
        volumeSlider.valueProperty().addListener(observable -> {
            creationPlayingThing.setVolume(volumeSlider.getValue() / 100);
        });
    }

    /**
     * sets the time labels onto the preview area according to where the video currently is
     * @param newVakue
     */
    private void setTimeLabels(Duration newVakue){
        String currentTime = "";
        currentTime += String.format("%02d", (int)newVakue.toMinutes());
        currentTime += ":";
        currentTime += String.format("%02d", (int)newVakue.toSeconds()%60);
        timeLabel.setText(currentTime);
        String stopTime = "";
        stopTime += String.format("%02d", (int)creationPlayingThing.getStopTime().toMinutes());
        stopTime += ":";
        stopTime += String.format("%02d", (int)creationPlayingThing.getStopTime().toSeconds()%60);
        finishTime.setText(stopTime);
    }

    /**
     * adds the listeners for the video components which the user will interact with
     */
    private void addVideoListener() {
        creationPlayingThing.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                videoBuffer.setMin(0);
                int endTime  =  Integer.parseInt(String.format("%02d", (int)creationPlayingThing.getStopTime().toMillis()));
                int currentTime =  Integer.parseInt(String.format("%02d",(int)newValue.toMillis()));
                videoBuffer.setMax(endTime);
                setTimeLabels(newValue);
                videoBuffer.setValue(currentTime);
                videoBuffer.valueProperty().addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        if(videoBuffer.isPressed()){
                            Duration newTime = new Duration(videoBuffer.getValue());
                            creationPlayingThing.seek(newTime);
                        }
                    }
                });
                if(timeLabel.getText().equals(finishTime.getText())){
                    creationPlayingThing.stop();
                    playPauseButton.setText("Repeat");
                }

            }
        });
    }

    /**
     * allows the user to go back to image selection
     * @throws IOException
     */
    @FXML
    private void handleBackToImageSelection() throws IOException {
        if(play == true){
            pauseMedia();
        }
        File deleteFile = new File(".temp" + System.getProperty("file.separator") + "final_creation.mp4");
        deleteFile.delete();
        deleteFile = new File(".temp" + System.getProperty("file.separator") + "quiz1.mp4");
        deleteFile.delete();
        deleteFile = new File(".temp" + System.getProperty("file.separator") + "quiz2.mp4");
        deleteFile.delete();
        deleteFile = new File(".temp" + System.getProperty("file.separator") + "blankVideo.mp4");
        deleteFile.delete();
        deleteFile = new File(".temp" + System.getProperty("file.separator") + "noTextVideo.mp4");
        deleteFile.delete();
        deleteFile = new File(".temp" + System.getProperty("file.separator") + "temp_video.mp4");
        deleteFile.delete();
        if(creationPlayingThing.getStatus() == MediaPlayer.Status.PLAYING){
            creationPlayingThing.pause();
        }
        switchScenes(rootBorderPane, "ImageSelectionScreen.fxml");
    }

    /**
     * exits the creation process
     * @throws IOException
     */
    @FXML
    private void handleExitButton()throws IOException {
        if(play == true){
            pauseMedia();
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to leave? All progress will be lost");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if(creationPlayingThing.getStatus() == MediaPlayer.Status.PLAYING){
                creationPlayingThing.pause();
            }
            deleteAllFiles();
            switchScenes(rootBorderPane, "MainMenu.fxml");

        }

    }

    /**
     * plays the creation under final previewing
     */
    private void playMedia(){
        creationPlayingThing.play();
        playPauseButton.setText("Pause");
    }

    /**
     * pauses the creation under final previewing
     */
    private void pauseMedia(){
        creationPlayingThing.pause();
        playPauseButton.setText("Play");
    }

    /**
     * sets the creation into the media player so the user can preview the creation before accepting it
     */
    private void setMediaForPlay(){
        if (!firsTime && creationPlayingThing.getStatus() == MediaPlayer.Status.PLAYING){
            creationPlayingThing.dispose();
        }
        fileURL =new File( ".temp" + System.getProperty("file.separator") + "final_creation.mp4");
        Media playCreation = new Media(fileURL.toURI().toString());
        creationPlayingThing = new MediaPlayer(playCreation);
        creationViewer.setMediaPlayer(creationPlayingThing);
    }

    /**
     * allows the user to play, pause, and repeat the creation under final previewing
     */
    @FXML
    private void handlePlayPauseButton(){
            if(playPauseButton.getText().equals("Repeat")){
                creationPlayingThing.stop();
                setMediaForPlay();
                playMedia();
                addVideoListener();
                addVolumeListener();
                play = true;
            } else if (creationPlayingThing.getStatus() == MediaPlayer.Status.PLAYING) {
                pauseMedia();
                play = false;
            } else if (creationPlayingThing.getStatus() == MediaPlayer.Status.PAUSED) {
                playMedia();
                play = true;

            }
    }

    /**
     * buffer the creation under final preview sligjtly forward
     */
    @FXML
    private void handleForwardButton(){
        if(play){
            creationPlayingThing.seek(creationPlayingThing.getCurrentTime().add(Duration.seconds(2)));
        }
    }

    /**
     * buffer the creation under final preview sligjtly forward
     */
    @FXML
    private void handleBackButton(){
        if(play){
            creationPlayingThing.seek(creationPlayingThing.getCurrentTime().subtract(Duration.seconds(2)));
        }
    }

    /**
     * allows the user to add music onto the creation
     */
    @FXML
    private void handleAddMusic(){
        File selectedMusic = musicSelector.showOpenDialog(musicSelectorWindow);
        if(selectedMusic != null) {
            addMusicJob addMusic = new addMusicJob(selectedMusic);
            team.submit(addMusic);
            addMusic.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    File file = new File(".temp" + System.getProperty("file.separator") + "The_final_creation.mp4");
                    file.delete();
                    addedMusic = true;
                    if(creationPlayingThing.getStatus() == MediaPlayer.Status.PLAYING){
                        pauseMedia();
                    }
                    initialize();
                }
            });
        }

    }

    /**
     * allows the user to remove music from the creation
     */
    @FXML
    private void handleRemoveMusic(){
            removeMusicJob removeMusic = new removeMusicJob();
            team.submit(removeMusic);
            removeMusic.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    addedMusic = false;
                    if(creationPlayingThing.getStatus() == MediaPlayer.Status.PLAYING){
                        pauseMedia();
                    }
                    initialize();
                }
            });
    }

    /**
     * creates all the files required so the creation which the user is creating can be accessed by the media player and can be accessed by the quiz page.
     */
    @FXML
    private void createAllThingsNecessary(){
        boolean okayName = true;
        File creationFile = new File("creations");

        if(!nameIsValid(creationNameInput.getText())){
            return;
        }

        for(File creationMade : creationFile.listFiles()){
            if(creationMade.getName().equals(creationNameInput.getText())){
                okayName = false;
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "A Creation already has this name. Please try again");
                alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                Optional<ButtonType> result = alert.showAndWait();
                break;
            }
        }
        if(okayName) {
            File containerForAll = new File(creationNameInput.getText());
            containerForAll.mkdir();
            File userCreation = new File(".temp" + System.getProperty("file.separator") + "final_creation.mp4");
            File quizElement1 = new File(".temp" + System.getProperty("file.separator") + "quiz1.mp4");
            File quizElement2 = new File(".temp" + System.getProperty("file.separator") + "quiz2.mp4");
            File quizElement3 = new File(".temp" + System.getProperty("file.separator") + "quiz3.mp4");
            File searchTerm = new File(".temp" + System.getProperty("file.separator") + "temp_searchterm.txt");
            userCreation.renameTo(new File(creationNameInput.getText() + System.getProperty("file.separator") + creationNameInput.getText() + ".mp4"));
            quizElement1.renameTo(new File(creationNameInput.getText() + System.getProperty("file.separator") + "quiz1.mp4"));
            quizElement2.renameTo(new File(creationNameInput.getText() + System.getProperty("file.separator") + "quiz2.mp4"));
            quizElement3.renameTo(new File(creationNameInput.getText() + System.getProperty("file.separator") + "quiz3.mp4"));
            searchTerm.renameTo(new File(creationNameInput.getText() + System.getProperty("file.separator") + "answer.txt"));
            containerForAll.renameTo(new File("creations" + System.getProperty("file.separator") + creationNameInput.getText()));
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Your creation is complete!");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            Optional<ButtonType> result = alert.showAndWait();
            try {
                deleteAllFiles();
                if(creationPlayingThing.getStatus() == MediaPlayer.Status.PLAYING){
                    creationPlayingThing.pause();
                }
                switchScenes(rootBorderPane, "MainMenu.fxml");
            } catch (Exception e) {

            }
        }
    }

    /**
     * delets all the unnecessary files that are left behind
     * @throws IOException
     */
    private void deleteAllFiles() throws IOException {
        File file = new File(".temp");
        for (File insideFile : file.listFiles()) {
            insideFile.delete();
        }
    }

    /**
     * makes the "remove music" button enabled or disabled
     */
    private void setMusicButtons(){
        if(addedMusic){
            removeMusicButton.setDisable(false);
        }else{
            removeMusicButton.setDisable(true);
        }
        addMusicButton.setDisable(false);
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Creation file name");
            alert.showAndWait();
            return false;
        }
        return true;
    }
}
