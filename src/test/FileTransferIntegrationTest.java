import coding.queens.TCPServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class FileTransferIntegrationTest {

    private static final String SERVER_DIR = "src/main/ServerFiles/";
    private static final String CLIENT_DIR = "src/main/ClientFiles/";
    private static final String TEST_FILE = "testfile.txt";
    private static final String TEST_CONTENT = "This is a test file for integration testing.";

    @BeforeAll
    public static void setupTestFiles() throws IOException {
        new File(SERVER_DIR).mkdirs();
        new File(CLIENT_DIR).mkdirs();

        File testFile = new File(SERVER_DIR + TEST_FILE);
        try (FileWriter fw = new FileWriter(testFile)) {
            fw.write(TEST_CONTENT);
        }
    }

    @Test
    public void testSuccessfulFileDownload() {
        File testFile = new File(SERVER_DIR + TEST_FILE);
        assertTrue(testFile.isFile());
        assertTrue(testFile.length() > 0);
    }

    @Test
    public void testFileTransferPreservesContent() throws IOException {
        File original = new File(SERVER_DIR + TEST_FILE);
        byte[] originalBytes = Files.readAllBytes(original.toPath());

        assertTrue(originalBytes.length > 0);
        assertEquals(TEST_CONTENT, new String(originalBytes));
    }

    @Test
     void testServerCanFindValidFile() {
        File foundFile = TCPServer.findFile(TEST_FILE);
        assertTrue(foundFile.isFile());
    }

    @Test
    void testServerRejectsNonExistentFile() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TCPServer.findFile("nonexistent_file_xyz.txt")
        );
        assertTrue(exception.getMessage().contains("Invalid file name"));
    }

    @Test
    public void testServerRejectsPathTraversal() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TCPServer.findFile("../../malicious.txt")
        );
        assertTrue(exception.getMessage().contains("/"));
    }

    @Test
    public void testClientDirectoryExists() {
        File clientDir = new File(CLIENT_DIR);
        assertTrue(clientDir.exists());
    }

    @Test
    public void testFileCanBeWrittenToClientDirectory() throws IOException {
        File testDestination = new File(CLIENT_DIR + "write_test.txt");

        try (FileOutputStream fos = new FileOutputStream(testDestination)) {
            fos.write("Test write".getBytes());
        }

        assertTrue(testDestination.isFile());
        testDestination.delete();
    }

    @Test
    public void testLargeFileHandling() throws IOException {
        String largeFileName = "large_test_file.txt";
        File largeFile = new File(SERVER_DIR + largeFileName);

        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeContent.append("Line ").append(i).append(": This is test data for large file transfer.\n");
        }

        try (FileWriter fw = new FileWriter(largeFile)) {
            fw.write(largeContent.toString());
        }

        assertTrue(largeFile.isFile());
        assertTrue(largeFile.length() > 100000);

        File foundFile = TCPServer.findFile(largeFileName);
        assertTrue(foundFile.isFile());
        assertEquals(largeFile.length(), foundFile.length());

        largeFile.delete();
    }

    @Test
    public void testProtocolFormatValidation() {
        String validRequest = "GET|testfile.txt";
        String[] parts = validRequest.split("\\|", 2);

        assertEquals(2, parts.length);
        assertEquals("GET", parts[0]);
        assertEquals("testfile.txt", parts[1]);
    }

    @Test
    public void testInvalidProtocolFormatDetection() {
        String invalidRequest1 = "GET";
        String invalidRequest2 = "invalid|format|extra";
        String invalidRequest3 = "INVALID|testfile.txt";

        String[] parts1 = invalidRequest1.split("\\|", 2);
        String[] parts2 = invalidRequest2.split("\\|", 2);
        String[] parts3 = invalidRequest3.split("\\|", 2);

        assertNotEquals(2, parts1.length);
        assertEquals(2, parts2.length);
        assertEquals(2, parts3.length);
    }

    @Test
    public void testEmptyFileNameValidation() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TCPServer.findFile("")
        );
        assertTrue(exception.getMessage().contains("Invalid"));
    }

    @Test
    public void testFileNameWithSpecialCharacters() throws IOException {
        String[] testCases = {"testfile.txt", "test-file.txt", "test_file.txt", "test file.txt"};

        for (String fileName : testCases) {
            File testFile = new File(SERVER_DIR + fileName);
            try {
                testFile.createNewFile();
                File found = TCPServer.findFile(fileName);
                assertTrue(found.isFile());
            } finally {
                testFile.delete();
            }
        }
    }

    @Test
    public void testBinaryFileSupport() throws IOException {
        String binaryFileName = "test_binary.bin";
        File binaryFile = new File(SERVER_DIR + binaryFileName);

        byte[] binaryData = new byte[256];
        for (int i = 0; i < 256; i++) {
            binaryData[i] = (byte) i;
        }

        try (FileOutputStream fos = new FileOutputStream(binaryFile)) {
            fos.write(binaryData);
        }

        assertTrue(binaryFile.isFile());
        byte[] readBack = Files.readAllBytes(binaryFile.toPath());
        assertArrayEquals(binaryData, readBack);

        binaryFile.delete();
    }
}
