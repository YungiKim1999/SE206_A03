package wikispeak.tasks;

import javafx.concurrent.Task;
import wikispeak.helpers.Command;

import java.io.File;
import java.util.ArrayList;

/**
 * The task that creations a Creation
 * Periodically updates its progress status for the ProgressBar
 */
public class createCreationJob extends Task<Void> {

    private  String blankText = "";
    private String _searchTerm;
    private ArrayList<File> images;

    public createCreationJob(String searchTerm, ArrayList<File> images){
        _searchTerm = searchTerm;
        this.images = images;
    }

    @Override
    protected Void call() throws Exception {

        //TODO: Fix output resolution of all videos to match player

        //get number of images
        File file = new File(".temp" + System.getProperty("file.separator") + "images_to_use");
        int _numberOfImages = file.listFiles().length;

        //calculate duration for each image in slideshow, given audio duration
        Command command = new Command("soxi -D .temp" + System.getProperty("file.separator") + "temp_audio.wav");
        command.execute();
        updateProgress(3,10);

        double duration = Double.parseDouble(command.getStream());
        Double framerate = (_numberOfImages/duration);

        updateProgress(5,10);

        //Make final, full creation
        command = new Command("cat .temp" + System.getProperty("file.separator") + "images_to_use" + System.getProperty("file.separator") + "*.jpg | ffmpeg -f image2pipe -framerate " + framerate + " -i - -vf \"scale=710:504, drawtext=fontsize=50:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:shadowcolor=black:shadowx=2:shadowy=2:text=" + _searchTerm + "\" -r 25 -y .temp" + System.getProperty("file.separator") + "temp_video.mp4");
        command.execute();
        command = new Command("ffmpeg -y -i .temp" + System.getProperty("file.separator") + "temp_audio.wav -i .temp" + System.getProperty("file.separator") + "temp_video.mp4 -c:v copy -c:a aac -strict experimental .temp" + System.getProperty("file.separator") + "final_creation.mp4");
        command.execute();

        updateProgress(6,10);

        //Make video with only audio,
        // TODO: does this duration need to be fixed??
        command = new Command("ffmpeg -f lavfi -i color=c=gray:s=900x400:d=5 -vf \"drawtext=fontsize=50: fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:shadowcolor=black:shadowx=2:shadowy=2:text='Enter the English Word'\" .temp" + System.getProperty("file.separator") + "blankVideo.mp4");
        command.execute();
        command = new Command("ffmpeg -y -i .temp" + System.getProperty("file.separator") + "temp_audio.wav -i .temp" + System.getProperty("file.separator") + "blankVideo.mp4 -c:v copy -c:a aac -strict experimental .temp" + System.getProperty("file.separator") + "quiz1.mp4");
        command.execute();

        updateProgress(7,10);

        //Make video with only pictures
        //change the scale or s= numbers in order to change the scale of the video
        command = new Command("cat .temp" + System.getProperty("file.separator") + "images_to_use" + System.getProperty("file.separator") + "*.jpg | ffmpeg -f image2pipe -framerate " + framerate + " -i - -vf \"scale=900:400, drawtext=fontsize=50:fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:shadowcolor=black:shadowx=2:shadowy=2:text='Enter the English Word'\" -r 25 -y .temp" + System.getProperty("file.separator") + "noTextVideo.mp4");
        command.execute();
        command = new Command("ffmpeg -y -i .temp" + System.getProperty("file.separator") + "temp_audio.wav -i .temp" + System.getProperty("file.separator") + "noTextVideo.mp4 -c:v copy -c:a aac -strict experimental .temp" + System.getProperty("file.separator") + "quiz2.mp4");
        command.execute();

        updateProgress(10,10);

        return null;
    }
}
