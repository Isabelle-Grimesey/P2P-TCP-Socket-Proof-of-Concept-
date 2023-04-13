import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CommThread extends Thread{
    // main accesses socket to write to it and view the ip and port of the other endpoint.
        public Socket stc;
        public PrintWriter out;
    private InputStreamReader isrdr;
    private BufferedReader in;

    CommThread(Socket stc, //  server to client
               PrintWriter out,
               InputStreamReader isrdr,
               BufferedReader in){
        this.stc = stc;
        this.out = out;
        this.isrdr = isrdr;
        this.in = in;
    }

    public void run(){
        try{
            // TEST
            String clientMessage = in.readLine();
            while(stc.isConnected()
                    && clientMessage!=null) // detect disconnection from other endpoint
            {
                System.out.println(stc.getInetAddress()+":"+stc.getPort()+":"+clientMessage);
                clientMessage = in.readLine();
            }
//            System.out.println("client disconnected. socket closing..."); //TEST
        } catch (Exception e){
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
        finally{
            try{ // close resources from most dependent to least dependent.
                isrdr.close();
                in.close();
                out.close();
                stc.close();
            } catch (Exception e){

            }
        }
    }
}
