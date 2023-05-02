import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
server has:
	- servicethreadlist
	- list of questions (and a lock for it)

server can:
    - accept a connection

	when server dies (take user input from terminal):
		- interrupt all service threads
		- wait for service threads to die
 */
public class Server {
    static ArrayList<ServiceThread> serviceThreadList = new ArrayList<ServiceThread>();
    static volatile ArrayList<Question> centralQuestionList = new ArrayList<Question>(); // all threads must have same copy
    static volatile Lock lock_centralQuestionList = new ReentrantLock(); // all threads must have same copy


    /*
    ServiceThread can:
        - read user request, check code and perform the appropriate service:
				- 0 store a received question in central question list
				- 1 send a question and its answers based on the code received
				- 2 receive an answer to a given question, store the answer in the central question list, and push the answer to the corresponding client.
     */
    public static class ServiceThread extends Thread {
        private Socket socket;
        private PrintWriter out;
        private InputStreamReader isrdr;
        private BufferedReader in;

        ServiceThread(
                Socket socket,
                PrintWriter out,
                InputStreamReader isrdr,
                BufferedReader in
        ) throws IOException {
            this.socket = socket;
            this.out = out;
            this.isrdr = isrdr;
            this.in = in;
        }

        @Override
        public void run() {
            try {
               if (!socket.isConnected()) {
                    this.interrupt();
                }
                    Question lastQuestionRead = null;
                    String code = in.readLine(); // read from socket
                    ArrayList<Question> clientQuestions = new ArrayList<Question>();
                    while (socket.isConnected() && code != null) {
                        switch (code) {
                            case "0": // receive and store a question in both the central list and client list
                                String newQuestionString = in.readLine();
                                if (newQuestionString != null) {
                                    Question newQuestion = new Question(newQuestionString);
                                    lock_centralQuestionList.lock();
                                    centralQuestionList.add(newQuestion);
                                    lock_centralQuestionList.unlock();
                                    clientQuestions.add(newQuestion);
                                }
                                break;
                            case "1": // send a random question from centralquestionlist
                                lock_centralQuestionList.lock();
                                Question question = centralQuestionList.get((int) ((Math.random() * (centralQuestionList.size() - 0)) + 0));
                                lock_centralQuestionList.unlock();
                                out.flush();
                                out.println(question.toString());
                                out.flush();
                                // save the last question read (so we can store the answer)
                                lastQuestionRead = question;
                                break;
                            case "2": // receive and answer to the last question sent
                                String answer = in.readLine();
                                if (answer == null) {
                                    break;
                                }
                                lastQuestionRead.addAnswer(answer);
                                break;
                            case "3":
//                                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
//                                objectOutputStream.flush(); socket.getOutputStream().flush();
//                                objectOutputStream.writeObject(clientQuestions.toString());
//                                out.flush();
//                                out.println(clientQuestions.toString());
                                out.flush();
                                System.out.println(clientQuestions.toString());
                                out.println(clientQuestions.toString());
                                System.out.println("printed correctly");
                                out.flush();

                            default:
                                System.out.println("Received invalid command code.");
                                break;
                        }
                        code = in.readLine(); // read from socket
                    }
                    System.out.println("Client disconnected.");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e.getMessage());
            } finally {
                // close resources from most dependent to least dependent.
                try {
                    isrdr.close();
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public static void main(String[] args) throws InterruptedException {
        if (args.length != 1) {
            System.out.println("usage: Server <server-port>");
            return;
        }
        // parse program args
            int srvr_port = Integer.parseInt(args[0]); // "usage above 20,000 is fairly sparse" - try 25,000
        try (
                ServerSocket serverSocket = new ServerSocket(srvr_port);
        ){
            while (true){ // when the client closes, the count of clients should go down
                    try
                    {
                        // initialize resources
                        Socket clientSocket = serverSocket.accept(); // waits for incoming connection
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        InputStreamReader isrdr = new InputStreamReader(clientSocket.getInputStream());
                        BufferedReader in = new BufferedReader(isrdr);

                        ServiceThread client_service = new ServiceThread(clientSocket, out, isrdr, in);
                        serviceThreadList.add(client_service);
                        client_service.start();

                    }
                    catch (Exception e){
                        e.printStackTrace();
                        System.err.println(e.getMessage());
                    }
                }
        }
        catch (Exception e){
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
        finally{
            for(ServiceThread thread: serviceThreadList){
                thread.interrupt();
            }
            for(ServiceThread thread: serviceThreadList){
                thread.join();
            }
        }
    }
}
