package wikispeak.tasks;

import javafx.concurrent.Task;
import wikispeak.helpers.Command;
import wikispeak.helpers.FlickreImageCreator;

import java.io.File;

/**
 * The task that creations a Creation
 * Periodically updates its progress status for the ProgressBar
 */
public class creationJob extends Task<Void> {

    private String _searchTerm;
    private int _numberOfImages;
    private String _audioFileList;
    private String _creationName;

    public creationJob(String searchTerm, int numberOfImages, String audioFileList, String creationName){
        _searchTerm = searchTerm;
        _numberOfImages = numberOfImages;
        _audioFileList = audioFileList;
        _creationName = creationName;
    }

    @Override
    protected Void call() throws Exception {

        updateProgress(0, 7);

        //delete any existing images
        new File("downloads").mkdir();
        for (File file : new File("downloads").listFiles()){
            file.delete();
        }

        updateProgress(1, 7);

        //download the images
        FlickreImageCreator imageCreator = new FlickreImageCreator(_searchTerm, _numberOfImages);
        imageCreator.start();

        updateProgress(3, 7);

        //merge the selected audiofiles
        File file = new File(".combined.wav"); //delete the file if it already exists
        file.delete();
        Command command = new Command("sox " + _audioFileList + " .temp_audio.wav");
        command.execute();

        updateProgress(4, 7);

        //calculate duration for each image in slideshow, given audio duration and number of imeages
        command = new Command("soxi -D .temp_audio.wav");
        command.execute();
        double audioDuration = Double.parseDouble(command.getStream());
        Double framerate = (_numberOfImages/audioDuration);

        updateProgress(5, 7);

        //make the video
        command = new Command("cat downloads" + System.getProperty("file.separator") + "*.jpg | ffmpeg -f image2pipe -framerate " + framerate + " -i - -vf \"scale=414:312, drawtext=fontfile=fonts/myfont.ttf:fontsize=50: fontcolor=black:x=(w-text_w)/2:y=(h-text_h)/2:text=" + _searchTerm + "\" -r 25 -y .temp_video.mp4");
        command.execute();

        updateProgress(6, 7);

        //make the creation: combine audio and video
        command = new Command("ffmpeg -y -i .temp_audio.wav -i .temp_video.mp4 -c:v copy -c:a aac -strict experimental creations" + System.getProperty("file.separator") + _creationName + ".mp4");
        command.execute();

        updateProgress(7, 7);

        return null;
    }
}
