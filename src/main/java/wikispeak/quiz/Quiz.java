package wikispeak.quiz;

import wikispeak.helpers.Command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Quiz {

    private List<QuizQuestion> randomisedListOfQuestions;
    private List<String> creationsToReview = new ArrayList<>();

    private int totalNumberOfQuestions;
    private int numberCorrect = 0;
    private int numberIncorrect = 0;
    private int currentQuestion = 0;

    public Quiz(int questionsSelected){
        List<QuizQuestion> list = generateList();
        java.util.Collections.shuffle(list);
        randomisedListOfQuestions = list;
        totalNumberOfQuestions = questionsSelected;
    }

    public String getCurrentQuestion(){
        return randomisedListOfQuestions.get(currentQuestion).toString();
    }

    public String getCurrentHint(){ return randomisedListOfQuestions.get(currentQuestion).getHint();
    }

    public String getCurrentAnswer(){ return randomisedListOfQuestions.get(currentQuestion).getAnswer(); }

    public boolean submitResponse(String response){
        if(randomisedListOfQuestions.get(currentQuestion).answerIsCorrect(response)){
            numberCorrect++;
            currentQuestion++;
            return true;
        }
        else{
            numberIncorrect++;
            String creationName = randomisedListOfQuestions.get(currentQuestion).getCreationName();
            if(!creationsToReview.contains(creationName)){
                creationsToReview.add(creationName);
            }
            currentQuestion++;
            return false;
        }
    }

    public boolean isFinished(){
        return currentQuestion == totalNumberOfQuestions;
    }

    public int getNumberCorrect(){ return numberCorrect; }

    public int getNumberIncorrect(){ return numberIncorrect; }

    public int getCurrentQuestionNumber(){ return currentQuestion + 1; }

    public int getTotalNumberOfQuestions(){ return totalNumberOfQuestions; }

    public String getCreationsToReview(){
        String output = "";
        for(String creation : creationsToReview){
            output += "\n" + creation;
        }
        return output;
    }

    /**
     * Lists all possible questions that could be put into the quiz
     * @return list of QuizQuestion objects
     */
    private static List<QuizQuestion> generateList(){
        List<QuizQuestion> fullList = new ArrayList<>();

        File file = new File("creations");
        for (File creation : file.listFiles()) {
            String name = creation.getName();
            Command command = new Command("cat creations" + System.getProperty("file.separator") + name + System.getProperty("file.separator") + "answer.txt");
            command.execute();
            String answer = command.getStream().trim();
            //3 possible videos to play per quiz
            for(int i = 1; i <= 3; i++){
                fullList.add(new QuizQuestion(name, i, answer));
            }
        }

        return  fullList;
    }

    /**
     * toString method for testing purposes
     */
    @Override
    public String toString(){
        return "" + totalNumberOfQuestions + " requested. List:" + randomisedListOfQuestions.subList(0,totalNumberOfQuestions).toString();
    }

}
