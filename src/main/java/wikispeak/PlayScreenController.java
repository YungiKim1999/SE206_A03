package wikispeak;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * PlayScreen Controller class
 */
public class PlayScreenController extends ListController {

    @FXML private BorderPane rootBorderPane;
    @FXML private VBox creationVBox;
    @FXML private Text infoText;

    @FXML
    public void initialize() {
        List<String> currentCreations = populateList();

        for(String creation : currentCreations){
            Button button = new Button(creation);
            button.setOnAction(e -> {
                playCreation(creation);
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

    private void playCreation(String creation){
        String command = "ffplay -autoexit creations" + System.getProperty("file.separator") + creation + ".mp4";
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        try{
            pb.start();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

}
