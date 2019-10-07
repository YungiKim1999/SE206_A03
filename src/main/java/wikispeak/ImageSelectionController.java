package wikispeak;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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

 private String searchTerm;
 private ExecutorService team = Executors.newSingleThreadExecutor();
 private String creationName;
 private boolean firstTime=true;

 private static final double ELEMENT_SIZE = 100;
 private static final double GAP = ELEMENT_SIZE/10;

 ArrayList<File> filesJpg = new ArrayList<>();
 File allImages[];
 ArrayList<File> addedImages = new ArrayList<>();

    @FXML
    public void initialize() {
        if(!firstTime){
            getImageButton.setText("get more images...");
        }
        filesJpg = new ArrayList<>();
        addedImages = new ArrayList<>();
        Command command = new Command("cat .temp_searchterm.txt");
        command.execute();
        searchTerm = command.getStream();

        command = new Command("cat .temp_creationName.txt");
        command.execute();
        creationName = command.getStream();

        tilePane.setHgap(GAP);
        tilePane.setVgap(GAP);

        selectedImages.setHgap(GAP);
        selectedImages.setVgap(GAP);

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
        createAllImageSelectionPageFirst();
    }

    private void createAllImageSelectionPageFirst() {
        tilePane.getChildren().clear();
        for(int i=0; i<filesJpg.size(); i++){
            tilePane.getChildren().add(createElementsFirst(i));
        }
    }

    private void createAllImageSelectionPageSecond(){
        tilePane.getChildren().clear();
        for(int i=0; i<filesJpg.size(); i++){
            tilePane.getChildren().add(createElementsSecond(i));
        }
    }

    private VBox createElementsSecond(int index){
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
                        filesJpg.add(addedImages.get(i));
                        addedImages.remove(addedImages.get(i));
                        tilePane.getChildren().add(imageView);
                        System.out.println("added");
                        createAllImageSelectionPageSecond();
                        found = true;

                    }
                }
                if(!found) {
                    for (int i = 0; i < filesJpg.size(); i++) {
                        if (filesJpg.get(i).equals(file)) {
                            addedImages.add(file);
                            filesJpg.remove(file);
                            selectedImages.getChildren().add(imageView);
                        }
                    }
                }
                found = false;
            }
        });

        imageView.setSmooth(true);
        imageView.setCache(true);

        VBox pageBox = new VBox();
        pageBox.getChildren().add(imageView);

        return pageBox;
    }

    private VBox createElementsFirst(int index) {

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
                        filesJpg.add(addedImages.get(i));
                        addedImages.remove(addedImages.get(i));
                        tilePane.getChildren().add(imageView);
                        System.out.println("added");
                        createAllImageSelectionPageSecond();
                        found = true;

                    }
                }
                if(!found) {
                    for (int i = 0; i < filesJpg.size(); i++) {
                        if (filesJpg.get(i).equals(file)) {
                            addedImages.add(file);
                            filesJpg.remove(file);
                            selectedImages.getChildren().add(imageView);
                        }
                    }
                }
                found = false;
            }
        });

        imageView.setSmooth(true);
        imageView.setCache(true);

        VBox pageBox = new VBox();
        pageBox.getChildren().add(imageView);

        return pageBox;
    }

    @FXML
    public void handleCreate(){
        newCreationJob work = new newCreationJob(searchTerm, addedImages);
        team.submit(work);
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
