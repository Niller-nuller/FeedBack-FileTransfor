package coding.queens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class TCPServer {

    private static final int PORT = 5000;

    public static void main(String[] args) {

        System.out.println("Starter server på port 5000 ...");
        startServer();
    }

    public static void startServer() {

            try (ServerSocket serverSocket = new ServerSocket(PORT)) {

                while (true) {

                    try (Socket clientSocket = serverSocket.accept()) {

                        handleClientConnection(clientSocket);

                    } catch(IOException e){
                        throw new RuntimeException(e);
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    public static void handleClientConnection(Socket clientSocket) {

            try (
                    BufferedReader reader = new BufferedReader((new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8)));
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8)) {

                System.out.println("Klient forbundet: " + clientSocket.getRemoteSocketAddress());

                handleClientFileRequest(reader , writer);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }
    public static void handleClientFileRequest(BufferedReader reader, PrintWriter writer) throws IOException {

        boolean clientFileRequest = true;
        try {
            while (clientFileRequest) {

                String fileName = reader.readLine();
                System.out.println(fileName);
                if (fileName == null || fileName.isEmpty()) {
                    writer.println("Error|Client sent empty request");
                }

                String[] parts = fileName.split(">\\|", 2);

                if (parts.length != 2 || parts[0].isBlank()) {
                    writer.println("Error|Invalid format");
                }
                String fileCommand = parts[0];
                String filePayload = parts[1];

                writer.println(fileCommand + " " + filePayload);
                clientFileRequest = false;
            }

        } catch(ArrayIndexOutOfBoundsException e) {
            writer.println("Error|Invalid format");
        }
    }
}
