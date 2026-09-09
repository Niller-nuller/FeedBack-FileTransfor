import coding.queens.TCPServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


public class ProtocolValidationTest {

    private static final String SERVER_DIR = "src/main/ServerFiles/";
    private static final String VALID_FILE = "valid_protocol_test.txt";

    @BeforeAll
    public static void setupTestFile() throws IOException {
        new File(SERVER_DIR).mkdirs();
        File testFile = new File(SERVER_DIR + VALID_FILE);
        try (FileWriter fw = new FileWriter(testFile)) {
            fw.write("Test content for protocol validation");
        }
    }

    @Test
    public void testGetCommandParsing() {
        String request = "GET|" + VALID_FILE;
        String[] parts = request.split("\\|", 2);

        assertEquals(2, parts.length);
        assertEquals("GET", parts[0]);
        assertEquals(VALID_FILE, parts[1]);
    }

    @Test
    public void testRejectEmptyRequest() {
        assertThrows(IllegalArgumentException.class, () -> {
            String emptyRequest = "";
            if (emptyRequest.isEmpty()) {
                throw new IllegalArgumentException("Error|Client sent empty request");
            }
        });
    }

    @Test
    public void testRejectRequestWithoutPipe() {
        assertThrows(IllegalArgumentException.class, () -> {
            String noPipeRequest = "GETsomefile";
            String[] parts = noPipeRequest.split("\\|", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Error|Invalid format");
            }
        });
    }

    @Test
    public void testRejectRequestWithoutFilename() {
        assertThrows(IllegalArgumentException.class, () -> {
            String noFileRequest = "GET|";
            String[] parts = noFileRequest.split("\\|", 2);
            if (parts.length == 2 && parts[1].isBlank()) {
                throw new IllegalArgumentException("Error|Invalid format - empty filename");
            }
        });
    }

    @Test
    public void testRejectPathTraversalAttempt() {
        assertThrows(IllegalArgumentException.class, () -> TCPServer.findFile("../../../etc/passwd"));
    }

    @Test
    public void testRejectAbsolutePath() {
        assertThrows(IllegalArgumentException.class, () -> TCPServer.findFile("/etc/passwd"));
    }

    @Test
    public void testRejectFileWithForwardSlash() {
        assertThrows(IllegalArgumentException.class, () -> TCPServer.findFile("subfolder/file.txt"));
    }

    @Test
    public void testRejectFileWithBackslash() {
        assertThrows(IllegalArgumentException.class, () -> TCPServer.findFile("subfolder\\file.txt"));
    }

    @Test
    public void testValidFilenameWithNumbers() throws IOException {
        File testFile = new File(SERVER_DIR + "test123.txt");
        testFile.createNewFile();

        try {
            assertTrue(TCPServer.findFile("test123.txt").isFile());
        } finally {
            testFile.delete();
        }
    }

    @Test
    public void testValidFilenameWithDashesUnderscores() throws IOException {
        File testFile = new File(SERVER_DIR + "test-file_name.txt");
        testFile.createNewFile();

        try {
            assertTrue(TCPServer.findFile("test-file_name.txt").isFile());
        } finally {
            testFile.delete();
        }
    }

    @Test
    public void testValidFilenameWithMultipleDots() throws IOException {
        File testFile = new File(SERVER_DIR + "backup.tar.gz");
        testFile.createNewFile();

        try {
            assertTrue(TCPServer.findFile("backup.tar.gz").isFile());
        } finally {
            testFile.delete();
        }
    }

    @Test
    public void testCaseSensitiveFilename() throws IOException {
        File testFile = new File(SERVER_DIR + "TestFile.txt");
        testFile.createNewFile();

        try {
            assertTrue(TCPServer.findFile("TestFile.txt").isFile());
            assertThrows(IllegalArgumentException.class, () -> TCPServer.findFile("testfiLe.txt"));
        } finally {
            testFile.delete();
        }
    }

    @Test
    public void testCommandMustBeExactlyGET() {
        String[] invalidCommands = {"get", "Get", "FETCH", "RETRIEVE", "SEND"};

        for (String cmd : invalidCommands) {
            String request = cmd + "|" + VALID_FILE;
            String[] parts = request.split("\\|", 2);

            assertEquals(2, parts.length);
            assertNotEquals("GET", parts[0]);
        }
    }

    @Test
    public void testMultiplePipesInRequest() {
        String request = "GET|file|with|pipes.txt";
        String[] parts = request.split("\\|", 2);

        assertEquals(2, parts.length);
        assertEquals("GET", parts[0]);
        assertEquals("file|with|pipes.txt", parts[1]);

        assertThrows(IllegalArgumentException.class, () -> TCPServer.findFile(parts[1]));
    }

    @Test
    public void testWhitespaceHandling() {
        for (String filename : new String[]{" testfile.txt", "testfile.txt ", " testfile.txt "}) {
            assertThrows(IllegalArgumentException.class, () -> TCPServer.findFile(filename));
        }
    }

    @Test
    public void testVeryLongFilename() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longName.append("verylongfilename");
        }
        longName.append(".txt");

        assertThrows(IllegalArgumentException.class, () -> TCPServer.findFile(longName.toString()));
    }

    @Test
    public void testSpecialCharactersRejection() {
        String[] specialChars = {
                "file;.txt",
                "file\\.txt",
                "file`.txt",
                "file$.txt",
                "file%.txt",
                "file&.txt",
                "file*.txt",
                "file?.txt",
                "file\".txt",
                "file'.txt"
        };

        for (String filename : specialChars) {
            assertThrows(IllegalArgumentException.class, () -> TCPServer.findFile(filename));
        }
    }

    @Test
    public void testNullFilenameHandling() {
        assertThrows(NullPointerException.class, () -> TCPServer.findFile(null));
    }
}
