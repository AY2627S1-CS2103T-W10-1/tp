package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests CSV export parsing, output or command behavior.
 */
public class CsvContactExporterTest {
    @TempDir
    public Path testFolder;

    @Test
    public void export_allFieldsAndRepeatedRecords_writesUtf8InOrder() throws Exception {
        Person person = new PersonBuilder().withAddress("北京, \"总部\"").withDepartment("R&D")
                .withPhone("01234567").withTags("zeta", "alpha").build();
        new CsvContactExporter(testFolder).export(List.of(person, ALICE, person), "contacts.csv");
        String csv = Files.readString(testFolder.resolve("contacts.csv"), StandardCharsets.UTF_8);
        String row = "Amy Bee,01234567,amy@gmail.com,R&D,\"北京, \"\"总部\"\"\",alpha; zeta\r\n";
        assertTrue(csv.startsWith("Name,Phone,Email,Department,Address,Tags\r\n" + row));
        assertTrue(csv.contains("Alice Pauline,94351253,alice@example.com,,"));
        assertTrue(csv.endsWith(row));
        assertEquals(4, csv.split("\r\n").length);
        assertOnlyFile("contacts.csv");
    }

    @Test
    public void writeRow_specialAndMissingValues_escapesAndKeepsEmptyCells() throws Exception {
        StringWriter writer = new StringWriter();
        CsvContactExporter.writeRow(writer, List.of("张三", "", "", "", "line1\r\nline2", ""));
        assertEquals("张三,,,,\"line1\r\nline2\",\r\n", writer.toString());
        assertEquals("plain", CsvContactExporter.escapeCell("plain"));
        assertEquals("\"a,b\"", CsvContactExporter.escapeCell("a,b"));
        assertEquals("\"a\"\"b\"", CsvContactExporter.escapeCell("a\"b"));
        assertEquals("\"a\rb\"", CsvContactExporter.escapeCell("a\rb"));
        assertEquals("\"a\nb\"", CsvContactExporter.escapeCell("a\nb"));
    }

    @Test
    public void export_existingDestination_doesNotReplaceIt() throws Exception {
        Path destination = testFolder.resolve("contacts.csv");
        Files.writeString(destination, "existing data");
        CsvContactExporter exporter = new CsvContactExporter(testFolder);
        assertThrows(FileAlreadyExistsException.class, () -> exporter.export(List.of(ALICE), "contacts.csv"));
        assertEquals("existing data", Files.readString(destination));
        assertOnlyFile("contacts.csv");
        Files.delete(destination);
        Files.createDirectory(destination);
        assertThrows(FileAlreadyExistsException.class, () -> exporter.export(List.of(ALICE), "contacts.csv"));
        assertTrue(Files.isDirectory(destination));
    }

    @Test
    public void export_destinationAppearsDuringWrite_doesNotReplaceIt() throws Exception {
        CsvContactExporter exporter = new CsvContactExporter(testFolder) {
            @Override
            protected void writeContacts(Writer writer, List<Person> contacts) throws IOException {
                super.writeContacts(writer, contacts);
                Files.writeString(testFolder.resolve("contacts.csv"), "another export");
            }
        };
        assertThrows(FileAlreadyExistsException.class, () -> exporter.export(List.of(ALICE), "contacts.csv"));
        assertEquals("another export", Files.readString(testFolder.resolve("contacts.csv")));
        assertOnlyFile("contacts.csv");
    }

    @Test
    public void export_writeFails_removesPartialFile() throws Exception {
        CsvContactExporter exporter = new CsvContactExporter(testFolder) {
            @Override
            protected void writeContacts(Writer writer, List<Person> contacts) throws IOException {
                writer.write("partial data");
                throw new IOException("Simulated disk failure");
            }
        };
        assertThrows(IOException.class, () -> exporter.export(List.of(ALICE), "contacts.csv"));
        try (var entries = Files.list(testFolder)) {
            assertEquals(0, entries.count());
        }
    }

    @Test
    public void export_unavailableDirectory_throwsIoException() {
        CsvContactExporter exporter = new CsvContactExporter(testFolder.resolve("missing"));
        assertThrows(IOException.class, () -> exporter.export(List.of(ALICE), "contacts.csv"));
        assertFalse(Files.exists(testFolder.resolve("missing")));
    }

    @Test
    public void isValidFileName_portableBasenames() {
        for (String fileName : new String[]{"contacts.csv", "my contacts.csv", "联系人.csv", ".hidden.csv",
            "com10.csv", "contact's.csv"}) {
            assertTrue(CsvContactExporter.isValidFileName(fileName), fileName);
        }
        for (String fileName : new String[]{"", ".csv", "a.csv ", " a.csv", "a.csv.", "a\u0000.csv",
            "a\n.csv", "a\t.csv", "NUL.csv", "CON .csv", "LPT1.backup.csv", "COM³.csv"}) {
            assertFalse(CsvContactExporter.isValidFileName(fileName), fileName);
        }
        assertFalse(CsvContactExporter.isValidFileName(null));
    }

    @Test
    public void export_invalidArguments_rejectedBeforeCreatingFiles() {
        CsvContactExporter exporter = new CsvContactExporter(testFolder);
        assertThrows(NullPointerException.class, () -> exporter.export(null, "contacts.csv"));
        assertThrows(IllegalArgumentException.class, () -> exporter.export(List.of(ALICE), "../a.csv"));
        assertThrows(NullPointerException.class, () -> new CsvContactExporter(null));
    }

    private void assertOnlyFile(String expectedName) throws IOException {
        try (var entries = Files.list(testFolder)) {
            assertEquals(List.of(testFolder.resolve(expectedName)), entries.toList());
        }
    }
}
