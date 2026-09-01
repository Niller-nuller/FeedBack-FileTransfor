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
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
             Scanner clientInput = new Scanner(System.in, StandardCharsets.UTF_8))
        {
            System.out.println("Connected to server");

            writer.println(handleClientFileRequest(clientInput));

            String fileRequest = reader.readLine();

            handleServerResponse(fileRequest);

            serverResponse(fileRequest);


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
    public static void serverResponse(String response){
        System.out.println(response);
    }

    public static void receiveFile(String fileRequest, InputStream in) throws IOException{
        DataInputStream dis = new DataInputStream(in);
        String[] parts = fileRequest.split("\\|", 2);
        String fileName = parts[1];
        File destination = new File ("src/main/ClientFiles/" +  fileName);

        long fileLength = dis.readLong();

        try (FileOutputStream fos = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            long remaining = fileLength;
            int bytesRead;

            while (remaining > 0 &&
                    (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }
        FileWriter fw = new FileWriter(destination, true);
        fw.close();
    }
    public static void receiveFiles2(String fileName){

    }

}
