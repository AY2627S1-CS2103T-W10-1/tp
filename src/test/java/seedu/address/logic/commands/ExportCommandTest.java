package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.CsvContactExporter;

/**
 * Tests CSV export parsing, output or command behavior.
 */
public class ExportCommandTest {
    @TempDir
    public Path testFolder;

    @Test
    public void execute_emptyFilteredList_exportsAllStoredContactsWithoutChangingModel() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.updateFilteredPersonList(person -> false);
        AddressBook original = new AddressBook(model.getAddressBook());
        CommandResult result = command("contacts.csv").execute(model);
        assertEquals(new CommandResult("Exported 7 contacts to contacts.csv."), result);
        assertEquals(original, model.getAddressBook());
        assertTrue(model.getFilteredPersonList().isEmpty());
        String csv = Files.readString(testFolder.resolve("contacts.csv"));
        assertEquals(8, csv.split("\r\n").length);
        for (Person person : original.getPersonList()) {
            assertTrue(csv.contains(person.getName().fullName));
        }
    }

    @Test
    public void execute_noStoredContacts_reportsErrorWithoutFile() {
        Model model = new ModelManager();
        assertThrows(CommandException.class, ExportCommand.MESSAGE_EMPTY_LIST, ()
                -> command("contacts.csv").execute(model));
        assertFalse(Files.exists(testFolder.resolve("contacts.csv")));
    }

    @Test
    public void execute_existingFile_reportsErrorWithoutOverwriting() throws Exception {
        Files.writeString(testFolder.resolve("contacts.csv"), "old content");
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        assertThrows(CommandException.class, String.format(ExportCommand.MESSAGE_ALREADY_EXISTS, "contacts.csv"), ()
                -> command("contacts.csv").execute(model));
        assertEquals("old content", Files.readString(testFolder.resolve("contacts.csv")));
    }

    @Test
    public void execute_permissionOrWriteFailure_reportsErrorWithoutChangingModel() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        AddressBook original = new AddressBook(model.getAddressBook());
        for (IOException failure : List.of(new IOException("Disk full"), new AccessDeniedException("folder"))) {
            CsvContactExporter exporter = new CsvContactExporter(testFolder) {
                @Override
                public void export(List<Person> contacts, String fileName) throws IOException {
                    throw failure;
                }
            };
            ExportCommand command = new ExportCommand("contacts.csv", exporter);
            assertThrows(CommandException.class, String.format(ExportCommand.MESSAGE_WRITE_ERROR, "contacts.csv"), ()
                    -> command.execute(model));
            assertEquals(original, model.getAddressBook());
        }
    }

    @Test
    public void constructorAndExecute_invalidArguments_throw() {
        assertThrows(NullPointerException.class, () -> new ExportCommand(null));
        assertThrows(IllegalArgumentException.class, () -> new ExportCommand("a.txt"));
        assertThrows(NullPointerException.class, () -> new ExportCommand("a.csv", null));
        assertThrows(NullPointerException.class, () -> command("a.csv").execute(null));
    }

    @Test
    public void equalsAndToString() {
        ExportCommand command = command("a.csv");
        assertTrue(command.equals(command));
        assertTrue(command.equals(command("a.csv")));
        assertFalse(command.equals(command("b.csv")));
        assertFalse(command.equals(new ExportCommand("a.csv", new CsvContactExporter(testFolder.resolve("other")))));
        assertFalse(command.equals(null));
        assertFalse(command.equals("a.csv"));
        String expected = ExportCommand.class.getCanonicalName() + "{fileName=a.csv, directory=" + testFolder + "}";
        assertEquals(expected, command.toString());
    }

    private ExportCommand command(String fileName) {
        return new ExportCommand(fileName, new CsvContactExporter(testFolder));
    }
}
