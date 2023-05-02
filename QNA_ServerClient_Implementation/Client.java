/*
client has:
	- client question list (and a lock for it)
	- socket and associated resources
	- answerreceipt thread
client can:
    - 0 ask a question (send code 0 to server)
    - 1 read a random question and its answers from the server (send code 1 to server) (lock the input stream).
    - 2 provide an answer to the last-read question (send code 2 to server)
    - 3 view their own questions and answers (no socket communication)
 */

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Client {
    static ArrayList<Question> clientQuestions = null;
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
        srvr_port = Integer.parseInt(args[1]); // "usage above 20,000 is fairly sparse" - try 30,000

        // create socket and associated resources
        try (
                Socket socket = new Socket(srvr_ip, srvr_port);
                // Even if created without error, this socket may not be connected. If the server was
                // fully loaded at the time of this socket's creation, then this socket will not be connected.
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Note that 'out' and 'in' are defined relative to this socket and its connection, not to this socket and this program.
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        ) {
            if (socket.isConnected()) {
                // user-interaction loop
                // print out guidance message
                System.out.println("Welcome to the Questions and Answers App!");
                System.out.println("----------------------------------------");
                System.out.println("To ask a Question, enter 0.");
                System.out.println("To see a random question from the server and its answers, enter 1.");
                System.out.println("To provide an answer to the last question you viewed, enter 2.");
                System.out.println("To view your questions and their answers, enter 3.");
                System.out.println("To quit the program, enter 4.");
                // get initial user command
                int code = Integer.parseInt(stdIn.readLine());
                // while loop
                while (code != 4) {
                    switch (code) {
                        case 0:
                            System.out.println("Enter your question on a single line: ");
                            String question = stdIn.readLine();
                            out.flush();
                            out.println("0"); // write to socket
                            out.flush();
                            out.println(question); // write to socket
                            break;
                        case 1:
                            out.flush();
                            out.println("1"); // write to socket
                            String lastQuestionRead = in.readLine(); // read from socket
                            if (lastQuestionRead == null) {
                                System.out.println("Server has disconnected");
                            } else {
                                System.out.println(lastQuestionRead);
                            }
                            break;
                        case 2:
                            System.out.println("Enter your answer to the last question on a single line: ");
                            out.flush();
                            out.println("2"); // write to socket
                            out.flush();
                            out.println(stdIn.readLine()); // write to socket
                            break;
                        case 3:
                            out.flush();
                            out.println("3"); // write to socket
                            out.flush();
                            // get clientQuestions from server
                            String clientQuestionsString = in.readLine(); // read from socket
                            System.out.println(clientQuestionsString);
                            break;
                        default:
                            System.out.println("Invalid Input.");
                            break;
                    }
                    // get next user command
                    System.out.println("----------------------------------------");
                    System.out.println("To ask a Question, enter 0.");
                    System.out.println("To see a random question and its answers, enter 1.");
                    System.out.println("To provide an answer to the last-asked question, enter 2.");
                    System.out.println("To view your questions and their answers, enter 3.");
                    System.out.println("To quit the program, enter 4.");
                    code = Integer.parseInt(stdIn.readLine());
                }
            } else {
                System.out.println("Problem in connecting to server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }
}
