package wikispeak;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import wikispeak.helpers.Command;
import wikispeak.helpers.FlickreImageCreator;
import wikispeak.tasks.createCreationJob;
import wikispeak.tasks.downloadImageJob;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageSelectionController extends Controller{
 @FXML private TilePane tilePane;
 @FXML private TilePane selectedImages;
 @FXML private BorderPane rootBorderPane;
 @FXML private Label imageCountLabel;
 @FXML private Button nextButton;
 @FXML private ScrollPane paneOne;
 @FXML private ScrollPane paneTwo;
 @FXML private ProgressBar imageWaitBar;
 @FXML private Label waitLabel;


 private String searchTerm;
 private ExecutorService team = Executors.newSingleThreadExecutor();

 private static final double ELEMENT_SIZE = 145;
 private static final double ELEMENT_SIZE_TWO = 235;
 private static final double NO_GAP = ELEMENT_SIZE_TWO/10;

 ArrayList<File> filesJpg = new ArrayList<>();
 File allImages[];
 ArrayList<File> addedImages = new ArrayList<>();

    @FXML
    public void initialize() {

        nextButton.setDisable(true);
        waitLabel.setVisible(true);

        //should change so back button isn't the same as main menu button
        deleteImagesToUse();
        deleteDownloads();

        filesJpg = new ArrayList<>();
        paneOne.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        paneOne.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        paneTwo.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        paneTwo.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        filesJpg = new ArrayList<>();
        addedImages = new ArrayList<>();
        Command command = new Command("cat .temp_searchterm.txt");
        command.execute();
        searchTerm = command.getStream();

        FlickreImageCreator getImages = new FlickreImageCreator(searchTerm,  10);
        downloadImageJob please = new downloadImageJob(searchTerm, 10, getImages);
        imageWaitBar.progressProperty().bind(please.progressProperty());
        team.execute(please);
        please.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                waitLabel.setVisible(false);
                tilePane.setHgap(NO_GAP);
                tilePane.setVgap(NO_GAP);

                selectedImages.setHgap(NO_GAP);
                selectedImages.setVgap(NO_GAP);

                File selectedDirectory = new File("downloads");

                if (selectedDirectory != null) {
                    FilenameFilter filterJpg = new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.toLowerCase().endsWith(".jpg");
                        }
                    };
                    allImages = selectedDirectory.listFiles(filterJpg);
                    for(int i=0; i< allImages.length; i++){
                        filesJpg.add(allImages[i]);
                    }
                }
                //now set image in tiles
                createAllImageSelectionPage();
            }
        });

    }

    private void createAllImageSelectionPage() {
        tilePane.getChildren().clear();
        for(int i=0; i<filesJpg.size(); i++){
            tilePane.getChildren().add(createElements(i));
        }
    }

    private VBox createElements(int index) {

        ImageView imageView = new ImageView();

        File file = filesJpg.get(index);
        Image image = new Image(file.toURI().toString());
        imageView.setImage(image);

        imageView.setFitWidth(ELEMENT_SIZE);
        imageView.setFitHeight(ELEMENT_SIZE);

        imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                boolean found = false;
                for(int i = 0; i<addedImages.size(); i++){
                    if(addedImages.get(i).equals(file)){
                        filesJpg.add(0,addedImages.get(i));
                        addedImages.remove(addedImages.get(i));
                        tilePane.getChildren().add(imageView);
                        createAllImageSelectionPage();
                        found = true;
                        imageCountLabel.setText(addedImages.size()+" Images selected for the creation");
                    }
                }
                if(!found) {
                    for (int i = 0; i < filesJpg.size(); i++) {
                        if (filesJpg.get(i).equals(file)) {
                            imageView.setFitWidth(ELEMENT_SIZE_TWO);
                            imageView.setFitHeight(ELEMENT_SIZE_TWO);
                            addedImages.add(file);
                            filesJpg.remove(file);
                            selectedImages.getChildren().add(imageView);
                            createAllImageSelectionPage();
                            imageCountLabel.setText(addedImages.size()+" Images selected for the creation");
                        }
                    }
                }
                if(addedImages.size()>0){
                    nextButton.setDisable(false);
                }else{
                    nextButton.setDisable(true);
                }
            }
        });

        imageView.setSmooth(true);
        imageView.setCache(true);

        VBox pageBox = new VBox();
        pageBox.getChildren().add(imageView);

        return pageBox;
    }

    @FXML
    public void handleCreate() throws IOException {
        createCreationJob work = new createCreationJob(searchTerm, addedImages);
        team.submit(work);
        work.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                try {
                    switchScenes(rootBorderPane, "FinalPreviewScreen.fxml");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private void deleteDownloads(){
        File dirForDownloads =new File("downloads");
        File contentsInDownloads[] = dirForDownloads.listFiles();
        for(File image : contentsInDownloads){
            image.delete();
        }
    }

    private void deleteImagesToUse(){
        File dirForImages = new File("images_to_use");
        File contentsInImagesToUse[] = dirForImages.listFiles();
        for(File image : contentsInImagesToUse){
            image.delete();
        }
    }

    @FXML
    public void handleMainMenuButton() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to go to main menu?");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            //only switch scene after confirmation
            deleteDownloads();
            deleteImagesToUse();
            switchScenes(rootBorderPane, "MainMenu.fxml");
        }

    }

    @FXML
    public void habdleBackButton() throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to go back?");
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            //only switch scene after confirmation
            deleteDownloads();
            deleteImagesToUse();
            switchScenes(rootBorderPane, "CreateAudioScreen.fxml");
        }

    }

}
