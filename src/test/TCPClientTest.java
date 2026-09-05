import coding.queens.TCPClient;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

public class TCPClientTest {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void TCPClientTest() {
        TCPClient.handleServerResponse("Error");
    }
    
    @Test(expectedExceptions = FileNotFoundException.class)
    public void testReceiveFileDestinationFolderNotExist() throws IOException {
        // Setup: Opret en destination hvor parent-folder ikke eksisterer
        File destination = new File("src/main/NonExistentFolder/testfile.txt");

        // Mock en DataInputStream
        DataInputStream mockStream = new DataInputStream(
                new ByteArrayInputStream(new byte[]{0, 0, 0, 0, 0, 0, 0, 10}) // fileLength = 10
        );

        // Execute: Kald receiveFile - skal failé fordi folder ikke eksisterer
        TCPClient.receiveFile(destination, mockStream);

        // Assert happens automatically via expectedExceptions
    }
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testReceiveFileReadOnlyDestination() throws IOException {
        // Setup: Opret en read-only fil
        File destination = new File("src/main/ClientFiles/readonly_test.txt");
        destination.getParentFile().mkdirs();
        destination.createNewFile();
        destination.setReadOnly(); // Gør filen read-only

        // Mock en DataInputStream med data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeLong(10); // fileLength = 10
        dos.writeBytes("0123456789"); // 10 bytes test data

        ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream mockStream = new DataInputStream(inputStream);

        try {
            // Execute: Skal failé fordi filen er read-only
            TCPClient.receiveFile(destination, mockStream);
            fail("Should throw IOException for read-only file");
        } finally {
            // Cleanup
            destination.setWritable(true);
            destination.delete();
        }
    }
    @Test
    public void testReceiveFileIncompleteData() throws IOException {
        // Setup: Opret destination
        File destination = new File("src/main/ClientFiles/incomplete_test.txt");
        destination.getParentFile().mkdirs();

        // Mock en DataInputStream der returnerer færre bytes end forventet
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeLong(100); // Siger at filen er 100 bytes
        dos.writeBytes("0123456789"); // Men sender kun 10 bytes

        ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
        DataInputStream mockStream = new DataInputStream(inputStream);

        // Execute: Kald receiveFile
        TCPClient.receiveFile(destination, mockStream);

        // Assert: Filen skal være mindre end forventet
        assertTrue("File should exist", destination.isFile());
        assertTrue("File should be incomplete (less than 100 bytes)",
                destination.length() < 100);
        assertTrue("File should have some data", destination.length() > 0);

        // Cleanup
        destination.delete();
    }
    @Test(expectedExceptions = SocketException.class)
    public void testServerNotStartedConnectionFails() throws IOException {
        Socket socket = new Socket("localhost", 5000);
        fail("Forventet connection refused, fordi serveren ikke er startet");
    }

}
