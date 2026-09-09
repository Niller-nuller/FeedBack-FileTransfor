package coding.queens;

import java.io.*;
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
             DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
             Scanner clientInput = new Scanner(System.in, StandardCharsets.UTF_8))
        {
            System.out.println("Connected to server");

            requestFile(clientInput,dataOutputStream,dataInputStream);

        } catch (IOException e) {
            System.out.println("Failed to connect to server");
        } catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
        }
    }

    public static void requestFile(Scanner clientInput, DataOutputStream dataOutputStream, DataInputStream dataInputStream) throws IOException {
        String fileName = handleClientFileRequest(clientInput); // GET|stuff.whatever

        sendFileRequest(fileName,dataOutputStream); // Sends the request to the server.

        String serverResponse = dataInputStream.readUTF();
        handleServerResponse(serverResponse);

        if (serverResponse.equals("ok!")) {
            String[] parts = fileName.split("\\|", 2);
            File destination = new File("src/main/ClientFiles/" + parts[1]);
            receiveFile(destination, dataInputStream);
            System.out.println("file saved to " + destination.getPath());
        }
    }

    public static String handleClientFileRequest(Scanner reader){
        System.out.println("Enter file name: ");
        System.out.println("With format GET|your file name");
        return reader.nextLine();
    }

    public static void sendFileRequest(String serverRequest, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeUTF(serverRequest);
        dataOutputStream.flush();
    }

    public static void handleServerResponse(String response){
        if (response.contains("Error")){
            System.out.println("Server responded with Error");
            throw new IllegalArgumentException(response);
        }
    }

    public static void receiveFile(File destination, DataInputStream in) throws IOException {

        long fileLength = in.readLong();

        try (FileOutputStream fos = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            long remaining = fileLength;
            int bytesRead;

            while (remaining > 0 &&
                    (bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        } catch (NullPointerException | FileNotFoundException e) {
            throw new IllegalArgumentException("Client file name has gone wrong");
        }
    }
}
