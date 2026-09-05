import coding.queens.TCPServer;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class TCPServerTest {

    private static final String SERVER_DIR = "src/main/ServerFiles/";

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testHandleClientConnection(){
        TCPServer tcpServer = new TCPServer();

    }

    @Test
    public void testFindFile(){
        assertEquals(TCPServer.findFile("test").isFile(), true);
    }
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFindFileError(){
        TCPServer tcpServer = new TCPServer();
        String fileName = "youcantfindthisfile";
        File file = new File("src/main/ServerFiles/youcantfindthisfile");
        IO.println(file.isFile());
        tcpServer.findFile(fileName);
    }
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFindFileError2(){
        String fileName = "/wrongfileformat";
        TCPServer.findFile(fileName);
    }
    @Test
    public void testNormalFileCanBeFound() throws IOException {
        File dir = new File(SERVER_DIR);
        dir.mkdirs();

        File file = new File(SERVER_DIR + "normal.txt");
        Files.write(file.toPath(), "hello from normal file".getBytes(StandardCharsets.UTF_8));

        File found = TCPServer.findFile("normal.txt");

        assertTrue("Fil skal findes", found.isFile());
        assertEquals("normal.txt", found.getName());

        file.delete();
    }
    @Test
    public void testLargeFileCanBeFound() throws IOException {
        File dir = new File(SERVER_DIR);
        dir.mkdirs();

        File largeFile = new File(SERVER_DIR + "large.txt");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 200000; i++) {
            sb.append("Dette er en stor fil test. ");
        }

        Files.write(largeFile.toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));

        File found = TCPServer.findFile("large.txt");

        assertTrue("Stor fil skal findes", found.isFile());
        assertTrue("Stor fil skal have indhold", found.length() > 100000);

        largeFile.delete();
    }
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUnknownFileThrowsException() {
        TCPServer.findFile("ukendt_fil_12345.txt");
    }
}
