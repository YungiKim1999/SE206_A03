package wikispeak;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
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

    @FXML
    public void initialize(){
        firsTime = true;
        creationList.setItems(creationsStrings);
        creationList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        creationList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                System.out.println(oldValue + " = old");
                System.out.println(newValue + " = new");
                if(oldValue == null){
                    getUserChoice(newValue);
                    setMediaForPlay();
                    playMedia();
                    firsTime = false;
                    play = true;
                }else{
                    creationPlayingThing.stop();
                    getUserChoice(newValue);
                    setMediaForPlay();
                    playMedia();
                    play = true;
                }


            }
        });
        volumeSlider.setValue(100);
        volumeSlider.valueProperty().addListener(observable -> {
            if(play) {
                creationPlayingThing.setVolume(volumeSlider.getValue() / 100);
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
    }

    private void pauseMedia(){
        creationPlayingThing.pause();
    }

    private void setMediaForPlay(){
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
            if (creationPlayingThing.getStatus() == MediaPlayer.Status.PLAYING) {
                System.out.println("Paused");
                pauseMedia();
                play = false;
            } else if (creationPlayingThing.getStatus() == MediaPlayer.Status.PAUSED) {
                System.out.println("Playing");
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
