package wikispeak.tasks;

import javafx.concurrent.Task;
import wikispeak.helpers.Command;

import java.io.File;
import java.util.List;

/**
 * The task that creations a Creation
 * Periodically updates its progress status for the ProgressBar
 */
public class createFullAudioJob extends Task<Void> {

    private List<String> _audioFileList;

    public createFullAudioJob(List<String> audioFileList){
        _audioFileList = audioFileList;
    }

    @Override
    protected Void call() throws Exception {

        String audioFileString = "";
        for(String file : _audioFileList){
            audioFileString = audioFileString + " .temp" + System.getProperty("file.separator") + "audio" + System.getProperty("file.separator") + file + ".wav";
        }

        //merge the selected audiofiles
        File file = new File(".temp" + System.getProperty("file.separator") + "temp_audio.wav"); //delete the file if it already exists
        file.delete();
        Command command = new Command("sox " + audioFileString + " .temp" + System.getProperty("file.separator") + "temp_audio.wav");
        command.execute();

        return null;
    }
}
