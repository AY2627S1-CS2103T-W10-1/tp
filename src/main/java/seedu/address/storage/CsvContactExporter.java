package seedu.address.storage;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import seedu.address.model.person.Person;

/**
 * Writes a complete UTF-8 CSV snapshot without replacing an existing destination.
 */
public class CsvContactExporter {
    private static final Pattern INVALID_CHARACTERS = Pattern.compile("[<>:\"/\\\\|?*\\p{Cntrl}]");
    private static final Pattern RESERVED_NAME = Pattern.compile("(?i)(CON|PRN|AUX|NUL|COM[1-9¹²³]|LPT[1-9¹²³])");
    private static final List<String> HEADERS = List.of("Name", "Phone", "Email", "Department", "Address", "Tags");
    private final Path directory;

    public CsvContactExporter(Path directory) {
        this.directory = requireNonNull(directory).toAbsolutePath().normalize();
    }

    /**
     * Accepts a portable .csv basename, rather than a directory path.
     */
    public static boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.length() <= 4 || !fileName.endsWith(".csv")
                || !fileName.equals(fileName.strip()) || INVALID_CHARACTERS.matcher(fileName).find()) {
            return false;
        }
        String stem = fileName.split("\\.", 2)[0].stripTrailing();
        return !RESERVED_NAME.matcher(stem).matches();
    }

    public Path getDirectory() {
        return directory;
    }

    /**
     * Publishes the CSV only after all rows have been written and the writer has closed.
     */
    public void export(List<Person> contacts, String fileName) throws IOException {
        requireNonNull(contacts);
        checkArgument(isValidFileName(fileName), "Invalid CSV filename");
        Path destination = directory.resolve(fileName);
        if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) {
            throw new FileAlreadyExistsException(destination.toString());
        }
        Path temporary = Files.createTempFile(directory, ".contacts-export-", ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                writeContacts(writer, contacts);
            }
            Files.move(temporary, destination);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /**
     * Writes every input contact in order, including repeated records.
     */
    protected void writeContacts(Writer writer, List<Person> contacts) throws IOException {
        writeRow(writer, HEADERS);
        for (Person person : contacts) {
            String tags = person.getTags().stream().map(tag -> tag.tagName).sorted().collect(Collectors.joining("; "));
            writeRow(writer, List.of(person.getName().fullName, person.getPhone().value, person.getEmail().value,
                    person.getDepartment().map(value -> value.value).orElse(""), person.getAddress().value, tags));
        }
    }

    static void writeRow(Writer writer, List<String> values) throws IOException {
        writer.write(values.stream().map(CsvContactExporter::escapeCell).collect(Collectors.joining(",")));
        writer.write("\r\n");
    }

    static String escapeCell(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\r") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
