package wikispeak;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import wikispeak.helpers.Command;
import wikispeak.helpers.FlickreImageCreator;
import wikispeak.tasks.downloadImageJob;
import wikispeak.tasks.newCreationJob;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageSelectionController extends Controller{
 @FXML private TilePane tilePane;
 @FXML private TilePane selectedImages;
 @FXML private Button getImageButton;
 @FXML private BorderPane rootBorderPane;
 @FXML private Label imageCountLabel;
 @FXML private Button nextButton;
 @FXML private ScrollPane paneOne;
 @FXML private ScrollPane paneTwo;

 private String searchTerm;
 private ExecutorService team = Executors.newSingleThreadExecutor();
 private String creationName;
 private boolean firstTime=true;

 private static final double ELEMENT_SIZE = 145;
 private static final double ELEMENT_SIZE_TWO = 235;
 private static final double NO_GAP = ELEMENT_SIZE_TWO/10;

 ArrayList<File> filesJpg = new ArrayList<>();
 File allImages[];
 ArrayList<File> addedImages = new ArrayList<>();

    @FXML
    public void initialize() {
        nextButton.setDisable(true);
        if(!firstTime){
            getImageButton.setText("get more images...");
        }
        paneOne.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        paneOne.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        paneTwo.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        paneTwo.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        filesJpg = new ArrayList<>();
        addedImages = new ArrayList<>();
        Command command = new Command("cat .temp_searchterm.txt");
        command.execute();
        searchTerm = command.getStream();

        command = new Command("cat .temp_creationName.txt");
        command.execute();
        creationName = command.getStream();

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
                found = false;
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
        newCreationJob work = new newCreationJob(searchTerm, addedImages);
        team.submit(work);

        switchScenes(rootBorderPane, "FinalPreviewScreen.fxml");
    }

    @FXML
    public void handleMainMenuButton() throws IOException {
        switchScenes(rootBorderPane, "MainMenu.fxml");
    }
    @FXML
    public void habdleBackButton() throws IOException {
        switchScenes(rootBorderPane, "CombineAudioScreen.fxml");
    }

    @FXML
    public void handleGetImageButton(){
        firstTime = false;
        FlickreImageCreator getImages = new FlickreImageCreator(searchTerm, allImages.length + 10);
        downloadImageJob please = new downloadImageJob(searchTerm, allImages.length + 10, getImages);
        team.execute(please);
        please.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                initialize();
            }
        });
    }
}
