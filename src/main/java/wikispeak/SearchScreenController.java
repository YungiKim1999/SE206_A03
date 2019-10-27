package wikispeak;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import wikispeak.helpers.Command;

import java.io.File;
import java.io.IOException;

public class SearchScreenController extends Controller {

    @FXML private BorderPane rootBorderPane;
    @FXML private TextField searchField;
    @FXML private Text infoText;
    @FXML private Button searchButton;
    @FXML private ProgressIndicator searchProgress;

    /**
     * Initial clean-up.
     * Configures the search button to only be pressable if some text is entered
     * Deletes any generated audio files and images.
     */
    public void initialize(){
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchButton.setDisable(newValue.trim().isEmpty());
        });
        for (File file : new File(".temp" + System.getProperty("file.separator") + "audio").listFiles()){
            file.delete();
        }
        for (File file : new File(".temp" + System.getProperty("file.separator") + "downloads").listFiles()){
            file.delete();
        }
        for (File file : new File(".temp" + System.getProperty("file.separator") + "images_to_use").listFiles()){
            file.delete();
        }
    }

    @FXML
    /**
     * Searches for a user-provided search term on Wikipedia.
     */
    private void handleSearch() {

        final String currentSearch = searchField.getText().toLowerCase();

        if (!currentSearch.isEmpty()) {
            infoText.setText("");
            searchProgress.setVisible(true);

            Thread searchThread = new Thread(new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Command wikitCommand = new Command("wikit " + currentSearch + " | sed 's/\\([.!?]\\) \\([[:upper:]]\\)/\\1\\n\\n\\2/g' | tee .temp" + System.getProperty("file.separator") + "temp_text.txt > .temp" + System.getProperty("file.separator") + "originalsearch_text.txt");
                    wikitCommand.execute();
                    return null;
                }

                @Override
                protected void done() {
                    Platform.runLater(() -> {
                        try {
                            postSearchUpdateGUI(currentSearch);
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
    private void postSearchUpdateGUI(String currentSearch) throws IOException {
        Command command = new Command("cat .temp" + System.getProperty("file.separator") + "temp_text.txt | grep -Fwq \":^(\"");
        if(command.execute() == 0){
            //nothing found on Wikipedia, update GUI to inform user
            command = new Command("cat .temp" + System.getProperty("file.separator") + "temp_text.txt");
            command.execute();
            infoText.setText(command.getStream());
            searchProgress.setVisible(false);
        }
        else {
            command = new Command("echo \"" + currentSearch + "\" > .temp" + System.getProperty("file.separator") + "temp_searchterm.txt");
            command.execute();
            //term found on wikipedia, go to next screen
            switchScenes(rootBorderPane, "EditText.fxml");
        }
    }

    /**
     * Switches to the MainMenu screen
     */
    @FXML
    private void handleMainMenu() throws IOException {
        switchScenes(rootBorderPane, "MainMenu.fxml");
    }
}
