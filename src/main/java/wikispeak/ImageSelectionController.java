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
import wikispeak.tasks.moveUsedImages;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageSelectionController extends Controller{
    @FXML private TilePane SelectionOfImagesPane;
    @FXML private TilePane selectedImages;
    @FXML private BorderPane rootBorderPane;
    @FXML private Label imageCountLabel;
    @FXML private Button nextButton;
    @FXML private ScrollPane paneOne;
    @FXML private ScrollPane paneTwo;
    @FXML private ProgressBar imageWaitBar;
    @FXML private ProgressBar createVideoBar;
    @FXML private Label waitLabel;


    private String searchTerm;
    private ExecutorService team = Executors.newSingleThreadExecutor();

    private static final double ELEMENT_SIZE_TWO = 235;
    private static final double NO_GAP = ELEMENT_SIZE_TWO/10;

    ArrayList<File> filesJpg = new ArrayList<>();
    ArrayList<File> addedImages = new ArrayList<>();
    File allUnselectedImages[];
    File allSelectedImage[];

    @FXML
    public void initialize() {

        File unselectedDirectory = new File("downloads");
        File selectedDirectory = new File("images_to_use");
        if(addedImages.size()>0){
            nextButton.setDisable(false);
        }else{
            nextButton.setDisable(true);
        }
        waitLabel.setVisible(true);

        paneOne.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        paneOne.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        paneTwo.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        paneTwo.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        Command command = new Command("cat .temp_searchterm.txt");
        command.execute();

        SelectionOfImagesPane.setHgap(NO_GAP);
        SelectionOfImagesPane.setVgap(NO_GAP);

        selectedImages.setHgap(NO_GAP);
        selectedImages.setVgap(NO_GAP);

        FilenameFilter filterJpg = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jpg");
            }
        };

        searchTerm = command.getStream();

        allUnselectedImages = unselectedDirectory.listFiles(filterJpg);
        allSelectedImage = selectedDirectory.listFiles(filterJpg);
        for(int i=0; i< allUnselectedImages.length; i++){
            filesJpg.add(allUnselectedImages[i]);
        }
        for(int i=0; i< allSelectedImage.length; i++){
            addedImages.add(allSelectedImage[i]);
        }

        if(filesJpg.size()==0 && addedImages.size()==0) {
            FlickreImageCreator getImages = new FlickreImageCreator(searchTerm,  10);
            downloadImageJob downloadImages = new downloadImageJob(searchTerm, 10, getImages);
            imageWaitBar.progressProperty().bind(downloadImages.progressProperty());
            team.execute(downloadImages);

            downloadImages.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    imageWaitBar.setVisible(false);
                    waitLabel.setVisible(false);

                    allUnselectedImages = unselectedDirectory.listFiles(filterJpg);
                    allSelectedImage = selectedDirectory.listFiles(filterJpg);
                    for(int i=0; i< allUnselectedImages.length; i++){
                        filesJpg.add(allUnselectedImages[i]);
                    }
                    for(int i=0; i< allSelectedImage.length; i++){
                        addedImages.add(allSelectedImage[i]);
                    }
                    createAllImageSelectionPage(SelectionOfImagesPane, filesJpg);
                    createAllImageSelectionPage(selectedImages, addedImages);
                }
            });
        }else{
            imageWaitBar.setVisible(false);
            waitLabel.setVisible(false);

        }

        //have all the arraylists ready, set images into tilePane using it
        createAllImageSelectionPage(SelectionOfImagesPane, filesJpg);
        createAllImageSelectionPage(selectedImages, addedImages);
    }

    private void createAllImageSelectionPage(TilePane givenPane, ArrayList<File> filesGiven ) {
        givenPane.getChildren().clear();
        for(int i=0; i<filesGiven.size(); i++){
            givenPane.getChildren().add(createElements(i,filesGiven));
        }
    }

    private VBox createElements(int index, ArrayList<File> filesGiven) {

        ImageView imageView = new ImageView();

        File file = filesGiven.get(index);
        Image image = new Image(file.toURI().toString());
        imageView.setImage(image);

        imageView.setFitWidth(ELEMENT_SIZE_TWO);
        imageView.setFitHeight(ELEMENT_SIZE_TWO);

        imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                boolean found = false;
                for(int i = 0; i<addedImages.size(); i++){
                    if(addedImages.get(i).equals(file)){
                        filesJpg.add(0,addedImages.get(i));
                        addedImages.remove(addedImages.get(i));
                        createAllImageSelectionPage(SelectionOfImagesPane, filesJpg);
                        createAllImageSelectionPage(selectedImages, addedImages);
                        found = true;
                        imageCountLabel.setText(addedImages.size()+" Images selected for the creation");
                    }
                }
                if(!found) {
                    for (int i = 0; i < filesJpg.size(); i++) {
                        if (filesJpg.get(i).equals(file)) {
                            addedImages.add(file);
                            filesJpg.remove(file);
                            createAllImageSelectionPage(SelectionOfImagesPane, filesJpg);
                            createAllImageSelectionPage(selectedImages, addedImages);
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
        createVideoBar.progressProperty().bind(work.progressProperty());
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
        moveUsedImages moveImages = new moveUsedImages(addedImages);
        team.submit(moveImages);
        moveImages.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                try {
                    switchScenes(rootBorderPane, "CreateAudioScreen.fxml");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
