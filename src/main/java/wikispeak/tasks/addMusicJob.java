package wikispeak.tasks;

import javafx.concurrent.Task;
import wikispeak.helpers.Command;

import java.io.File;

public class addMusicJob extends Task<Void> {

    File selectedMusic;

    public addMusicJob(File selectedMusic){
        this.selectedMusic = selectedMusic;
    }


    @Override
    protected Void call() throws Exception {
        File finalCreation = new File(".temp" + System.getProperty("file.separator") + "final_creation.mp4");
        finalCreation.delete();
        Command command = new Command("ffmpeg -y -i .temp" + System.getProperty("file.separator") + "temp_audio.wav -i .temp" + System.getProperty("file.separator") + "temp_video.mp4 -c:v copy -c:a aac -strict experimental .temp" + System.getProperty("file.separator") + "final_creation.mp4");
        command.execute();
        File creationCreated = new File(".temp" + System.getProperty("file.separator") + "final_creation.mp4");
        creationCreated.renameTo(new File(".temp" + System.getProperty("file.separator")+"The_final_creation.mp4"));
        String lineOfCommand = "ffmpeg -i .temp" + System.getProperty("file.separator") + "The_final_creation.mp4 -i music" + System.getProperty("file.separator") + "\'" +selectedMusic.getName() + "\'" +" -filter_complex amix=inputs=2 -shortest .temp" + System.getProperty("file.separator")+ "final_creation.mp4";
        command = new Command(lineOfCommand);
        command.execute();
        return null;
    }
}
