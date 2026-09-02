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
                System.out.println("Critical Error: " + e.getMessage());;
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

                String filePayload = validateRequest(fileName, dataInputStream, dataOutputStream);

                File fileToSend = findFile(filePayload);

                sendFile(fileToSend, dataOutputStream);
                clientFileRequest = false;
            }

        } catch(ArrayIndexOutOfBoundsException e) {
            dataOutputStream.writeUTF(e.getMessage());
            dataOutputStream.flush();
        } catch (IllegalArgumentException e) {
            dataOutputStream.writeUTF(e.getMessage());
            dataOutputStream.flush();
        }
    }

    public static String validateRequest(String fileName, DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {

        System.out.println(fileName);
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("Error|Client sent empty request");
        }

        String[] parts = fileName.split("\\|", 2);

        if (parts.length != 2 || parts[0].isBlank()) {
            throw new ArrayIndexOutOfBoundsException("Error|Invalid format");
        }
        String fileCommand = parts[0];
        String filePayload = parts[1];

        dataOutputStream.writeUTF("Confirmed!");
        dataOutputStream.flush();

        return filePayload;
    }


    public static File findFile(String filePayload){

        if (filePayload.contains("/")) {
            throw new IllegalArgumentException("Error|File name cannot contain '/'");
        } else {
            File file = new File("src/main/ServerFiles/" + filePayload);
            if (file.isFile()) {
                return file;
            }
        }
        throw new IllegalArgumentException("Error|Invalid file name");
    }

    public static void sendFile(File file, DataOutputStream dos) throws IOException {

        long fileLength = file.length();
        dos.writeLong(fileLength);
        dos.flush();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int byteRead;
            while ((byteRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, byteRead);
            }
        }
        dos.flush();
        System.out.println("File sent to client");
    }
}
