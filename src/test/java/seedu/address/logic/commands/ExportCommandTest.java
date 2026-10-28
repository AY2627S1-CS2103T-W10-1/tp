package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.CsvContactExporter;
import seedu.address.testutil.LogCapture;

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
    public void execute_concurrentExports_onlyOneSucceedsAndKeepsItsCompleteCsv() throws Exception {
        AddressBook firstBook = new AddressBook();
        firstBook.addPerson(ALICE);
        firstBook.addPerson(BOB);
        AddressBook secondBook = new AddressBook();
        secondBook.addPerson(BOB);
        Model firstModel = new ModelManager(firstBook, new UserPrefs());
        Model secondModel = new ModelManager(secondBook, new UserPrefs());
        firstModel.updateFilteredPersonList(person -> false);
        secondModel.updateFilteredPersonList(person -> false);

        command("first.csv").execute(firstModel);
        command("second.csv").execute(secondModel);
        String firstCsv = Files.readString(testFolder.resolve("first.csv"));
        String secondCsv = Files.readString(testFolder.resolve("second.csv"));
        Files.delete(testFolder.resolve("first.csv"));
        Files.delete(testFolder.resolve("second.csv"));

        CyclicBarrier readyToPublish = new CyclicBarrier(2);
        ExportCommand first = new ExportCommand("contacts.csv", concurrentExporter(readyToPublish));
        ExportCommand second = new ExportCommand("contacts.csv", concurrentExporter(readyToPublish));
        try (var executor = Executors.newFixedThreadPool(2)) {
            for (int attempt = 0; attempt < 100; attempt++) {
                var firstExport = executor.submit(() -> executeOrAssertConflict(first, firstModel));
                var secondExport = executor.submit(() -> executeOrAssertConflict(second, secondModel));
                boolean firstSucceeded = firstExport.get(15, TimeUnit.SECONDS);
                boolean secondSucceeded = secondExport.get(15, TimeUnit.SECONDS);
                assertNotEquals(firstSucceeded, secondSucceeded, "Exactly one export must succeed");
                assertEquals(firstSucceeded ? firstCsv : secondCsv,
                        Files.readString(testFolder.resolve("contacts.csv")));
                try (var entries = Files.list(testFolder)) {
                    assertEquals(List.of(testFolder.resolve("contacts.csv")), entries.toList());
                }
                Files.delete(testFolder.resolve("contacts.csv"));
            }
        }
        assertEquals(firstBook, firstModel.getAddressBook());
        assertEquals(secondBook, secondModel.getAddressBook());
        assertTrue(firstModel.getFilteredPersonList().isEmpty());
        assertTrue(secondModel.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_permissionOrWriteFailure_reportsErrorWithoutChangingModel() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        AddressBook original = new AddressBook(model.getAddressBook());
        for (IOException failure : List.of(new IOException("confidential.csv: Disk full"),
                new AccessDeniedException("confidential.csv"))) {
            CsvContactExporter exporter = new CsvContactExporter(testFolder) {
                @Override
                public void export(List<Person> contacts, String fileName) throws IOException {
                    throw failure;
                }
            };
            ExportCommand command = new ExportCommand("contacts.csv", exporter);
            try (LogCapture logs = new LogCapture()) {
                assertThrows(CommandException.class,
                        String.format(ExportCommand.MESSAGE_WRITE_ERROR, "contacts.csv"), () -> command.execute(model));
                assertFalse(logs.getOutput().contains("confidential.csv"), logs.getOutput());
                assertFalse(logs.getOutput().contains("contacts.csv"), logs.getOutput());
                assertTrue(logs.getOutput().contains("CSV export failed: " + failure.getClass().getSimpleName()));
            }
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

    private CsvContactExporter concurrentExporter(CyclicBarrier readyToPublish) {
        return new CsvContactExporter(testFolder) {
            @Override
            protected void writeContacts(Writer writer, List<Person> contacts) throws IOException {
                super.writeContacts(writer, contacts);
                writer.flush();
                try {
                    readyToPublish.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while waiting for the other export", e);
                } catch (BrokenBarrierException | TimeoutException e) {
                    throw new IOException("The other export did not reach publication", e);
                }
            }
        };
    }

    private boolean executeOrAssertConflict(ExportCommand command, Model model) throws CommandException {
        try {
            assertEquals(new CommandResult(String.format(ExportCommand.MESSAGE_SUCCESS,
                    model.getAddressBook().getPersonList().size(), "contacts.csv")), command.execute(model));
            return true;
        } catch (CommandException e) {
            assertEquals(String.format(ExportCommand.MESSAGE_ALREADY_EXISTS, "contacts.csv"), e.getMessage());
            assertInstanceOf(FileAlreadyExistsException.class, e.getCause());
            return false;
        }
    }
}
