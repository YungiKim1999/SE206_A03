package wikispeak;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import wikispeak.quiz.QuizHolder;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class QuizStartScreenController extends Controller {

    @FXML private BorderPane rootBorderPane;
    @FXML private Slider numberSlider;
    @FXML private Text alertText;
    @FXML private Button startButton;

    public void initialize() throws IOException {

        int numberOfQuestionsAvailable;

        //Finds number of questions available
        File file = new File("creations");
        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });

        numberOfQuestionsAvailable = files.length * 3;

        //set 10 as max, else set max to maximum available quiz questions
        if(numberOfQuestionsAvailable >= 10){
            numberSlider.setMax(10);
        }
        else{
            numberSlider.setMax(numberOfQuestionsAvailable);
        }
        //show the alert text if no creations have been made
        if(numberOfQuestionsAvailable == 0){
            numberSlider.setDisable(true);
            startButton.setDisable(true);
            alertText.setVisible(true);
        }

        //listen to changes in the slider
        numberSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            int intValue = newValue.intValue();
            numberSlider.setValue(intValue);
            startButton.setDisable(intValue == 0);
        });

    }

    @FXML
    public void handleStartQuiz() throws IOException {
        Double valueSelected = numberSlider.getValue();
        int valueSelectedInt = valueSelected.intValue();
        QuizHolder.makeQuiz(valueSelectedInt);
        switchScenes(rootBorderPane, "QuizScreen.fxml");
    }

    @FXML
    public void handleMainMenu() throws IOException {
        switchScenes(rootBorderPane, "MainMenu.fxml");
    }
}
