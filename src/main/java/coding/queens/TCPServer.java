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

    public static void handleClientConnection(Socket clientSocket) {
            try (
                    BufferedReader reader = new BufferedReader((new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8)));
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true, StandardCharsets.UTF_8)) {

                System.out.println("Klient forbundet: " + clientSocket.getRemoteSocketAddress());

                handleClientFileRequest(reader , writer, clientSocket);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }
    public static void handleClientFileRequest(BufferedReader reader, PrintWriter writer, Socket clientSocket) throws IOException {

        boolean clientFileRequest = true;
        try {
            while (clientFileRequest) {

                String fileName = reader.readLine();
                System.out.println(fileName);

                if (fileName == null || fileName.isBlank()) {
                    writer.println("Error|Client sent empty request");
                    break ;
                }

                String[] parts = fileName.split("\\|", 2);

                if (parts.length != 2 || parts[0].isBlank()) {
                    writer.println("Error|Invalid format");
                    break;
                }
                String fileCommand = parts[0];

                if(!fileCommand.contains("GET")) {
                    throw new IllegalArgumentException("Error|Invalid format");
                }

                String filePayload = parts[1];

                File file = findFile(filePayload);

                sendFile(file, clientSocket.getOutputStream());
                clientFileRequest = false;
            }

        } catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("Out of bounds!");
            writer.println("Error|Invalid format");
        } catch (IllegalArgumentException e) {
            writer.println(e);
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
        DataOutputStream dos = new DataOutputStream(out);

        long fileLength = file.length();
        dos.writeLong(fileLength);
        dos.flush();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytesRead);
            }
        }
        dos.flush();
    }
}
