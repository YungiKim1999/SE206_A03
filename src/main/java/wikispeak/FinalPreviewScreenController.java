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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;


public class FinalPreviewScreenController extends ListController {
    File fileURL;

    ObservableList<String> creationsStrings = FXCollections.observableArrayList(populateList("creations", ".mp4"));
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

    @FXML
    public void initialize(){

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
    private void handleExitButton()throws IOException {
        if(play == true){
            pauseMedia();
        }
        switchScenes(rootBorderPane, "MainMenu.fxml");
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
        fileURL =new File( "final_creation.mp4");
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
}
