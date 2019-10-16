package wikispeak.quiz;

public class QuizQuestion {

    private String _creationName;
    private int _quizElement;
    private String _answer;

    public QuizQuestion(String creationName, int quizElement, String answer){
        _creationName = creationName;
        _quizElement = quizElement;
        _answer = answer;
    }

    @Override
    public String toString(){
        return "" + _creationName + " " + _quizElement + " " + _answer;
    }


}
