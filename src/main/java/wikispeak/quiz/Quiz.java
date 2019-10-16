package wikispeak.quiz;

import wikispeak.helpers.Command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Quiz {

    private static List<QuizQuestion> randomisedListOfQuestions = new ArrayList<>();

    //private constructor to prevent instantiation
    private Quiz(){
        throw new IllegalStateException("Quiz class, not to be instantiated");
    }

    public static List<QuizQuestion> getQuizList(){
        return randomisedListOfQuestions;
    }

    public static void generateQuiz(int questionsSelected){

        List<QuizQuestion> fullList = generateFullList();
        java.util.Collections.shuffle(fullList);

        randomisedListOfQuestions = fullList.subList(0, questionsSelected);

    }


    /**
     * Lists all possible questions that could be put into the quiz
     * @return list of QuizQuestion objects
     */
    private static List<QuizQuestion> generateFullList(){
        List<QuizQuestion> fullList = new ArrayList<>();

        File file = new File("creations");
        for (File creation : file.listFiles()) {
            String name = creation.getName();
            Command command = new Command("cat creations" + System.getProperty("file.separator") + name + System.getProperty("file.separator") + "answer.txt");
            command.execute();
            String answer = command.getStream();
            for(int i = 1; i <= 3; i++){
                fullList.add(new QuizQuestion(name, i, answer));
            }
        }

        return  fullList;
    }

}
