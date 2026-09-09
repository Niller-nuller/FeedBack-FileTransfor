import coding.queens.TCPClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class TCPClientTest {

    private static final String HOST = "localhost";
    private static final int PORT = 5000;

    @Test()
    public void testHandleServerResponseThrowsForError() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TCPClient.handleServerResponse("Error|File not found")
        );

        Assertions.assertTrue(exception.getMessage().contains("Error"));
    }

    @Test()
    public void testReceiveFileDestinationFolderNotExist() {
        File destination = new File("src/main/NonExistentFolder/testfile.txt");

        DataInputStream mockStream = new DataInputStream(new ByteArrayInputStream(new byte[] {
                0, 0, 0, 0, 0, 0, 0, 10
        }));

        assertThrows(FileNotFoundException.class, () -> TCPClient.receiveFile(destination, mockStream));
    }

    @Test
    public void testReceiveFileIncompleteData() throws IOException {
        File destination = new File("src/main/ClientFiles/incomplete_test.txt");
        destination.getParentFile().mkdirs();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeLong(100);
        dos.writeBytes("0123456789");

        try (DataInputStream mockStream = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
            TCPClient.receiveFile(destination, mockStream);
        }

        assert(destination.isFile());
        Assertions.assertTrue(destination.length() < 100);
        Assertions.assertTrue(destination.length() > 0);

        destination.delete();
    }

    @Test()
    public void testServerNotStartedConnectionFails() {
        IOException exception = assertThrows(IOException.class, () -> {
            try (Socket socket = new Socket(HOST, PORT)) {
                // just attempting to connect when server is not running should fail
            }
        });

        assertNotNull(exception);
    }
}
