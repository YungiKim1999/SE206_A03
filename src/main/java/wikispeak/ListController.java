package wikispeak;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract ListController class
 * Provides listing functionality for items inside a given directory
 */
public abstract class ListController extends Controller {

    /**
     * Lists the creations in the specified directory (with the specified extension removed)
     * @return alphabetically sorted list of filenames
     */
    public List<String> populateList(String directory, String extension) {

        List<String> results = new ArrayList<String>();
        if (!extension.equals("")) {
            //in case directory has been removed
            new File(directory).mkdir();
            //find all files in given directory with specified extension
            File[] files = new File(directory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(extension);
                }
            });

            for (File file : files) {
                if (file.isFile()) {
                    String name = file.getName();
                    //remove the extension from the filename
                    int extensionIndex = name.lastIndexOf(".");
                    if (extensionIndex == -1) {
                        results.add(name);
                    } else {
                        results.add(name.substring(0, extensionIndex));
                    }
                }
            }
            Collections.sort(results, String.CASE_INSENSITIVE_ORDER);
            return results;
        }else{
            new File(directory).mkdir();
            File[] files = new File(directory).listFiles();
            for (File file : files) {
                String name = file.getName();
                results.add(name);
            }
            return  results;
            }
        }
    }

