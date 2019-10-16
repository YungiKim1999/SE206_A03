package wikispeak.quiz;

public final class Quiz {

    private static final QuizQuestion[] randomisedListOfQuestions = new QuizQuestion[0];

    //private constructor to prevent instantiation
    private Quiz(){
        throw new IllegalStateException("Quiz class, not to be instantiated");
    }

    public static QuizQuestion[] getQuizList(){
        return randomisedListOfQuestions;
    }

    public static void generateQuiz(int numberOfQuestions){

    }
}
