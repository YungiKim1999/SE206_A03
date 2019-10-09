package wikispeak.helpers;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *This class packages the Flickr api so that it is easy to use.
 */
public class FlickreImageCreator {

    String searchedTerm;
    int numOfImages;
    Flickr flickrObject;
    //private ExecutorService workerTeam = Executors.newSingleThreadExecutor();

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
     * In order to use this method properly, the keys api key as well as the shared secret key should be in the
     * "flickr-api-keys.txt"
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
    public void start(){
        try {

            String query = searchedTerm;
            int resultsPerPage = numOfImages;
            int page = 0;

            PhotosInterface photos = flickrObject.getPhotosInterface();
            SearchParameters params = new SearchParameters();
            params.setSort(SearchParameters.RELEVANCE);
            params.setMedia("photos");
            params.setText(query);

            PhotoList<Photo> results = photos.search(params, resultsPerPage, page);

            for (Photo photo: results) {
                try {
                    BufferedImage image = photos.getImage(photo, Size.LARGE);
                    String filename = query.trim().replace(' ', '-')+"-"+System.currentTimeMillis()+"-"+photo.getId()+".jpg";
                    //you can change the name of the file that the photos are to be outputted by changing "downloads" to whatever~
                    new File("downloads").mkdir();
                    File outputfile = new File("downloads",filename);

                    ImageIO.write(image, "jpg", outputfile);
                } catch (FlickrException fe) {
                    System.err.println("Ignoring image " +photo.getId() +": "+ fe.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Flickr getFlickrObject() {
        return flickrObject;
    }

}
