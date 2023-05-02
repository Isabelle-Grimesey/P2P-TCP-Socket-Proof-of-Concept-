/*
Question has:
	- question string
	- list of answers
	- a unique identifier of the associated ServiceThread.
*/
import java.util.ArrayList;

public class Question {
    private String question;
    private ArrayList<String> answers;


    public Question(String question) {
        this.question = question;
        this.answers = new ArrayList<String>();
    }

    public String getQuestion() {
        return question;
    }

    public ArrayList<String> getAnswers() {
        return answers;
    }

    public void addAnswer(String answer){
        this.answers.add(answer);
    }

    @Override
    public String toString() {
        return "Question{" +
                "question='" + question + '\'' +
                ", answers=" + answers +
                '}';
    }
}
