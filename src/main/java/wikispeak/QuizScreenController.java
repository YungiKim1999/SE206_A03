package wikispeak;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;

public class QuizScreenController {
    /**
     * The "viewingWindow" is the physical "window" that you put into in the scene builder
     * This "window" has a "MediaPlayer" type object which will play your video for you.
     * Link this up to a MediaView on scene builder to player videos
     */
    @FXML private MediaView viewingWindow;
    /**
     * The "mediaPlayer" is the object which goes inside of a "MediaView" in order for videos to be played.
     * You set videos to play in this object as well as tell this object to play, pause, stop, and dispose the video
     * it is currently playing.
     * Put this into a "MediaView" in order to play/pause videos
     */
    private MediaPlayer mediaPlayer;

    /**
     * I'll just have one button to do most of these things for this player
     */
    @FXML private Button playPauseRepeatButton;
    /**
     * This is the slider that will be used in order to show the user where the video is currently
     */
    @FXML private Slider videoBuffingSlider;
    /**
     * Yeah, I don't know how to comment on this without repeating the name...
     */
    @FXML private Slider volumeSlider;
    /**
     * This is the label to show the current time of the video
     */
    @FXML private Label currentTimeLabel;
    /**
     * This is the label to show the end time of the video
     */
    @FXML private Label finishTimeLabel;
    /**
     * This string will determine what creation folder you would like to go into
     */
    private String selectedCreationToPlay;
    /**
     * This String will determine what quiz video will be selected
     */
    private String selectedQuizElement;

    @FXML
    public void initialize() {
        quizRandomizer();
        getVideoReady();
    }


    /**
     *This will set the media file ready for it to be played
     */
    private void getVideoReady(){
        File quizURL = new File("creations" + System.getProperty("file.separator") + selectedCreationToPlay + System.getProperty("file.separator") + selectedQuizElement +".mp4");
        Media quizToPay = new Media(quizURL.toURI().toString());
        mediaPlayer = new MediaPlayer(quizToPay);
        viewingWindow.setMediaPlayer(mediaPlayer);
        addVolumeListener();
        addVideoBufferListener();
    }

    /**
     * This method sets a listener on the volume slider and sets the volume of the media player to that volume every time there is a change
     */
    private void addVolumeListener(){
        volumeSlider.valueProperty().addListener(observable -> {
            mediaPlayer.setVolume(volumeSlider.getValue() / 100);
        });
    }

    /**
     * This method sets the video time labels to update with the video playing as well as the video buffer slider
     */
    private void addVideoBufferListener(){
        //A listener is set on the time property (Event is triggered every time the current time of the video is changed)
        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                //Set the minimum value of the slider to be 0
                videoBuffingSlider.setMin(0);

                //Get the current time of the video as well as the ending time of the video
                double endTime = mediaPlayer.getStopTime().toMillis();
                double currentTime = newValue.toMillis();

                //Set the max value of the video to the end time
                videoBuffingSlider.setMax(endTime);

                //Set the time labels so that the user can see whats going on.
                setTimeLabels(newValue);

                //Set the video buffer to the correct place as well
                videoBuffingSlider.setValue(currentTime);

                //We add a listener to the video buffer as well so that it can can be clicked on to change the video
                //and allow it to be scrubbed through
                videoBuffingSlider.valueProperty().addListener(new InvalidationListener() {
                    @Override
                    public void invalidated(Observable observable) {
                        if (videoBuffingSlider.isPressed()) {
                            Duration newTime = new Duration(videoBuffingSlider.getValue());
                            mediaPlayer.seek(newTime);
                        }
                    }
                });

                //At the end of the video, we allow the video to be played again
                mediaPlayer.setOnEndOfMedia(new Runnable() {
                    @Override
                    public void run() {
                        mediaPlayer.stop();
                        playPauseRepeatButton.setText("Repeat");
                    }
                });

            }
        });
    }

    /**
     * This method allows the 2 labels to be set to the correct times.
     * One will indicate at what point the video is
     * The other will indicate when the video will finish
     * @param newValue : The value which the video is currently at.
     */
    private void setTimeLabels(Duration newValue) {
        String currentTime = "";
        currentTime += String.format("%02d", (int) newValue.toMinutes());
        currentTime += ":";
        currentTime += String.format("%02d", (int) newValue.toSeconds() % 60);
        currentTimeLabel.setText(currentTime);
        String stopTime = "";
        stopTime += String.format("%02d", (int) mediaPlayer.getStopTime().toMinutes());
        stopTime += ":";
        stopTime += String.format("%02d", (int) mediaPlayer.getStopTime().toSeconds() % 60);
        finishTimeLabel.setText(stopTime);
    }

    private void playMedia() {
        mediaPlayer.play();
        playPauseRepeatButton.setText("Pause");
    }

    private void pauseMedia() {
        mediaPlayer.pause();
        playPauseRepeatButton.setText("Play");
    }

    /**
     * This will handle all the play/pause/repeat functionality of a button
     */
    @FXML
    private void handlePlayPauseButton() {
            if (playPauseRepeatButton.getText().equals("Repeat")) {
                getVideoReady();
                playMedia();
            } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                pauseMedia();
            } else{
                playMedia();

            }
        }

    @FXML
    private void handleForwardButton() {
            mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(2)));
    }

    @FXML
    private void handleBackButton() {
            mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(2)));

    }

    /**
     * This is the function which will be used to randomise the quiz stuff
     * The way I set up the API is you just need to set the "selectedCreationToPlay" and the "selectedQuizElement"
     * and then call "getVideoReady" method and everything will be done for you.
     * I have put this in the "initialize" method as i think you would randomize, set the video, and then have the
     * user answer the question
     * Right now, it is just playing a creation that i have made...
     */
    private void quizRandomizer(){
        selectedCreationToPlay = "goats";
        selectedQuizElement = "quiz2";
    }
}





