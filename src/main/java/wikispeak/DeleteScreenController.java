package wikispeak;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for DeleteScreen
 */
public class DeleteScreenController extends ListController {

    @FXML private BorderPane rootBorderPane;
    @FXML private VBox creationVBox;
    @FXML private Text infoText;

    @FXML
    public void initialize() {
        List<String> currentCreations = populateList("creations", ".mp4");

        for(String creation : currentCreations){
            Button button = new Button(creation);
            button.setOnAction(e -> {
                try {
                    deleteCreation(creation);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            creationVBox.getChildren().add(button);
        }

        //check that the default text persists
        if(currentCreations.isEmpty()){
            infoText.setText("No creations found.");
        }
    }

    @FXML
    private void handleMainMenu() throws IOException {
        switchScenes(rootBorderPane, "MainMenu.fxml");
    }

    /**
     * Deletes specified creation after asking for confirmation
     * @param creation
     * @throws IOException
     */
    private void deleteCreation(String creation) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete \"" + creation + "\"?");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            File file = new File("creations" + System.getProperty("file.separator") + creation + ".mp4");
            file.delete();

            //refresh the scene
            switchScenes(rootBorderPane, "DeleteScreen.fxml");
        }
    }

}
