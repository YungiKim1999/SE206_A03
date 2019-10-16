package wikispeak.quiz;

/**
 * A quiz holder that allows a quiz to be passed between QuizStartScreenController and QuizScreenController
 */
public final class QuizPasser {

    private static Quiz currentQuiz;

    public static void makeQuiz(int numberOfQuestions){
        currentQuiz = new Quiz(numberOfQuestions);
    }

    public static Quiz getCurrentQuiz(){
        return currentQuiz;
    }

}
