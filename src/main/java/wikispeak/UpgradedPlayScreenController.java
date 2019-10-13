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
import wikispeak.tasks.deletionJobs;
import wikispeak.tasks.newDeletionJob;


import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The UpgradedPlayScreenController is a better version of the normal "PlayScreenController" where it has the embedded
 * video player as well as other functions in the player which was not in the base model.
 */
public class UpgradedPlayScreenController extends ListController {

    ObservableList<String> creationsStrings = FXCollections.observableArrayList(populateList("creations", ""));
    String selectedCreation = null;
    String selectedDir = null;
    File fileURL;

    private boolean play = false;
    private boolean deleted = false;
    private boolean firsTime;
    private MediaPlayer creationMediaPlayer = null;

    @FXML
    private Button deleteButton;
    @FXML
    private MediaView creationViewer;
    @FXML
    private BorderPane rootBorderPane;
    @FXML
    private ListView<String> creationList;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Label timeLabel;
    @FXML
    private Label finishTime;
    @FXML
    private Button playPauseButton;
    @FXML
    private Slider videoBuffer;
    @FXML
    private Label listIsEmpty;
    private ExecutorService workerTeam = Executors.newSingleThreadExecutor();

    @FXML
    public void initialize() {
        playPauseButton.setStyle("-fx-base: rgb(30,170,255);");
        deleteButton.setStyle("-fx-base: red;");
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
                if (!creationList.getItems().isEmpty()) {
                    setEmptyLabelText();
                    getUserChoice(newValue);
                    setMediaForPlay();
                    firsTime = false;
                    if(deleted){
                        deleted = false;
                        play = false;
                    }else {
                        playMedia();
                        play = true;
                    }

                } else {
                    setEmptyLabelText();
                    creationMediaPlayer.dispose();
                    timeLabel.setText("00:00");
                    finishTime.setText("00:00");
                    videoBuffer.setValue(0);
                }
            }
        });
        volumeSlider.setValue(100);

    }

    /**
     * displays a message to the user telling them that there is no creation that has been created
     */
    private void setEmptyLabelText() {
        if (!creationList.getItems().isEmpty()) {
            listIsEmpty.setVisible(false);
        } else {
            listIsEmpty.setVisible(true);
        }
    }

    private void addVolumeListener() {
        volumeSlider.valueProperty().addListener(observable -> {
            creationMediaPlayer.setVolume(volumeSlider.getValue() / 100);
        });
    }

    /**
     * sets the time labels for the video player(current time and how long the video is going to be).
     * @param newValue
     */
    private void setTimeLabels(Duration newValue) {
        String currentTime = "";
        currentTime += String.format("%02d", (int) newValue.toMinutes());
        currentTime += ":";
        currentTime += String.format("%02d", (int) newValue.toSeconds() % 60);
        timeLabel.setText(currentTime);
        String stopTime = "";
        stopTime += String.format("%02d", (int) creationMediaPlayer.getStopTime().toMinutes());
        stopTime += ":";
        stopTime += String.format("%02d", (int) creationMediaPlayer.getStopTime().toSeconds() % 60);
        finishTime.setText(stopTime);
    }

    private void addVideoListener() {
        creationMediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                videoBuffer.setMin(0);
                double endTime = creationMediaPlayer.getStopTime().toMillis();
                double currentTime = newValue.toMillis();
                videoBuffer.setMax(endTime);
                setTimeLabels(newValue);
                videoBuffer.setValue(currentTime);
                videoBuffer.valueProperty().addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        if (videoBuffer.isPressed()) {
                            Duration newTime = new Duration(videoBuffer.getValue());
                            creationMediaPlayer.seek(newTime);
                        }
                    }
                });
                creationMediaPlayer.setOnEndOfMedia(new Runnable() {
                    @Override
                    public void run() {
                        creationMediaPlayer.stop();
                        playPauseButton.setText("Repeat");
                    }
                });

            }
        });
    }

    @FXML
    private void handleExitButton() throws IOException {
        if (play == true) {
            pauseMedia();
        }
        switchScenes(rootBorderPane, "MainMenu.fxml");
    }

    private void playMedia() {
        creationMediaPlayer.play();
        playPauseButton.setText("Pause");
    }

    private void pauseMedia() {
        creationMediaPlayer.pause();
        playPauseButton.setText("Play");
    }

    /**
     * prepares the media file to be playable in the media viewer
     */
    private void setMediaForPlay() {
        if (!firsTime && creationMediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            creationMediaPlayer.dispose();
        }
        fileURL = new File( "creations" + System.getProperty("file.separator") + selectedCreation + ".mp4");
        Media playCreation = new Media(fileURL.toURI().toString());
        creationMediaPlayer = new MediaPlayer(playCreation);
        creationViewer.setMediaPlayer(creationMediaPlayer);
        addVideoListener();
        addVolumeListener();
    }

    private void getUserChoice(String selection) {
        selectedCreation = selection + System.getProperty("file.separator") + selection;
        selectedDir = selection;
    }

    /**
     * handles the situation where the user would like to start/stop or repeat the creation again.
     */
    @FXML
    private void handlePlayPauseButton() {
        if (!firsTime && !creationList.getItems().isEmpty()) {
            if (playPauseButton.getText().equals("Repeat")) {
                creationMediaPlayer.stop();
                String repeatingCreation = creationList.getSelectionModel().getSelectedItem();
                getUserChoice(repeatingCreation);
                setMediaForPlay();
                playMedia();
                play = true;
            } else if (creationMediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                pauseMedia();
                play = false;
            } else{
                playMedia();
                play = true;

            }
        }
    }

    @FXML
    private void handleDeleteButton() {
        if (selectedCreation != null && !creationList.getItems().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete \"" + selectedCreation + "\"?");
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                newDeletionJob deleteSelected = new newDeletionJob(selectedDir);
                workerTeam.submit(deleteSelected);
                creationList.getItems().remove(selectedDir);
                setEmptyLabelText();
                deleted = true;

            }
        }
    }

    @FXML
    private void handleForwardButton() {
        if (play) {
            creationMediaPlayer.seek(creationMediaPlayer.getCurrentTime().add(Duration.seconds(2)));
        }
    }

    @FXML
    private void handleBackButton() {
        if (play) {
            creationMediaPlayer.seek(creationMediaPlayer.getCurrentTime().subtract(Duration.seconds(2)));
        }
    }
}
