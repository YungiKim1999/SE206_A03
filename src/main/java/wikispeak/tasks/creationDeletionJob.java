package wikispeak.tasks;

import javafx.concurrent.Task;

import java.io.File;

public class creationDeletionJob extends Task<Boolean> {

    private String fileName;
    private File creationFile = new File("creations");
    private File[] allCreations = creationFile.listFiles();

    public creationDeletionJob(String fileName) {
        this.fileName = fileName;
    }

    @Override
    protected Boolean call() throws Exception {
        for(int i = 0; i< allCreations.length; i++){
            if(allCreations[i].getName().equals(fileName)){
                File deleteDir = new File("creations" + System.getProperty("file.separator") + fileName);
                for(File delete : deleteDir.listFiles()){
                    delete.delete();
                }
                deleteDir.delete();
            }
        }
        return true;
    }
}
