package wikispeak;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;

public class CombineAudioScreenController extends ListController {

    @FXML private BorderPane rootBorderPane;
    @FXML private VBox audioListBox;

    public void initialize() {
        List<String> audioFileList = populateList("audio", ".wav");

        ListView<SelectableFile> listView = new ListView<>();

        for (String fileName : audioFileList) {
            SelectableFile selectableFile = new SelectableFile(fileName, false);
            listView.getItems().add(selectableFile);
        }

        listView.setCellFactory(CheckBoxListCell.forListView(new Callback<SelectableFile, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(SelectableFile selectableFile) {
                return selectableFile.selectedProperty();
            }
        }));

        audioListBox.getChildren().add(listView);
    }

    @FXML
    private void handleBackToCreateAudio() throws IOException {
        switchScenes(rootBorderPane, "CreateAudioScreen.fxml");
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
}
