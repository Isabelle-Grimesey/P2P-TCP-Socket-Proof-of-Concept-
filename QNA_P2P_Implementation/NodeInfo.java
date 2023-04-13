import java.util.ArrayList;
import java.net.Socket;

public class NodeInfo {
    private int maxGroupSize = 5;
    private ArrayList<> sockets;
    private ArrayList<Question> localQuestions;
    private ArrayList<Question> othersQuestions;

    public NodeInfo() {
        this.sockets = new ArrayList<Socket>();
        this.questions = new ArrayList<Question>();
    }

    public NodeInfo(int maxGroupSize, ArrayList<Socket> sockets, ArrayList<Question> questions) {
        this.maxGroupSize = maxGroupSize;
        this.sockets = sockets;
        this.questions = questions;
    }

    public int getmaxGroupSize() {
        return maxGroupSize;
    }

    public void setmaxGroupSize(int maxGroupSize) {
        this.maxGroupSize = maxGroupSize;
    }

    public ArrayList<Socket> getSockets() {
        return sockets;
    }

    public void setSockets(ArrayList<Socket> sockets) {
        this.sockets = sockets;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(ArrayList<Question> questions) {
        this.questions = questions;
    }

    public void addQuestion(String question){
        this.questions.add(new Question(question));
    }


    @Override
    public String toString() {
        return "NodeInfo{" +
                "maxGroupSize=" + maxGroupSize +
                ", sockets=" + sockets +
                ", questions=" + questions +
                '}';
    }
}
