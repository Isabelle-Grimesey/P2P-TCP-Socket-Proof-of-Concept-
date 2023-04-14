/*
Question has:
	- question string
	- list of answers
	- a unique identifier of the associated ServiceThread.
*/
import java.util.ArrayList;
import java.util.UUID;

public class Question {
    private String question;
    private ArrayList<String> answers;

    public UUID getUuidofType1Conn() {
        return uuidofType1Conn;
    }

    public UUID getUuidofType2Conn() {
        return uuidofType2Conn;
    }

    private UUID uuidofType1Conn = null;
    private UUID uuidofType2Conn = null;


    public Question(String question) {
        this.question = question;
        this.answers = new ArrayList<String>();
    }
    public Question(String question, boolean type1, UUID uuidofAssociatedServiceThread){
        this.question = question;
        this.answers = new ArrayList<String>();
        if (type1){
            uuidofType1Conn = UUID.randomUUID();
        }
        else{
            uuidofType2Conn = UUID.randomUUID();
        }
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
