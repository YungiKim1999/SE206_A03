package wikispeak.components;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import wikispeak.tasks.creationDeletionJob;
import wikispeak.tasks.generalDeletionJob;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Custom cell for a ListView.
 * The cell can be clicked and dragged for reordering and features a delete button with icon.
 * Adapted from: https://gist.github.com/jewelsea/7821196
 */
public class DeleteAndMoveCell extends ListCell<String> {

    HBox hbox = new HBox();
    Label label = new Label("");
    Pane pane = new Pane();
    Button button = new Button();

    private ExecutorService workerTeam = Executors.newSingleThreadExecutor();

    /**
     * Adds delete functionality
     * Constructor to use when deleting creations
     */
    public DeleteAndMoveCell(){

        hbox.getChildren().addAll(label, pane, button);
        HBox.setHgrow(pane, Priority.ALWAYS);
        Image deleteIcon = new Image(getClass().getResourceAsStream("redcross.png"));
        button.setGraphic(new ImageView(deleteIcon));
        button.setPadding(new Insets(2,3,2,3));

        button.setOnAction(event -> {
            String itemName = getItem();
            creationDeletionJob deleteSelected = new creationDeletionJob(itemName);
            workerTeam.submit(deleteSelected);
            getListView().getItems().remove(itemName);
        });

        makeReorderable();
    }

    /**
     * Adds delete functionality
     * Consructor to use when deleting audio files
     * @param containingFolder
     * @param extension
     */
    public DeleteAndMoveCell(String containingFolder, String extension){

        hbox.getChildren().addAll(label, pane, button);
        HBox.setHgrow(pane, Priority.ALWAYS);
        Image deleteIcon = new Image(getClass().getResourceAsStream("redcross.png"));
        button.setGraphic(new ImageView(deleteIcon));

        button.setPadding(new Insets(2,3,2,3));

        button.setOnAction(event -> {
            String itemName = getItem();
            generalDeletionJob deleteSelected = new generalDeletionJob(containingFolder, itemName, extension);
            workerTeam.submit(deleteSelected);
            getListView().getItems().remove(itemName);
        });

        makeReorderable();
    }


    /**
     * Adds reordorable functionality.
     */
    private void makeReorderable() {

        ListCell thisCell = this;

        setOnDragDetected(event -> {
            if (getItem() == null) {
                return;
            }

            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(getItem());

            dragboard.setContent(content);

            event.consume();
        });

        setOnDragOver(event -> {
            if (event.getGestureSource() != thisCell &&
                    event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }

            event.consume();
        });

        setOnDragEntered(event -> {
            if (event.getGestureSource() != thisCell &&
                    event.getDragboard().hasString()) {
                setOpacity(0.3);
            }
        });

        setOnDragExited(event -> {
            if (event.getGestureSource() != thisCell &&
                    event.getDragboard().hasString()) {
                setOpacity(1);
            }
        });

        setOnDragDropped(event -> {
            if (getItem() == null) {
                return;
            }

            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString()) {
                ObservableList<String> items = getListView().getItems();
                int draggedIdx = items.indexOf(db.getString());
                int thisIdx = items.indexOf(getItem());


                items.set(draggedIdx, getItem());
                items.set(thisIdx, db.getString());

                List<String> itemscopy = new ArrayList<>(getListView().getItems());
                getListView().getItems().setAll(itemscopy);

                success = true;
            }
            event.setDropCompleted(success);

            event.consume();
        });

        setOnDragDone(DragEvent::consume);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        setText(null);
        setGraphic(null);

        if (item != null && !empty) {
            label.setText(item);
            setGraphic(hbox);
        }

    }
}
