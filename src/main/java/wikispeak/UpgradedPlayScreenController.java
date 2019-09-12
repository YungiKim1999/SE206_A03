package wikispeak;

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


public class UpgradedPlayScreenController extends ListController {

    ObservableList<String> creationsStrings = FXCollections.observableArrayList(populateList());
    String  selectedCreation = null;
    File fileURL;

    private boolean play = false;
    private boolean firsTime;
    private  MediaPlayer creationPlayingThing = null;

    @FXML private MediaView creationViewer;
    @FXML private BorderPane rootBorderPane;
    @FXML private ListView<String> creationList;
    @FXML private Slider volumeSlider;
    @FXML private Label timeLabel;
    @FXML private Label finishTime;
    @FXML private Button playPauseButton;
    @FXML private Slider videoBuffer;

    @FXML
    public void initialize(){
        firsTime = true;
        creationList.setItems(creationsStrings);
        creationList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        timeLabel.setText("00:00");
        finishTime.setText("00:00");
        creationList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    getUserChoice(newValue);
                    setMediaForPlay();
                    playMedia();
                    addVideoListener();
                    addVolumeListener();
                    firsTime = false;
                    play = true;
            }
        });
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
        currentTime += String.format("%02d", (int)newVakue.toSeconds());
        timeLabel.setText(currentTime);
        String stopTime = "";
        stopTime += String.format("%02d", (int)creationPlayingThing.getStopTime().toMinutes());
        stopTime += ":";
        stopTime += String.format("%02d", (int)creationPlayingThing.getStopTime().toSeconds());
        finishTime.setText(stopTime);
    }

    private void addVideoListener() {
        creationPlayingThing.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                videoBuffer.setMin(0);
                int endTime = Integer.parseInt(String.format("%02d", (int)creationPlayingThing.getStopTime().toMinutes())) * 60 +Integer.parseInt( String.format("%02d", (int)creationPlayingThing.getStopTime().toSeconds()));
                int currentTime = Integer.parseInt(String.format("%02d", (int)newValue.toMinutes())) * 60 +Integer.parseInt( String.format("%02d", (int)newValue.toSeconds()));
                videoBuffer.setMax(endTime);
                videoBuffer.setValue(currentTime);
                setTimeLabels(newValue);
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
        fileURL =new File("." + System.getProperty("file.separator") +"creations" + System.getProperty("file.separator") + selectedCreation + ".mp4");
        Media playCreation = new Media(fileURL.toURI().toString());
        creationPlayingThing = new MediaPlayer(playCreation);
        creationViewer.setMediaPlayer(creationPlayingThing);
    }

    private void getUserChoice(String selection){
        selectedCreation = selection;
    }


    @FXML
    private void handlePlayPauseButton(){
        if(!firsTime) {
            if(playPauseButton.getText().equals("Repeat")){
                creationPlayingThing.stop();
                selectedCreation = creationList.getSelectionModel().getSelectedItem();
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
