package wikispeak.tasks;

import javafx.concurrent.Task;

import java.io.File;

public class deletionJobs extends Task<Boolean> {

    private String selectedCreation;
    public deletionJobs(String selectedCreation){
        this.selectedCreation = selectedCreation;
    }
    @Override
    protected Boolean call() throws Exception {
        File file = new File("creations" + System.getProperty("file.separator") + selectedCreation + ".mp4");
        file.delete();
        return true;
    }
}
