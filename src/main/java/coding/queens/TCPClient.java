package coding.queens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class TCPClient {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("Starter klient ...");
        startClient();
    }
    public static void startClient(){

        System.out.println("Connecting to server...");

        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
             Scanner clientInput = new Scanner(System.in, StandardCharsets.UTF_8))
        {
            System.out.println("Connected to server");

            writer.println(handleClientFileRequest(clientInput));

            String fileRequest = reader.readLine();

            handleServerResponse(fileRequest);




        } catch (IOException e) {
            System.out.println("Failed to connect to server");
        }
    }
    public static String handleClientFileRequest(Scanner reader){
        System.out.println("Enter file name: ");
        System.out.println("With format GET|your file name");
        return reader.nextLine();
    }
    public static void handleServerResponse(String response){
        if (response.contains("Error")){
            System.out.println("Server responded with Error");
            System.out.println(response);
        }
    }
}
