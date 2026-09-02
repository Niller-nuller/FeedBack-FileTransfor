package coding.queens;

import java.io.*;
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

    public static void handleClientConnection(Socket clientSocket) throws IOException {

            try (
                    DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream())) {

                System.out.println("Klient forbundet: " + clientSocket.getRemoteSocketAddress());

                handleClientFileRequest(dataInputStream , dataOutputStream);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }
    public static void handleClientFileRequest(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {

        boolean clientFileRequest = true;

        try {
            while (clientFileRequest) {

                String fileName = dataInputStream.readUTF();
                System.out.println(fileName);
                if (fileName.isEmpty()) {
                    dataOutputStream.writeUTF("Error|Client sent empty request");
                    dataOutputStream.flush();
                }

                String[] parts = fileName.split("\\|", 2);

                if (parts.length != 2 || parts[0].isBlank()) {
                    dataOutputStream.writeUTF("Error|Invalid format");
                    dataOutputStream.flush();
                }
                String fileCommand = parts[0];
                String filePayload = parts[1];

                dataOutputStream.writeUTF(fileCommand + " " + filePayload);
                dataOutputStream.flush();
                clientFileRequest = false;
            }

        } catch(ArrayIndexOutOfBoundsException e) {
            dataOutputStream.writeUTF("Error|Invalid format");
            dataOutputStream.flush();
        }
    }
}
