package wikispeak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main extends Application {

    @Override
    //TODO: these mkdir() should be added JUST before the folder is used in case the user deletes them halfway through
    //TODO: research "you can't delete this because it is in use" protection to stop the user deleting stuff
    public void start(Stage primaryStage) {
        new File(".temp").mkdir();
        new File(".temp" + System.getProperty("file.separator") + "audio").mkdir();
        new File(".temp" + System.getProperty("file.separator") + "downloads").mkdir();
        new File(".temp" + System.getProperty("file.separator") + "images_to_use").mkdir();

        new File("creations").mkdir();

        try{
            //Load in and display the Home screen (Main Menu)
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.getClass().getResource("MainMenu.fxml"));
            Parent layout = loader.load();
            Scene scene = new Scene(layout);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);

            primaryStage.show();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Shutdown clean-up, deletes the entire temp file structure
     */
    @Override
    public void stop() throws IOException {
        Path directory = Paths.get(".temp");
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void main(String[] args) throws IOException {
        launch(args);
    }

}
