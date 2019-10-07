package wikispeak.tasks;

import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.*;
import javafx.concurrent.Task;
import wikispeak.helpers.FlickreImageCreator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * this is the "task" that is run by the "FlickrImageCreator".
 *
 * in order for this to work, there needs to be a folder called "downloads" inside of the project
 *
 */
public class downloadImageJob extends Task<Boolean> {

    String searchedTerm;
    int numOfImages;
    FlickreImageCreator flickrObject;

    public downloadImageJob(String searchedTerm, int numOfImages, FlickreImageCreator flickrObject){
        this.searchedTerm = searchedTerm;
        this.flickrObject = flickrObject;
        this.numOfImages = numOfImages;
    }

    @Override
    protected Boolean call() {
        try {

            String query = searchedTerm;
            int resultsPerPage = numOfImages;
            int page = 0;

            PhotosInterface photos = flickrObject.getFlickrObject().getPhotosInterface();
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
        //this true return can be used in order to make a popup saying that the user has completed the video creation~~

        return true;
    }


}
