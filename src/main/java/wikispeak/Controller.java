package wikispeak;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import wikispeak.helpers.Command;

import java.io.IOException;

/**
 * Abstract controller class. Provides scene switching functionality.
 */
public abstract class Controller {

    @FXML Button helpButton;

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

    /**
     * Concurrently opens the User Manual PDF
     */
    @FXML
    public void openUserManual(){
        Thread openPDFThread = new Thread(new Task<Void>(){
            @Override
            protected Void call() throws Exception {
                Command command = new Command("xdg-open VARpedia_User_Manual.pdf");
                command.execute();
                return null;
            }
        });
        openPDFThread.start();

    }


}
