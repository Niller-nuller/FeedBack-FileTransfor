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
                System.out.println("Something has gone wrong");
            }
    }

    public static void handleClientConnection(Socket clientSocket) throws IOException {
            try (
                    DataInputStream stream = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                    DataOutputStream out = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream())))
                    {

                    System.out.println("Klient forbundet: " + clientSocket.getRemoteSocketAddress());

                    handleClientFileRequest(stream , out);

            } catch(IOException e){
                System.out.println("So far");
                e.printStackTrace();
            }
    }
    public static void handleClientFileRequest(DataInputStream stream, DataOutputStream out) throws IOException {

        boolean clientFileRequest = true;

        try {
            while (clientFileRequest) {

                String fileName = stream.readUTF();
                System.out.println(fileName);

                if (fileName == null || fileName.isBlank()) {
                    out.writeUTF("Error|Client sent empty request");
                    out.flush();
                    break ;
                }

                String[] parts = fileName.split("\\|", 2);

                if (parts.length != 2 || parts[0].isBlank()) {
                    out.writeUTF("Error|Invalid format");
                    out.flush();
                    break;
                }
                String fileCommand = parts[0];

                if(!fileCommand.contains("GET")) {
                    throw new IllegalArgumentException("Error|Invalid format");
                }
                out.writeUTF(fileName);
                out.flush();
                String filePayload = parts[1];
                File file = findFile(filePayload);

                sendConfirmation(out);

                sendFile(file, out);
                clientFileRequest = false;
            }

        } catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("Out of bounds!");
            out.writeUTF("Error|Invalid format");
            out.flush();

        } catch (IllegalArgumentException e) {
            out.writeUTF(String.valueOf(e));
            out.flush();
        }
    }
    public static File findFile(String fileName){

            if (fileName.contains("/")) {
                throw new IllegalArgumentException("Error|File name cannot contain '/'");

            } else {
                File file = new File("src/main/ServerFiles/" + fileName);
                if (file.isFile()) {
                    return file;
                }
            }
        throw new IllegalArgumentException("Error|Invalid file name");
    }
    public static void sendFile(File file, OutputStream out) throws IOException {


            int fileLength = (int) file.length();
            out.write(fileLength);
            out.flush();

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            out.flush();
    }
    public static void sendConfirmation(DataOutputStream out) throws IOException {
        out.writeUTF("Confirm sending file");
    }
}
