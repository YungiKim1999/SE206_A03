package wikispeak;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import wikispeak.helpers.Command;
import wikispeak.helpers.FlickreImageCreator;
import wikispeak.tasks.finalJob;
import wikispeak.tasks.newCreationJob;

import java.io.File;
import java.io.FilenameFilter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageSelectionController extends Controller{
 @FXML private TilePane tilePane;
 @FXML private TilePane selectedImages;
 @FXML private Button createButton;
 private String imageNames = "";
 private String searchTerm;
 private ExecutorService team = Executors.newSingleThreadExecutor();
 private String creationName;

 int count = 0;

 private static final double ELEMENT_SIZE = 100;
 private static final double GAP = ELEMENT_SIZE/10;

 File filesJpg[];

    @FXML
    public void initialize() {
        Command command = new Command("cat .temp_searchterm.txt");
        command.execute();
        searchTerm = command.getStream();

        command = new Command("cat .temp_creationName.txt");
        command.execute();
        creationName = command.getStream();
        System.out.println(creationName);

        FlickreImageCreator getImages = new FlickreImageCreator(searchTerm, 10);
        getImages.start();

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
            filesJpg = selectedDirectory.listFiles(filterJpg);

        }
        //now set image in tiles
        createElements();
    }

    private void createElements() {
        tilePane.getChildren().clear();
        for(int i=0; i<filesJpg.length; i++){
            tilePane.getChildren().add(createPage(count));
            count++;
        }
    }


    private VBox createPage(int index) {

        ImageView imageView = new ImageView();
        File file = filesJpg[index];
        Image image = new Image(file.toURI().toString());
        imageView.setImage(image);
        imageView.setFitWidth(ELEMENT_SIZE);
        imageView.setFitHeight(ELEMENT_SIZE);
        imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                selectedImages.getChildren().add(imageView);
                imageView.removeEventHandler(MouseEvent.MOUSE_CLICKED, this);
                imageNames = imageNames + filesJpg[index].toString().replaceAll("downloads/", "") + " ";
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
        newCreationJob creationJob = new newCreationJob(searchTerm, imageNames, creationName);
        creationJob.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                finalJob pleaseWork = new finalJob(creationName);
                team.execute(pleaseWork);
            }
        });
        team.execute(creationJob);

    }
}
