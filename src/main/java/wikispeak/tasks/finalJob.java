package wikispeak.tasks;

import javafx.concurrent.Task;
import wikispeak.helpers.Command;

/**
 * The task that creations a Creation
 * Periodically updates its progress status for the ProgressBar
 */
public class finalJob extends Task<Void> {

    private String _creationName;

    public finalJob(String creationName){
        _creationName = creationName;
        System.out.println(_creationName);
    }

    @Override
    protected Void call() throws Exception {
        //make the creation: combine audio and video

        Command command = new Command("ffmpeg -y -i .temp_audio.wav -i .temp_video.mp4 -c:v copy -c:a aac -strict experimental creations" + System.getProperty("file.separator") + _creationName + ".mp4");
        command.execute();
        return null;
    }
}
