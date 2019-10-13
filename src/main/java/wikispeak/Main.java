package wikispeak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

    @Override
    //TODO: these mkdir() should be added JUST before the folder is used in case the user deletes them halfway through
    //TODO: research "you can't delete this because it is in use" protection to stop the user deleting stuff
    public void start(Stage primaryStage) {
        new File("creations").mkdir();
        new File("audio").mkdir();
        new File("downloads").mkdir();
        new File("images_to_use").mkdir();
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
     * Shutdown clean-up, deletes all temp files
     */
    @Override
    public void stop(){
        File file = new File(".temp_text.txt");
        file.delete();
        file = new File(".temp_video.mp4");
        file.delete();
        file = new File(".temp_audio.wav");
        file.delete();
        file = new File(".temp_searchterm.txt");
        file.delete();
        file = new File(".temp_creationName.txt");
        file.delete();
        file = new File("audio");
        for (File insideFile : file.listFiles()){
            insideFile.delete();
        }
        file.delete();

        file = new File("downloads");
        for (File insideFile : file.listFiles()){
            insideFile.delete();
        }
        file.delete();

        file = new File("images_to_use");
        for (File insideFile : file.listFiles()){
            insideFile.delete();
        }
        file.delete();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
