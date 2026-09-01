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
             DataInputStream stream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
             //BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             //PrintWriter writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
             Scanner clientInput = new Scanner(System.in, StandardCharsets.UTF_8))
        {
            System.out.println("Connected to server");

            String fileName = handleClientFileRequest(clientInput);

            out.writeUTF(fileName);
            out.flush();

            String fileRequestResponds = stream.readUTF();
            handleServerResponse(fileRequestResponds);

            if(fileRequestResponds.equals("Confirm sending file")){
                receiveFile(fileName, stream);
            }

        } catch (IOException e) {
            System.out.println("Failed to connect to server");
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            //throw new R2untimeException(e);
        }
    }
    public static String handleClientFileRequest(Scanner reader){
        System.out.println("Enter file name: ");
        System.out.println("With format GET|your file name");
        return reader.nextLine();
    }
    public static void handleServerResponse(String response)throws IllegalStateException {
        if(response == null){
            System.out.println("No response from server");
        }
        if (response.contains("Error")){
            throw new IllegalArgumentException(response);
        }
    }


    public static void receiveFile(String fileRequest, InputStream stream) throws IOException{
        DataInputStream dis = new DataInputStream(stream);

        String[] parts = fileRequest.split("\\|", 2);
        String fileName = parts[1];
        File destination = new File ("src/main/ClientFiles/" +  fileName);

        int fileLength = dis.readInt();

        try (FileOutputStream fos = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int remaining = fileLength;
            int bytesRead;

            while (remaining > 0 &&
                    (bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
        }
    }
}
