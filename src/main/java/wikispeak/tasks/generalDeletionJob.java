package wikispeak.tasks;

import javafx.concurrent.Task;

import java.io.File;

/**
 * The task that deletes a specified file
 */
public class generalDeletionJob extends Task<Boolean> {

    private String containingFolder;
    private String fileName;
    private String extension;


    /**
     * Deletes a specified file within a specified directory with a specified extension
     * @param containingFolder enter empty string "" if target file is in project root
     * @param fileName
     * @param extension enter empty string "" if file has no extension
     */
    public generalDeletionJob(String containingFolder, String fileName, String extension) {
        this.containingFolder = containingFolder;
        this.fileName = fileName;
        this.extension = extension;
    }

    @Override
    protected Boolean call() throws Exception {
        File file = new File(containingFolder + System.getProperty("file.separator") + fileName + extension);
        file.delete();
        return true;
    }
}
