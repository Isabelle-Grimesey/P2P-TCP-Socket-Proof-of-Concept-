import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GuessingGameClient {
    public static void main(String[] args) throws IOException {
        String serverAddress = "127.0.0.1";
        int port = 5000;

        try (Socket socket = new Socket(serverAddress, port)) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                String serverMessage = in.readLine();
                System.out.println(serverMessage);

                if (serverMessage.startsWith("Congratulations")) {
                    break;
                }

                System.out.print("Enter your guess: ");
                String userInput = userIn.readLine();
                out.println(userInput);
            }
        }
    }
}