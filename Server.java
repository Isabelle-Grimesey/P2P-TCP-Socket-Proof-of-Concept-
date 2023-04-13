import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
    private int localPort;

    Server(int localPort) {
        this.localPort=localPort;
    }
    public void run()
    {
        try (
                ServerSocket serverSocket = new ServerSocket(this.localPort); // closes resources try block is exited.
        ){
            while (!Thread.interrupted()) { // used  for interruptions that occur when the thread is not waiting
                try {
                    // initialize resources
                    Socket commSocket = serverSocket.accept(); // waits for incoming connection
                    PrintWriter out = new PrintWriter(commSocket.getOutputStream(), true);
                    InputStreamReader isrdr = new InputStreamReader(commSocket.getInputStream());
                    BufferedReader in = new BufferedReader(isrdr);

                    CommThread commThread = new CommThread(commSocket, out, isrdr, in);
                    // add commThread to list of threads
                    //
                    commThread.start();
                    //            System.out.println("Communication thread started"); //TEST
                } catch (Exception e) { // used, among other things,
                                        // for interruptions that occur when this thread is waiting
                    e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            }
        }
        catch (Exception e){ // when server socket cannot be created
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }
}