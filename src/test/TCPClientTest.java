import coding.queens.TCPClient;
import coding.queens.TCPServer;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

import static org.testng.AssertJUnit.assertEquals;


public class TCPClientTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void TCPClientTest() {
        TCPClient.handleServerResponse("Error");
    }

}
