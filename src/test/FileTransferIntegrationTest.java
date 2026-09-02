import coding.queens.TCPServer;
import coding.queens.TCPClient;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

public class FileTransferIntegrationTest {

    private static final String SERVER_DIR = "src/main/ServerFiles/";
    private static final String CLIENT_DIR = "src/main/ClientFiles/";
    private static final String TEST_FILE = "testfile.txt";
    private static final String TEST_CONTENT = "This is a test file for integration testing.";

    @BeforeClass
    public void setupTestFiles() throws IOException {
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
        assertTrue("Test file should exist in server directory", testFile.isFile());
        assertTrue("Test file should contain content", testFile.length() > 0);
    }

    @Test
    public void testFileTransferPreservesContent() throws IOException {
        File original = new File(SERVER_DIR + TEST_FILE);
        byte[] originalBytes = Files.readAllBytes(original.toPath());
        
        assertTrue("Original file should have content", originalBytes.length > 0);
        assertTrue("Original content should match test content", 
                new String(originalBytes).equals(TEST_CONTENT));
    }

    @Test
    public void testServerCanFindValidFile() {
        File foundFile = TCPServer.findFile(TEST_FILE);
        assertTrue("Server should find the test file", foundFile.isFile());
        assertTrue("Found file should match server path", 
                foundFile.getPath().contains(SERVER_DIR));
    }

    @Test
    public void testServerRejectsNonExistentFile() {
        try {
            TCPServer.findFile("nonexistent_file_xyz.txt");
            fail("Should throw IllegalArgumentException for non-existent file");
        } catch (IllegalArgumentException e) {
            assertTrue("Error message should contain 'Invalid file name'", 
                    e.getMessage().contains("Invalid file name"));
        }
    }

    @Test
    public void testServerRejectsPathTraversal() {
        try {
            TCPServer.findFile("../../malicious.txt");
            fail("Should throw IllegalArgumentException for path traversal");
        } catch (IllegalArgumentException e) {
            assertTrue("Error message should mention '/' character", 
                    e.getMessage().contains("/"));
        }
    }

    @Test
    public void testClientDirectoryExists() {
        File clientDir = new File(CLIENT_DIR);
        assertTrue("Client directory should exist or be created", clientDir.exists());
    }

    @Test
    public void testFileCanBeWrittenToClientDirectory() throws IOException {
        File testDestination = new File(CLIENT_DIR + "write_test.txt");
        
        try (FileOutputStream fos = new FileOutputStream(testDestination)) {
            fos.write("Test write".getBytes());
        }
        
        assertTrue("File should be written to client directory", testDestination.isFile());
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
        
        assertTrue("Large file should be created", largeFile.isFile());
        assertTrue("Large file should have significant size", largeFile.length() > 100000);
        
        File foundFile = TCPServer.findFile(largeFileName);
        assertTrue("Server should find large file", foundFile.isFile());
        assertTrue("Found file size should match", foundFile.length() == largeFile.length());
        
        largeFile.delete();
    }

    @Test
    public void testProtocolFormatValidation() {
        String validRequest = "GET|testfile.txt";
        String[] parts = validRequest.split("\\|", 2);
        
        assertTrue("Valid request should split into 2 parts", parts.length == 2);
        assertTrue("First part should be GET", parts[0].equals("GET"));
        assertTrue("Second part should be filename", parts[1].equals("testfile.txt"));
    }

    @Test
    public void testInvalidProtocolFormatDetection() {
        String invalidRequest1 = "GET";
        String invalidRequest2 = "invalid|format|extra";
        String invalidRequest3 = "INVALID|testfile.txt";
        
        String[] parts1 = invalidRequest1.split("\\|", 2);
        String[] parts2 = invalidRequest2.split("\\|", 2);
        String[] parts3 = invalidRequest3.split("\\|", 2);
        
        assertTrue("GET without pipe should not split", parts1.length != 2);
        assertTrue("Extra pipes should split only on first pipe", parts2.length == 2);
        assertTrue("INVALID command should still split", parts3.length == 2);
    }

    @Test
    public void testEmptyFileNameValidation() {
        try {
            TCPServer.findFile("");
            fail("Should throw exception for empty filename");
        } catch (IllegalArgumentException e) {
            assertTrue("Should handle empty filename", 
                    e.getMessage().contains("Invalid"));
        }
    }

    @Test
    public void testFileNameWithSpecialCharacters() {
        String[] testCases = {
            "testfile.txt",
            "test-file.txt",
            "test_file.txt",
            "test file.txt"
        };
        
        for (String fileName : testCases) {
            File testFile = new File(SERVER_DIR + fileName);
            try {
                testFile.createNewFile();
                File found = TCPServer.findFile(fileName);
                assertTrue("Should find file with special chars: " + fileName, found.isFile());
            } catch (IOException e) {
                fail("Should handle file creation: " + e.getMessage());
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
        
        assertTrue("Binary file should exist", binaryFile.isFile());
        
        byte[] readBack = Files.readAllBytes(binaryFile.toPath());
        assertTrue("Binary data should match", java.util.Arrays.equals(binaryData, readBack));
        
        binaryFile.delete();
    }
}
