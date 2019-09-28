package wikispeak.helpers;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.REST;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import wikispeak.tasks.downloadImageJob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * How to use this thing:
 *
 * you create an object of "FlickreImageCreator" with the searched term you want images of(as a string) and the number of
 * images that you want.
 *
 * then, you should "start();" the object.
 *
 * note that the download of images is done concurrently with whatever(i assume) is being done. If this is not wanted,
 * then i can make changes to it.
 * I have commented out the lines of code that causes concurrency just in case.
 *
 */
public class FlickreImageCreator {

    String searchedTerm;
    int numOfImages;
    Flickr flickrObject;
    private ExecutorService workerTeam = Executors.newSingleThreadExecutor();

    public FlickreImageCreator(String searchedTerm, int numOfImages){
        this.numOfImages=numOfImages;
        this.searchedTerm=searchedTerm;
        try {
            String apiKey = getAPIKey("apiKey");
            String sharedSecret = getAPIKey("sharedSecret");
            flickrObject = new Flickr(apiKey, sharedSecret, new REST());
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * in order for this function to work, the there needs to be a text file called "flickr-api-keys.txt"
     * its included here so there should be no worries
     * if not included, message me and ill give it to you with the codes
     *
     *
     * @param key
     * @return
     * @throws Exception
     */
    private String getAPIKey(String key) throws Exception {

        String config = System.getProperty("user.dir")
                + System.getProperty("file.separator")+ "flickr-api-keys.txt";

        File file = new File(config);
        BufferedReader br = new BufferedReader(new FileReader(file));

        String line;
        while ( (line = br.readLine()) != null ) {
            if (line.trim().startsWith(key)) {
                br.close();
                return line.substring(line.indexOf("=")+1).trim();
            }
        }
        br.close();
        throw new RuntimeException("Couldn't find " + key +" in config file "+file.getName());
    }
    //

    //slow part
    public boolean start() {
        downloadImageJob getImages = new downloadImageJob(searchedTerm, numOfImages, flickrObject);
        workerTeam.execute(getImages);
        getImages.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {

            }
        });

        return true;

    }
}

