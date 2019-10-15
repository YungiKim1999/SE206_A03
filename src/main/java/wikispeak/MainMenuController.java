package wikispeak;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * MainMenu Controller class
 * Automatically instantiated when MainMenu fxml is loaded
 */
public class MainMenuController extends Controller {

    @FXML private BorderPane rootBorderPane;

    @FXML
    private void handleCreate() throws IOException {
        switchScenes(rootBorderPane, "SearchScreen.fxml");
    }

    @FXML
    private void handlePlay() throws IOException {
        switchScenes(rootBorderPane,"UpgradedPlayScreen.fxml");
    }

    @FXML
    private void handleQuiz() throws IOException {
        switchScenes(rootBorderPane,"QuizScreen.fxml");
    }

}
