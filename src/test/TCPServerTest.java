import coding.queens.TCPServer;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class TCPServerTest {

    private static final String SERVER_DIR = "src/main/ServerFiles/";

    @Test
    public void testFindFile() throws IOException {
        File dir = new File(SERVER_DIR);
        dir.mkdirs();

        File file = new File(SERVER_DIR + "test.txt");
        Files.write(file.toPath(), "hello".getBytes(StandardCharsets.UTF_8));

        assertTrue(TCPServer.findFile("test.txt").isFile());
        file.delete();
    }

    @Test
    public void testFindFileError() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TCPServer.findFile("youcantfindthisfile")
        );

        assertTrue(exception.getMessage().contains("Invalid file name"));
    }

    @Test
    public void testFindFileError2() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TCPServer.findFile("/wrongfileformat")
        );

        assertTrue(exception.getMessage().contains("/"));
    }
}
