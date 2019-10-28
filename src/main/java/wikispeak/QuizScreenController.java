package wikispeak;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.util.Duration;
import wikispeak.quiz.Quiz;
import wikispeak.quiz.QuizHolder;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class QuizScreenController extends Controller {

    @FXML private BorderPane rootBorderPane;

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

    //Video related components
    @FXML private Button playPauseRepeatButton;
    @FXML private Slider videoBuffingSlider;
    @FXML private Slider volumeSlider;
    @FXML private Label currentTimeLabel;
    @FXML private Label finishTimeLabel;

    //Quiz related components
    @FXML private Text currentQuestionNumber;
    @FXML private Text correctNumber;
    @FXML private Text incorrectNumber;

    @FXML private TextField answerField;
    @FXML private Button submitButton;

    private Quiz quiz;

    /**
     * This string will determine what creation quiz video to play
     */
    private String selectedQuestionToPlay;


    @FXML
    public void initialize() {
        answerField.textProperty().addListener((observable, oldValue, newValue) -> {
            submitButton.setDisable(newValue.trim().isEmpty());
        });
        quiz = QuizHolder.getCurrentQuiz();
        startCurrentQuestion();
        setTimeLabels(new Duration(0));
    }


    /**
     *This will set the media file ready for it to be played
     */
    private void getVideoReady(){
        if(mediaPlayer != null){
            mediaPlayer.dispose();
        }
        File quizURL = new File("creations" + System.getProperty("file.separator") + selectedQuestionToPlay + ".mp4");
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
     * Puts GUI in the "Current Question" state
     */
    private void startCurrentQuestion(){
        videoBuffingSlider.setValue(0);
        playPauseRepeatButton.setText("Play");
        answerField.setText("");
        setQuizLabels();
        selectedQuestionToPlay = quiz.getCurrentQuestion();
        getVideoReady();
    }

    /**
     * Sets all GUI quiz labels based on the current state of the quiz
     */
    private void setQuizLabels(){
        currentQuestionNumber.setText("" + quiz.getCurrentQuestionNumber() + "/" + quiz.getTotalNumberOfQuestions());
        correctNumber.setText("" + quiz.getNumberCorrect());
        incorrectNumber.setText("" + quiz.getNumberIncorrect());
    }

    /**
     * Handles the progression of the quiz
     */
    @FXML
    private void handleSubmit() throws IOException {
        String answer = quiz.getCurrentAnswer();
        Boolean result = quiz.submitResponse(answerField.getText());
        if(result){
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "The answer was: " + answer);
            alert.setHeaderText("Correct!");
            alert.setTitle("Answer");
            alert.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("correctTick.png"))));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
        }
        else{

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "The correct answer was: " + answer);
            alert.setHeaderText("Incorrect");
            alert.setTitle("Answer");
            alert.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("incorrectCross.png"))));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
        }

        if(quiz.isFinished()){
            String message = "You got " + quiz.getNumberCorrect() + " questions right and " + quiz.getNumberIncorrect() + " wrong";
            if(quiz.getNumberIncorrect() > 0){
                message += "\n\nYou should review the following Creations: \n" + quiz.getCreationsToReview();
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
            alert.setHeaderText("Vocabulary Test Results");
            alert.showAndWait();
            switchScenes(rootBorderPane, "QuizStartScreen.fxml");
        }
        else{
            startCurrentQuestion();
        }

    }

    /**
     * Gives a pop-up hint to the user based on the current question
     */
    @FXML
    private void handleGetHint(){
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "The answer starts with the letter " + quiz.getCurrentHint());
        alert.setHeaderText("Question Hint");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    /**
     * Takes the user back to the quiz start page, confirms they want to end the quiz
     */
    @FXML
    private void handleExitQuiz() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to stop the test?");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            //only switch scene after confirmation
            if(mediaPlayer != null){
                mediaPlayer.dispose();
            }
            switchScenes(rootBorderPane, "QuizStartScreen.fxml");
        }
    }

}





