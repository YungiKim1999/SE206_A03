package wikispeak.tasks;

import javafx.concurrent.Task;

import java.io.File;
import java.util.ArrayList;

public class moveUsedImages extends Task<Void> {

    private ArrayList<File> images;

        public moveUsedImages(ArrayList<File> images){
            this.images = images;
        }

        @Override
        protected Void call() throws Exception {

            for (File file : new File("images_to_use").listFiles()){
                file.delete();
            }
            int count = 0;
           for(File image : images){
               image.renameTo(new File("images_to_use" + System.getProperty("file.separator") + "image"+count + ".jpg"));
               count++;
           }
            return null;
        }
}
