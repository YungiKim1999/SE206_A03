package wikispeak;

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
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
    @FXML private Label listIsEmpty;
    private ExecutorService workerTeam = Executors.newSingleThreadExecutor();

    @FXML
    public void initialize(){
        firsTime = true;
        listIsEmpty.setText("There seems to be\nno creation to\nplay/delete");
        creationList.setItems(creationsStrings);
        creationList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        timeLabel.setText("00:00");
        finishTime.setText("00:00");
        setEmptyLabelText();
        creationList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                   if(!creationList.getItems().isEmpty()) {
                       setEmptyLabelText();
                       getUserChoice(newValue);
                       setMediaForPlay();
                       playMedia();
                       addVideoListener();
                       addVolumeListener();
                       firsTime = false;
                       play = true;
                   }else{
                       setEmptyLabelText();
                       creationPlayingThing.dispose();
                       timeLabel.setText("00:00");
                       finishTime.setText("00:00");
                       videoBuffer.setValue(0);
                   }
            }
        });
        volumeSlider.setValue(100);

    }

    private void setEmptyLabelText(){
        if(!creationList.getItems().isEmpty()) {
            listIsEmpty.setVisible(false);
        }else{
            listIsEmpty.setVisible(true);
        }
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
        if(!firsTime && !creationList.getItems().isEmpty()) {
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
    private void handleDeleteButton(){
        if(selectedCreation != null && !creationList.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete \"" + selectedCreation + "\"?");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                deletionJobs deleteSelected = new deletionJobs(selectedCreation);
                workerTeam.submit(deleteSelected);
                creationList.getItems().remove(selectedCreation);
                setEmptyLabelText();
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
