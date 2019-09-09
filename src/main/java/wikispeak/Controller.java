package wikispeak;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Abstract controller class. Provides scene switching functionality.
 */
public abstract class Controller {

    /**
     * Changes the scene
     * @param fxml scene to switch to
     * @throws IOException
     */
    public void switchScenes(BorderPane root, String fxml) throws IOException {
        //use fxmlloader to change the fxml file
        Parent pane = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage) root.getScene().getWindow();
        Scene scene = stage.getScene();

        //change and show the scene
        scene = new Scene(pane);
        stage.setScene(scene);
        stage.sizeToScene();
    }

}
