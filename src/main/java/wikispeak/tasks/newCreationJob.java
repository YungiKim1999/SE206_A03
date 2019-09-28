package wikispeak.tasks;

import javafx.concurrent.Task;
import wikispeak.helpers.Command;

import java.io.File;

/**
 * The task that creations a Creation
 * Periodically updates its progress status for the ProgressBar
 */
public class newCreationJob extends Task<Void> {

    private String _searchTerm;
    private String _creationName;
    private String _imagesToKeep;

    public newCreationJob(String searchTerm, String imagesToKeep, String creationName){
        _searchTerm = searchTerm;
        _imagesToKeep = imagesToKeep;
        _creationName = creationName;
    }

    @Override
    protected Void call() throws Exception {
        //delete all unselected files

        //get number of images
        File file = new File("downloads");
        int _numberOfImages = file.listFiles().length;
        //calculate duration for each image in slideshow, given audio duration
        Command command = new Command("soxi -D .temp_audio.wav");
        command.execute();
        double duration = Double.parseDouble(command.getStream());
        Double framerate = (_numberOfImages/duration);

        //make the video
        //TODO: check the output resolution is the same as the resolution of the video player
        command = new Command("ffmpeg -framerate " + framerate + " -pattern_type glob -i 'downloads/*.jpg' -vf \"scale=414:312, drawtext=fontfile=fonts/myfont.ttf:fontsize=100: fontcolor=black:x=(w-text_w)/2:y=(h-text_h)/2:text=" + _searchTerm + "\" .temp_video.mp4");
        command.execute();

        command = new Command("ffmpeg -y -i .temp_audio.wav -i .temp_video.mp4 -c:v copy -c:a aac -strict experimental final_creation.mp4");
        command.execute();

        return null;
    }
}
