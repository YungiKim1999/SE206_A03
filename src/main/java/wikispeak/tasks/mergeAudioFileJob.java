package wikispeak.tasks;

import javafx.concurrent.Task;
import wikispeak.helpers.Command;

import java.io.File;

public class mergeAudioFileJob extends Task<Boolean> {
    private String _audioFileList;

    public mergeAudioFileJob(String audioFileList){
        _audioFileList = audioFileList;
    }


    @Override
    protected Boolean call() throws Exception {
        File file = new File(".combined.wav"); //delete the file if it already exists
        file.delete();
        Command command = new Command("sox " + _audioFileList + " .temp_audio.wav");
        command.execute();
        return true;
    }
}
