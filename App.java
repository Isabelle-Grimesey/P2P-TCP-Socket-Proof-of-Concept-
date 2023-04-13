import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class App {
    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to a Q&A app where you can ask and answer" +
                "questions of anyone in the group.)");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        boolean quit = false;
        while (!quit){
            System.out.println("To join a group by requesting a connection to an online peer, enter '1'.");
            System.out.println("To ask a question, enter '2'.");
            System.out.println("To view your questions and their answers, enter '3'.");
            System.out.println("To view others' questions and those questions' answers, and " +
                    "to answer those questions, press '4'.");
            System.out.println("To close the app, press any other key.");
            int command = Integer.parseInt(br.readLine());
            switch (command){
                case 1:
                    connectToNetwork();
                    break;
                case 2:
                    askQuestion();
                    break;
                case 3:
                    viewLocalQuestions();
                    break;
                case 4:
                    viewPeerQuestions();
                    break;
                default:
                    leaveNetwork();
                    quit=true;
            }
        }
    }
}
