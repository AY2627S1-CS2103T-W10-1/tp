package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Publishes complete UTF-8 files through atomic replacement, preserving existing data when saving fails.
 */
public class AtomicFileWriter {

    /**
     * Writes all content to a sibling temporary file before atomically replacing the destination.
     * Rejects read-only destinations and does not fall back to a non-atomic replacement.
     *
     * @param file Destination to create or replace.
     * @param content Complete content to save.
     * @throws IOException If temporary-file creation, writing, or atomic replacement fails.
     */
    public void write(Path file, String content) throws IOException {
        requireNonNull(file);
        requireNonNull(content);
        Path destination = file.toAbsolutePath();
        Files.createDirectories(destination.getParent());
        if (Files.exists(destination) && !Files.isWritable(destination)) {
            throw new AccessDeniedException(destination.toString());
        }
        Path temporary = createTemporaryFile(destination);
        try {
            writeTemporaryFile(temporary, content);
            replaceFile(temporary, destination);
        } catch (IOException | RuntimeException failure) {
            discardTemporaryFile(temporary, failure);
            throw failure;
        }
    }

    /**
     * Creates a temporary file on the destination's filesystem so replacement can be atomic.
     */
    protected Path createTemporaryFile(Path destination) throws IOException {
        return Files.createTempFile(destination.getParent(), ".sudocontact-save-", ".tmp");
    }

    /**
     * Writes and closes the temporary file before it is published.
     */
    protected void writeTemporaryFile(Path temporary, String content) throws IOException {
        Files.writeString(temporary, content, StandardCharsets.UTF_8);
    }

    /**
     * Atomically replaces the destination, failing if the filesystem cannot support this operation.
     */
    protected void replaceFile(Path temporary, Path destination) throws IOException {
        Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    }

    private void discardTemporaryFile(Path temporary, Exception failure) {
        try {
            Files.deleteIfExists(temporary);
        } catch (IOException | RuntimeException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }
}
