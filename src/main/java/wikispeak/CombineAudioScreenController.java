package wikispeak;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class CombineAudioScreenController extends ListController {

    @FXML private BorderPane rootBorderPane;
    @FXML private VBox audioListBox;
    @FXML private ComboBox numberSelection;
    @FXML private Text infoText;
    @FXML private Button creationButton;

    //Hashset to ensure no duplicates
    private HashSet<SelectableFile> _selectedFiles = new HashSet<>();

    public void initialize() {

        //Turning a list of String audio files into SelectableFile objects that have a "selected" property
        List<String> audioFileList = populateList("audio", ".wav");

        ListView<SelectableFile> listView = new ListView<>();

        for (String fileName : audioFileList) {
            SelectableFile selectableFile = new SelectableFile(fileName, false);
            listView.getItems().add(selectableFile);

            //Observer Selected property and add/remove the file from _selectedFiles appropriately
            selectableFile.selectedProperty().addListener((obs, previouslySelected, currentlySelected) -> {
                if(currentlySelected){
                    _selectedFiles.add(selectableFile);
                }
                else if(_selectedFiles.contains(selectableFile)){
                    _selectedFiles.remove(selectableFile);
                }
                updateButtonAccess();
            });
        }

        listView.setCellFactory(CheckBoxListCell.forListView(new Callback<SelectableFile, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(SelectableFile selectableFile) {
                return selectableFile.selectedProperty();
            }
        }));

        if(audioFileList.size() == 0){
            infoText.setText("No audio files found");
        }
        else{
            audioListBox.getChildren().add(listView);
        }
        populateImageNumberSelectionBox();
    }

    @FXML
    private void handleBackToCreateAudio() throws IOException {
        switchScenes(rootBorderPane, "CreateAudioScreen.fxml");
    }

    @FXML
    private void updateButtonAccess(){
        //Button is disabled if there are no selected audio files OR no number has been selected
        creationButton.setDisable(_selectedFiles.isEmpty() || numberSelection.getValue() == null);
    }

    @FXML
    private void handleCreateCreation(){
        System.out.println("make the creation");
    }

    private static class SelectableFile {
        private final StringProperty _name = new SimpleStringProperty();
        private final BooleanProperty _selected = new SimpleBooleanProperty();

        public SelectableFile(String name, boolean selected) {
            _name.set(name);
            _selected.set(selected);
        }

        public final BooleanProperty selectedProperty() {
            return _selected;
        }

        public final boolean isSelected() {
            return selectedProperty().get();
        }

        @Override
        public String toString() {
            return _name.get();
        }

    }

    /**
     * Adds numbers 1 to 10 to the combobox
     */
    private void populateImageNumberSelectionBox(){
        for(int i = 1; i <= 10; i++){
            numberSelection.getItems().add(i);
        }
    }
}
