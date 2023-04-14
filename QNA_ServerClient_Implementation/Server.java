import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
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
        private UUID uuid = UUID.randomUUID();

        public UUID getUuid() {
            return uuid;
        }

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
                // determine type of connection
                String connectionType = in.readLine();
                if (!socket.isConnected() || connectionType == null) {
                    this.interrupt();
                }
                if (connectionType == "type1") {
                    // send the uuid of this thread to the client
                        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                        objectOutputStream.flush(); socket.getOutputStream().flush();
                        objectOutputStream.writeObject(uuid);
                    Question lastQuestionRead = null;
                    String code = in.readLine(); // read from socket
                    while (socket.isConnected() && code != null) {
                        switch (code) {
                            case "0": // receive and store a question
                                String newQuestion = in.readLine();
                                if (newQuestion != null) {
                                    lock_centralQuestionList.lock();
                                    centralQuestionList.add(new Question(newQuestion, true, uuid));
                                    lock_centralQuestionList.unlock();
                                }
                                break;
                            case "1": // send a random question from centralquestionlist
                                lock_centralQuestionList.lock();
                                Question question = centralQuestionList.get((int) Math.random() * centralQuestionList.size());
                                lock_centralQuestionList.unlock();
                                out.flush();
                                out.println(question.toString());
                                out.flush();
                                // save the last question read (so we can forward the client's answer to the asker)
                                lastQuestionRead = question;
                                break;
                            case "2": // receive answer to the last question sent; store this answer in the central list and forward it to the asker.
                                String answer = in.readLine();
                                if (answer == null) {
                                    break;
                                }
                                lastQuestionRead.addAnswer(answer);
                                // notify thread by uuid
//                                    // find thread
//                                        for (ServiceThread thread : serviceThreadList) {
//                                            if (thread.getUuid()==lastQuestionRead.getUuidofType2Conn()) {
//                                                synchronized(this){
//                                                    thread.notify();
//                                                }
//                                                break;
//                                            }
//                                        }
                                synchronized(this){
                                    notify();
                                }
                                break;
                            default:
                                System.out.println("Received invalid command code.");
                                break;
                        }
                        code = in.readLine(); // read from socket
                    }
                    System.out.println("Client disconnected.");
                }
                else if (connectionType == "type2") {
                    // receive uuid of the service thread of this client's type1 connection.
                        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                        UUID uuid_srvThread_Type1Conn = (UUID) objectInputStream.readObject();
                    // get service thread by uuid.
                        ServiceThread notifier = null;
                        for (ServiceThread thread : serviceThreadList) {
                            if (thread.getUuid()==uuid_srvThread_Type1Conn) {
                                notifier = thread;
                                break;
                            }
                        }
                        if (notifier!=null){
                            while(!interrupted()){
                                synchronized(notifier){
                                    notifier.wait();

                                }
                            }
                            // when a new answer is created, send the question and the answer to the client.
                            while(!interrupted()){
                                notifier.wait();
                                //TODO:create servicethread.mostRecentAnswer with a lock. get notifier.mostRecentAnswer
                                    // and forward it to the client, with it being locked. go implement a lock where mostrecentanswer is initialized.
                            }
                        }//TODO: service threads must add themselves to list
                }
                else {
                    System.out.println("Cannot determine connection type.");
                    this.interrupt();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e.getMessage());
            } finally {
                // close resources from most dependent to least dependent.
                isrdr.close();
                in.close();
                out.close();
                socket.close();
            }
        }
    }
    public static void main(String[] args){
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

                        Thread client_service = new ServiceThread(clientSocket, out, isrdr, in);
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
    }
}
