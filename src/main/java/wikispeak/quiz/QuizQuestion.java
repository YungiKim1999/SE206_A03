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

    public String hint(){
        return _answer.substring(0,1).toUpperCase();
    }

    public Boolean answerIsCorrect(String response){
        return _answer.toLowerCase().equals(response.toLowerCase());
    }

    @Override
    public String toString(){
        return "" + _creationName + System.getProperty("file.separator") + "quiz" + _quizElement;
    }


}
