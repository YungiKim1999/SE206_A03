package wikispeak;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract ListController class
 * Provides creation listing functionality
 */
public abstract class ListController extends Controller {

    /**
     * Lists the creations in the creations directory (.mp4 extension removed)
     * @return alphabetically sorted list of creations
     */
    public List<String> populateList(){

        List<String> results = new ArrayList<String>();

        //in case creations directory has been deleted
        new File("creations").mkdir();
        //find all files in 'creations' directory with .mp4 extension
        File[] files = new File("creations").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp4");
            }
        });

        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();
                //remove the .mp4 extension from the filename
                int extensionIndex = name.lastIndexOf(".");
                if (extensionIndex == -1){
                    results.add(name);
                }
                else{
                    results.add(name.substring(0, extensionIndex));
                }
            }
        }
        Collections.sort(results, String.CASE_INSENSITIVE_ORDER);
        return results;
    }

}
