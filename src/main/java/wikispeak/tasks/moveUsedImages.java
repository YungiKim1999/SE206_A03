package wikispeak.tasks;

import javafx.concurrent.Task;

import java.io.File;
import java.util.ArrayList;

public class moveUsedImages extends Task<Void> {

    private ArrayList<File> usedImages;
    private ArrayList<File> unusedImages;

        public moveUsedImages(ArrayList<File> usedImages, ArrayList<File> unusedImages){

            this.usedImages = usedImages;
            this.unusedImages = unusedImages;
        }

        @Override
        protected Void call() throws Exception {

            int countUsed = usedImages.size();
            int countUnused = unusedImages.size();
           for(File image : usedImages){
               boolean alreadyInside = false;
               for(File checkImage : new File("images_to_use").listFiles()){
                   if(checkImage.equals(image)){
                       alreadyInside = true;
                   }
               }
               if(!alreadyInside){
                   image.renameTo(new File("images_to_use" + System.getProperty("file.separator") + image.getName()));
                   countUsed++;
               }
           }

            for(File image : unusedImages){
                boolean alreadyInside = false;
                for(File checkImage : new File("downloads").listFiles()){
                    if(checkImage.equals(image)){
                        alreadyInside = true;
                    }
                }
                if(!alreadyInside){
                    image.renameTo(new File("downloads" + System.getProperty("file.separator") + image.getName()));
                    countUnused++;
                }
            }
            return null;
        }
}
