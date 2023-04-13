import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class GuessingGameServer {
    public static void main(String[] args) throws IOException {
        int port = 5000;
        Random random = new Random();
        int targetNumber = random.nextInt(10) + 1;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Waiting for client...");
            try (Socket clientSocket = serverSocket.accept()) {
                System.out.println("Client connected.");
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                out.println("Guess a number between 1 and 10");
                boolean gameRunning = true;
                while (gameRunning) {
                    String input = in.readLine();
                    int guess = Integer.parseInt(input);

                    if (guess == targetNumber) {
                        out.println("Congratulations! You guessed the correct number: " + targetNumber);
                        gameRunning = false;
                    } else if (guess < targetNumber) {
                        out.println("Too low, try again.");
                    } else {
                        out.println("Too high, try again.");
                    }
                }
            }
        }
    }
}