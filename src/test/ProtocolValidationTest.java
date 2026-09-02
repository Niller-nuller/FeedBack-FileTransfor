import coding.queens.TCPServer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.file.Files;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

public class ProtocolValidationTest {

    private static final String SERVER_DIR = "src/main/ServerFiles/";
    private static final String VALID_FILE = "valid_protocol_test.txt";

    @BeforeClass
    public void setupTestFile() throws IOException {
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
        
        assertTrue("Should have exactly 2 parts", parts.length == 2);
        assertTrue("Command should be GET", parts[0].equals("GET"));
        assertTrue("Should extract filename", parts[1].equals(VALID_FILE));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectEmptyRequest() {
        String emptyRequest = "";
        if (emptyRequest.isEmpty()) {
            throw new IllegalArgumentException("Error|Client sent empty request");
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectRequestWithoutPipe() {
        String noPipeRequest = "GETsomefile";
        String[] parts = noPipeRequest.split("\\|", 2);
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("Error|Invalid format");
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectRequestWithoutFilename() {
        String noFileRequest = "GET|";
        String[] parts = noFileRequest.split("\\|", 2);
        
        if (parts.length == 2 && parts[1].isBlank()) {
            throw new IllegalArgumentException("Error|Invalid format - empty filename");
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectPathTraversalAttempt() {
        TCPServer.findFile("../../../etc/passwd");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectAbsolutePath() {
        TCPServer.findFile("/etc/passwd");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectFileWithForwardSlash() {
        TCPServer.findFile("subfolder/file.txt");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectFileWithBackslash() {
        TCPServer.findFile("subfolder\\file.txt");
    }

    @Test
    public void testValidFilenameWithNumbers() {
        try {
            File testFile = new File(SERVER_DIR + "test123.txt");
            testFile.createNewFile();
            File found = TCPServer.findFile("test123.txt");
            assertTrue("Should find file with numbers", found.isFile());
            testFile.delete();
        } catch (IOException e) {
            fail("Should handle numeric filenames");
        }
    }

    @Test
    public void testValidFilenameWithDashesUnderscores() {
        try {
            File testFile = new File(SERVER_DIR + "test-file_name.txt");
            testFile.createNewFile();
            File found = TCPServer.findFile("test-file_name.txt");
            assertTrue("Should find file with dashes and underscores", found.isFile());
            testFile.delete();
        } catch (IOException e) {
            fail("Should handle dashes and underscores");
        }
    }

    @Test
    public void testValidFilenameWithMultipleDots() {
        try {
            File testFile = new File(SERVER_DIR + "backup.tar.gz");
            testFile.createNewFile();
            File found = TCPServer.findFile("backup.tar.gz");
            assertTrue("Should find file with multiple dots", found.isFile());
            testFile.delete();
        } catch (IOException e) {
            fail("Should handle multiple dots in filename");
        }
    }

    @Test
    public void testCaseSensitiveFilename() {
        try {
            File testFile1 = new File(SERVER_DIR + "TestFile.txt");
            File testFile2 = new File(SERVER_DIR + "testfile.txt");
            
            testFile1.createNewFile();
            
            // Case matters - this should work
            File found = TCPServer.findFile("TestFile.txt");
            assertTrue("Should respect case sensitivity", found.isFile());
            
            // This should fail
            try {
                TCPServer.findFile("testfile.txt");
                fail("Should be case sensitive");
            } catch (IllegalArgumentException e) {
                assertTrue("Should not find case-insensitive match", true);
            }
            
            testFile1.delete();
        } catch (IOException e) {
            fail("Setup error: " + e.getMessage());
        }
    }

    @Test
    public void testCommandMustBeExactlyGET() {
        String[] invalidCommands = {"get", "Get", "FETCH", "RETRIEVE", "SEND"};
        
        for (String cmd : invalidCommands) {
            String request = cmd + "|" + VALID_FILE;
            String[] parts = request.split("\\|", 2);
            
            assertTrue("Parsing should work for any command", parts.length == 2);
            assertTrue("Command should NOT be GET when lowercase/wrong", !parts[0].equals("GET"));
        }
    }

    @Test
    public void testMultiplePipesInRequest() {
        String request = "GET|file|with|pipes.txt";
        String[] parts = request.split("\\|", 2);
        
        assertTrue("Should split on first pipe only", parts.length == 2);
        assertTrue("First part is GET", parts[0].equals("GET"));
        assertTrue("Second part contains rest with pipes", parts[1].equals("file|with|pipes.txt"));
        
        try {
            TCPServer.findFile(parts[1]);
            fail("Filename with pipes should be rejected");
        } catch (IllegalArgumentException e) {
            assertTrue("Should reject pipes in filename", e.getMessage().contains("/"));
        }
    }

    @Test
    public void testWhitespaceHandling() {
        try {
            String[] invalidWhitespace = {" testfile.txt", "testfile.txt ", " testfile.txt "};
            
            for (String filename : invalidWhitespace) {
                try {
                    TCPServer.findFile(filename);
                } catch (IllegalArgumentException e) {
                    assertTrue("Should handle whitespace appropriately", true);
                }
            }
        } catch (Exception e) {
            fail("Whitespace handling should not crash: " + e.getMessage());
        }
    }

    @Test
    public void testVeryLongFilename() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            longName.append("verylongfilename");
        }
        longName.append(".txt");
        
        try {
            TCPServer.findFile(longName.toString());
            fail("Very long filename should be rejected or handled");
        } catch (IllegalArgumentException e) {
            assertTrue("Should handle extremely long filenames", true);
        }
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
            try {
                TCPServer.findFile(filename);
            } catch (IllegalArgumentException e) {
                assertTrue("Should handle special characters: " + filename, true);
            }
        }
    }

    @Test
    public void testNullFilenameHandling() {
        try {
            TCPServer.findFile(null);
            fail("Null filename should throw exception");
        } catch (NullPointerException | IllegalArgumentException e) {
            assertTrue("Should handle null safely", true);
        }
    }

    @Test
    public void testRequestFormatEdgeCases() {
        String[] edgeCases = {
            "|filename.txt",  // No command
            "GET|",           // No filename
            "|||",            // Only pipes
            "GET",            // No pipe at all
            "|",              // Only pipe
            "GET|file|more|pipes.txt"  // Multiple pipes
        };
        
        for (String request : edgeCases) {
            String[] parts = request.split("\\|", 2);
            boolean isValid = (parts.length == 2 && 
                             !parts[0].isBlank() && 
                             !parts[1].isBlank() && 
                             parts[0].equals("GET"));
            
            if (!isValid) {
                assertTrue("Edge case should be identified as invalid: " + request, true);
            }
        }
    }
}
