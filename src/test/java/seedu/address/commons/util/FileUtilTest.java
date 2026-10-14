package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {
    @TempDir
    public Path testFolder;

    @Test
    public void createIfMissing_createsParentDirectoriesAndPreservesExistingContent() throws Exception {
        Path file = testFolder.resolve("nested/folder/contacts.txt");
        FileUtil.createIfMissing(file);
        assertTrue(Files.isRegularFile(file));
        FileUtil.writeToFile(file, "Alice Tan");
        FileUtil.createIfMissing(file);
        assertEquals("Alice Tan", FileUtil.readFromFile(file));
    }

    @Test
    public void createIfMissing_relativeBasename_doesNotRequireParentDirectory() throws Exception {
        Path file = Path.of("tp-fileutil-test-" + UUID.randomUUID() + ".txt");
        try {
            FileUtil.createIfMissing(file);
            assertTrue(Files.isRegularFile(file));
        } finally {
            Files.deleteIfExists(file);
        }
    }
}
