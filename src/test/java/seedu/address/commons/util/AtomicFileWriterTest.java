package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AtomicFileWriterTest {
    private static final String ORIGINAL = "{\"contacts\":\"original\"}";
    private static final String REPLACEMENT = "{\"contacts\":\"replacement\"}";

    @TempDir
    public Path testFolder;

    @Test
    public void write_existingFile_publishesOnlyCompleteContent() throws Exception {
        Path destination = testFolder.resolve("contacts.json");
        Files.writeString(destination, ORIGINAL);
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            protected void replaceFile(Path temporary, Path target) throws IOException {
                assertEquals(destination, target);
                assertEquals(target.getParent(), temporary.getParent());
                assertEquals(ORIGINAL, Files.readString(target));
                assertEquals(REPLACEMENT, Files.readString(temporary));
                super.replaceFile(temporary, target);
            }
        };

        writer.write(destination, REPLACEMENT);

        assertEquals(REPLACEMENT, Files.readString(destination));
        assertOnlyDestination(testFolder, destination);
    }

    @Test
    public void write_missingParent_createsDirectoriesAndUtf8File() throws Exception {
        Path destination = testFolder.resolve("nested/folder/contacts.json");
        String content = "{\"address\":\"北京\"}";

        new AtomicFileWriter().write(destination, content);

        assertArrayEquals(content.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(destination));
        assertOnlyDestination(destination.getParent(), destination);
    }

    @Test
    public void write_relativeBasename_createsDestination() throws Exception {
        Path destination = Path.of("tp-atomic-save-test-" + UUID.randomUUID() + ".json");
        try {
            new AtomicFileWriter().write(destination, REPLACEMENT);
            assertEquals(REPLACEMENT, Files.readString(destination));
        } finally {
            Files.deleteIfExists(destination);
        }
    }

    @Test
    public void write_temporaryCreationFailure_preservesDestination() throws Exception {
        IOException failure = new IOException("Cannot create temporary file");
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            protected Path createTemporaryFile(Path destination) throws IOException {
                throw failure;
            }
        };
        assertFailedSavePreservesDestination(writer, failure);
    }

    @Test
    public void write_interruptedTemporaryWrite_preservesDestinationAndRemovesPartialFile() throws Exception {
        IOException failure = new IOException("Interrupted write");
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            protected void writeTemporaryFile(Path temporary, String content) throws IOException {
                Files.writeString(temporary, "{");
                throw failure;
            }
        };
        assertFailedSavePreservesDestination(writer, failure);
    }

    @Test
    public void write_replacementFailure_preservesDestinationAndRemovesTemporaryFile() throws Exception {
        IOException failure = new IOException("Cannot replace destination");
        assertFailedSavePreservesDestination(rejectingReplacement(failure), failure);
    }

    @Test
    public void write_atomicMoveUnsupported_failsWithoutUnsafeFallback() throws Exception {
        IOException failure = new AtomicMoveNotSupportedException("temporary", "contacts.json", "Unsupported");
        assertFailedSavePreservesDestination(rejectingReplacement(failure), failure);
    }

    @Test
    public void write_parentIsFile_preservesBlockingFile() throws Exception {
        Path parent = testFolder.resolve("blocked");
        Files.writeString(parent, ORIGINAL);

        assertThrows(IOException.class, () ->
                new AtomicFileWriter().write(parent.resolve("contacts.json"), REPLACEMENT));

        assertEquals(ORIGINAL, Files.readString(parent));
        assertOnlyDestination(testFolder, parent);
    }

    @Test
    public void write_cleanupFailure_preservesOriginalException() throws Exception {
        IOException failure = new IOException("Original write failure");
        AtomicFileWriter writer = new AtomicFileWriter() {
            @Override
            protected Path createTemporaryFile(Path destination) throws IOException {
                return Files.createTempDirectory(destination.getParent(), "cleanup-failure-");
            }

            @Override
            protected void writeTemporaryFile(Path temporary, String content) throws IOException {
                Files.writeString(temporary.resolve("blocked.txt"), content);
                throw failure;
            }
        };
        Path destination = testFolder.resolve("contacts.json");
        Files.writeString(destination, ORIGINAL);

        assertSame(failure, assertThrows(IOException.class, () -> writer.write(destination, REPLACEMENT)));

        assertEquals(ORIGINAL, Files.readString(destination));
        assertEquals(1, failure.getSuppressed().length);
        assertTrue(failure.getSuppressed()[0] instanceof DirectoryNotEmptyException);
    }

    private AtomicFileWriter rejectingReplacement(IOException failure) {
        return new AtomicFileWriter() {
            @Override
            protected void replaceFile(Path temporary, Path destination) throws IOException {
                assertEquals(REPLACEMENT, Files.readString(temporary));
                throw failure;
            }
        };
    }

    private void assertFailedSavePreservesDestination(AtomicFileWriter writer, IOException failure) throws Exception {
        for (boolean existing : List.of(false, true)) {
            Path directory = Files.createTempDirectory(testFolder, "save-case-");
            Path destination = directory.resolve("contacts.json");
            if (existing) {
                Files.writeString(destination, ORIGINAL);
            }

            assertSame(failure, assertThrows(IOException.class, () -> writer.write(destination, REPLACEMENT)));

            if (existing) {
                assertArrayEquals(ORIGINAL.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(destination));
                assertOnlyDestination(directory, destination);
            } else {
                assertFalse(Files.exists(destination));
                try (var entries = Files.list(directory)) {
                    assertEquals(0, entries.count());
                }
            }
        }
    }

    private void assertOnlyDestination(Path directory, Path destination) throws IOException {
        try (var entries = Files.list(directory)) {
            assertEquals(List.of(destination), entries.toList());
        }
    }
}
