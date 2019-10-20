package wikispeak.tasks;

import javafx.concurrent.Task;
import wikispeak.helpers.Command;

import java.io.File;

public class removeMusicJob extends Task<Void> {
    private boolean canCall = false;
    public removeMusicJob(){
        canCall = true;
    }
    @Override
    protected Void call() throws Exception {
       if(canCall) {
           File finalCreation = new File(".temp" + System.getProperty("file.separator") + "final_creation.mp4");
           finalCreation.delete();
           Command command = new Command("ffmpeg -y -i .temp" + System.getProperty("file.separator") + "temp_audio.wav -i .temp" + System.getProperty("file.separator") + "temp_video.mp4 -c:v copy -c:a aac -strict experimental .temp" + System.getProperty("file.separator") + "final_creation.mp4");
           command.execute();
       }
        return null;
    }
}
