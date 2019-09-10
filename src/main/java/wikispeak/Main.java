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
    public void start(Stage primaryStage) {
        new File("creations").mkdir();
        new File("audio").mkdir();
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
        file = new File(".temp_audio.wav");
        file.delete();
        file = new File(".temp_video.mp4");
        file.delete();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
