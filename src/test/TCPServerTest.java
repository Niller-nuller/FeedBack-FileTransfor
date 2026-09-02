import coding.queens.TCPServer;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import static org.testng.AssertJUnit.assertEquals;

public class TCPServerTest {

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
}
