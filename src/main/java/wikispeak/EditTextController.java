package wikispeak;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import wikispeak.helpers.Command;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Controller for EditText.fxml
 */
public class EditTextController extends Controller{

    @FXML private BorderPane rootBorderPane;
    @FXML private TextArea textOutput;

    //Populates the text area
    public void initialize() throws IOException {
        Command command = new Command("cat .temp" + System.getProperty("file.separator") + "temp_text.txt");
        command.execute();
        textOutput.setText(command.getStream());
    }

    @FXML
    /**
     * Takes the user back to the search screen. Confirms they are happy to abandon any progress
     */
    private void handleBackToSearch() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to go back?\nAny progress will be lost.");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            //only switch scene after confirmation
            switchScenes(rootBorderPane, "SearchScreen.fxml");
        }
    }

    @FXML
    /**
     * Takes the user to the next screen where they combine audio files
     * Saves the text area so any edits persist if the user wants to go back
     */
    private void handleDone() throws IOException {
        //save the current text area status to the text file
        String text = textOutput.getText();
        Command command = new Command("echo \"" + text + "\" > .temp" + System.getProperty("file.separator") + "temp_text.txt");
        command.execute();
        switchScenes(rootBorderPane, "CreateAudioScreen.fxml");
    }

}
