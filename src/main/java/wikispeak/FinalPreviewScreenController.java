package wikispeak;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FinalPreviewScreenController extends ListController {
    File fileURL;

    ObservableList<String> creationsStrings = FXCollections.observableArrayList(populateList("creations", ""));
    private boolean play = false;
    private boolean firsTime;
    private  MediaPlayer creationPlayingThing = null;

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

    @FXML
    public void initialize(){

        //create button is only available if some text is entered in the field
        creationNameInput.textProperty().addListener((observable, oldValue, newValue) -> {
            createButton.setDisable(creationNameInput.getText().trim().isEmpty());
        });

        previousCreations.setItems(creationsStrings);
        firsTime = true;
        timeLabel.setText("00:00");
        finishTime.setText("00:00");
        setMediaForPlay();
        addVideoListener();
        addVolumeListener();
        playMedia();

        volumeSlider.setValue(100);

    }

    private void addVolumeListener(){
        volumeSlider.valueProperty().addListener(observable -> {
            creationPlayingThing.setVolume(volumeSlider.getValue() / 100);
        });
    }
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

    private void playMedia(){
        creationPlayingThing.play();
        playPauseButton.setText("Play/Pause");
    }

    private void pauseMedia(){
        creationPlayingThing.pause();
    }

    private void setMediaForPlay(){
        if (!firsTime && creationPlayingThing.getStatus() == MediaPlayer.Status.PLAYING){
            creationPlayingThing.dispose();
        }
        fileURL =new File( ".temp" + System.getProperty("file.separator") + "final_creation.mp4");
        Media playCreation = new Media(fileURL.toURI().toString());
        creationPlayingThing = new MediaPlayer(playCreation);
        creationViewer.setMediaPlayer(creationPlayingThing);
    }


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

    @FXML
    private void handleForwardButton(){
        if(play){
            creationPlayingThing.seek(creationPlayingThing.getCurrentTime().add(Duration.seconds(2)));
        }
    }

    @FXML
    private void handleBackButton(){
        if(play){
            creationPlayingThing.seek(creationPlayingThing.getCurrentTime().subtract(Duration.seconds(2)));
        }
    }

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
            userCreation.renameTo(new File(creationNameInput.getText() + System.getProperty("file.separator") + creationNameInput.getText() + ".mp4"));
            quizElement1.renameTo(new File(creationNameInput.getText() + System.getProperty("file.separator") + "quiz1.mp4"));
            quizElement2.renameTo(new File(creationNameInput.getText() + System.getProperty("file.separator") + "quiz2.mp4"));
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

    private void deleteAllFiles() throws IOException {
        File file = new File(".temp");
        for (File insideFile : file.listFiles()) {
            insideFile.delete();
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Creation file name");
            alert.showAndWait();
            return false;
        }
        return true;
    }
}
