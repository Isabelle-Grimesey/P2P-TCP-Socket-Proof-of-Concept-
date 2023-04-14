/*
client has:
	- client question list (and a lock for it)
	- socket and associated resources
	- answerreceipt thread
client can:
    - 0 ask a question (send code 0 to server)
    - 1 read somebody else's question (send code 1 to server) and its answers from the server. (lock the input stream).
    - 2 provide an answer to the last-asked question (send code 2 to server)
    - 3 view their own questions and answers (no socket communication)
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client {
    static volatile ArrayList<Question> clientQuestions = new ArrayList<Question>(); // both threads must have same copy
    static volatile Lock lock_clientQuestions = new ReentrantLock(); // both threads must have same copy
    static String srvr_ip;
    static int srvr_port;

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2) {
            System.out.println("usage: Client <server-ip> <server-port>");
            return;
        }
        // parse program args
            // run server on same internet-connected device as client, and use local loopback address:
            // 127.0.01
            srvr_ip = args[0];
            srvr_port = Integer.parseInt(args[1]); // "usage above 20,000 is fairly sparse" - try 29492

        // startup answer receipt
            AnswerReceiptThread answerReceiptThread = new AnswerReceiptThread();
            answerReceiptThread.start();

        // create socket and associated resources
        try (
                Socket socket_conn1 = new Socket(srvr_ip, srvr_port);
                // Even if created without error, this socket may not be connected. If the server was
                // fully loaded at the time of this socket's creation, then this socket will not be connected.
                PrintWriter out = new PrintWriter(socket_conn1.getOutputStream(), true); // Note that 'out' and 'in' are defined relative to this socket and its connection, not to this socket and this program.
                BufferedReader in = new BufferedReader(new InputStreamReader(socket_conn1.getInputStream()));
                BufferedReader stdIn =  new BufferedReader(new InputStreamReader(System.in));
        ){
            if (socket_conn1.isConnected()){
                // tell server this is a type 1 connection
                    out.println("type1"); // write to socket
                // user-interaction loop
                    // print out guidance message
                    System.out.println("Welcome to the Questions and Answers App!");
                    System.out.println("----------------------------------------");
                    System.out.println("To ask a Question, enter 0.");
                    System.out.println("To see someone else's question and its answers, enter 1.");
                    System.out.println("To provide an answer to the last-asked question, enter 2.");
                    System.out.println("To view your questions and their answers, enter 3.");
                    System.out.println("To quit the program, enter 4.");
                    // get initial user command
                    int code = Integer.parseInt(stdIn.readLine());
                    String lastQuestionRead = "";
                    // while loop
                    while (code != 4){
                        switch (code) {
                            case 0:
                                System.out.println("Enter your question on a single line: ");
                                String question = stdIn.readLine();
                                out.flush();
                                out.println("0"+question); // write to socket
                                // store question locally
                                lock_clientQuestions.lock();
                                clientQuestions.add(new Question(question));
                                lock_clientQuestions.unlock();
                                break;
                            case 1:
                                out.flush();
                                out.println("1"); // write to socket
                                lastQuestionRead = in.readLine(); // read from socket
                                if (lastQuestionRead == null){
                                    System.out.println("Server has disconnected");
                                }
                                else{
                                    System.out.println(lastQuestionRead);
                                }
                                break;
                            case 2:
                                System.out.println("Enter your answer to the last question on a single line: ");
                                out.flush();
                                out.println("2"+lastQuestionRead); // write to socket
                                out.println(stdIn.readLine()); // write to socket
                                break;
                            case 3:
                                System.out.println(clientQuestions);
                                break;
                            default:
                                System.out.println("Invalid Input.");
                                break;
                        }
                        // get next user command
                        System.out.println("----------------------------------------");
                        System.out.println("To ask a Question, enter 0.");
                        System.out.println("To see someone else's question and its answers, enter 1.");
                        System.out.println("To provide an answer to the last-asked question, enter 2.");
                        System.out.println("To view your questions and their answers, enter 3.");
                        System.out.println("To quit the program, enter 4.");
                        code = Integer.parseInt(stdIn.readLine());
                    }
            }
            else{
                System.out.println("Problem in connecting to server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
        // Close remaining open resources before closing program.
        answerReceiptThread.interrupt();
        answerReceiptThread.join();
    }
    /*
    answerreceiptthread has:
        - a socket and associated resources.
        - access to the client thread's question list (and its lock)
    answerreceiptthread can:
        - accept and store answers to its questions from the server.
    */
    public static class AnswerReceiptThread extends Thread{
        public void run(){
            try (
                    Socket socket_conn2 = new Socket(srvr_ip, srvr_port);
                    // Even if created without error, this socket may not be connected. If the server was
                    // fully loaded at the time of this socket's creation, then this socket will not be connected.
                    PrintWriter out = new PrintWriter(socket_conn2.getOutputStream(), true); // Note that 'out' and 'in' are defined relative to this socket and its connection, not to this socket and this program.
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket_conn2.getInputStream()));
                    ){
                if (socket_conn2.isConnected()) {
                    // tell server that this socket is a type 2 connection.
                        out.println("type2"); // write to socket
                    while (!interrupted()) {
                        String answered_question = in.readLine(); // read from socket
                        String answer = in.readLine(); // read from socket
                        if (answered_question == null || answer == null){
                            System.out.println("Server has disconnected");
                            this.interrupt();
                        }
                        // store answer to question
                        lock_clientQuestions.lock();
                        for (Question question : clientQuestions) {
                            if (question.getQuestion().compareTo(answered_question) == 0) {
                                question.addAnswer(answer);
                                break;
                            }
                        }
                        lock_clientQuestions.unlock();
                    }
                } else{
                    System.out.println("Problem in connecting to server.");
                }
            } catch (Exception e){
                e.printStackTrace();
                System.err.println(e.getMessage());
            }

        }
    }
}
