package wikispeak;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.io.IOException;

public class SearchScreenController extends Controller {

    @FXML private BorderPane rootBorderPane;
    @FXML private TextField searchField;
    @FXML private Text infoText;

    /**
     * Switches to the MainMenu screen
     */
    @FXML
    private void handleMainMenu() throws IOException {
        switchScenes(rootBorderPane, "MainMenu.fxml");
    }

    @FXML
    /**
     * Searches for a user-provided search term on Wikipedia.
     */
    private void handleSearch() {

        String currentSearch = searchField.getText();

        if (!currentSearch.isEmpty()) {
            infoText.setText("Searching...");

            Thread searchThread = new Thread(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Command wikitCommand = new Command("wikit " + currentSearch + " | sed 's/\\([.!?]\\) \\([[:upper:]]\\)/\\1\\n\\2/g' > .temp_text.txt");
                    wikitCommand.execute();
                    return null;
                }

                @Override
                protected void done() {
                    Platform.runLater(() -> {
                        try {
                            postSearchUpdateGUI();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });

            searchThread.start();
        }
    }

    /**
     * Updates the GUI appropriately based on the outcome of a Wikit Search
     */
    private void postSearchUpdateGUI() throws IOException {
        Command command = new Command("cat .temp_text.txt | grep -Fwq \":^(\"");
        if(command.execute() == 0){
            //nothing found on Wikipedia, update GUI to inform user
            command = new Command("cat .temp_text.txt");
            command.execute();
            infoText.setText(command.getStream());
        }
        else {
            //term found on wikipedia, go to next screen
            switchScenes(rootBorderPane, "CreateAudioScreen.fxml");
        }
    }
}
