import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class App {
    // node info is thread-safe
        static volatile NodeInfo nodeinfo = new NodeInfo(); // volatile - all threads access same copy
        static volatile Lock lock_nodeinfo = new ReentrantLock(); // lock is so that multiple threads don't try to write at once.
    // list of worker threads is thread-safe
        static volatile ArrayList<Thread> commthreads = new ArrayList<Thread>();
        static volatile Lock lock_commthreads = new ReentrantLock();
    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    static Server serverThread;

    public static void main(String[] args) throws IOException {
        System.out.println("Welcome to a Q&A app where you can ask and answer" +
                "questions of anyone in the group.)");
        System.out.println("Please enter in the local port number you would like to use." +
                " To use the default, simply press enter.");
        int localPort;
        try{
            localPort = Integer.parseInt(br.readLine());
        } catch (Error err){
            System.out.println("Default port number 29492 will be used.");
            localPort = 29492;
        }
        // start server thread
            serverThread = new Server(localPort);
            serverThread.start();
        boolean quit = false;
        while (!quit){
            System.out.println("To join a group / merge your group with another, by requesting a connection to an online peer, enter '1'.");
            System.out.println("To view everyone else in your group, enter '2'.")
            System.out.println("To ask a question, enter '3'.");
            System.out.println("To view your questions and their answers, enter '4'.");
            System.out.println("To view others' questions and those questions' answers, and " +
                    "to answer those questions, press '5'.");
            System.out.println("To close the app, press any other key.");
            int command = Integer.parseInt(br.readLine());
            switch (command){
                case 1:
                    connectToGroup();
                    break;
                case 2:
                    System.out.println(nodeinfo.getSockets().toString());
                    break;
                case 3:
                    System.out.println("Enter your question: ");
                    lock_nodeinfo.lock();
                        nodeinfo.addQuestion(br.readLine());
                    lock_nodeinfo.unlock();
                    break;
                case 4:
                    System.out.println(nodeinfo.getQuestions().toString());
                    break;
                case 5:
                    viewPeerQuestions(); //TODO: create a lock over nodeinfo when you are adding an answer.
                    break;
                default:
                    leaveGroup();
                    quit=true;
            }
        }
    }
    public void connectToGroup(){
        System.out.println("Enter <dest ip> <dest port> <optional: src port>");
        System.out.println(br.readLine().split(" "));
    }
    public void viewPeerQuestions(){

    }
    public void leaveGroup() throws InterruptedException {
        // Shut down server socket first
        // so that no new connections can be made while we are deleting the existing connections.
            serverThread.interrupt();
            serverThread.join(); // Waits for server thread to die.
        // Tell the other nodes in the group that this node is leaving via a quit message.
        // close all the sockets.
        //
    }
}
