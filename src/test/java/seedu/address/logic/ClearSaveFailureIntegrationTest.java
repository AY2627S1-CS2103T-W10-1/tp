package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.util.AtomicFileWriter;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.SortField;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class ClearSaveFailureIntegrationTest {
    @TempDir
    public Path folder;

    private ModelManager model;
    private LogicManager logic;
    private RecordingStorage storage;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        storage = new RecordingStorage(folder.resolve("contacts.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
    }

    @Test
    public void execute_permissionFailure_preservesDataDisplayAndDeletionHistory() throws Exception {
        assertFailedClearPreservesState(new AccessDeniedException("contacts.json"));
    }

    @Test
    public void execute_ioFailure_preservesDataDisplayAndDeletionHistory() throws Exception {
        assertFailedClearPreservesState(new IOException("Storage unavailable"));
    }

    @Test
    public void execute_departmentOnlyFailure_preservesUnusedCatalogueEntries() throws Exception {
        logic.execute("depart --add Engineering");
        logic.execute("depart --add Unused");
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] savedBefore = Files.readAllBytes(storage.getAddressBookFilePath());
        storage.failure = new IOException("Storage unavailable");

        assertThrows(CommandException.class, () -> logic.execute("clear --confirm"));

        assertEquals(before, model.getAddressBook());
        assertArrayEquals(savedBefore, Files.readAllBytes(storage.getAddressBookFilePath()));
        storage.failure = null;
        logic.execute("list");
        assertEquals(before, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_retryAfterFailure_savesOnceBeforeClearingAndPreservesEarlierHistory() throws Exception {
        populateBookWithHistoryAndDisplayState();
        AddressBook before = new AddressBook(model.getAddressBook());
        storage.failure = new IOException("Storage unavailable");
        assertThrows(CommandException.class, () -> logic.execute("clear --confirm"));
        int attemptsBeforeRetry = storage.requests.size();
        storage.failure = null;

        assertEquals(ClearCommand.MESSAGE_SUCCESS, logic.execute("clear --confirm").getFeedbackToUser());

        assertEquals(attemptsBeforeRetry + 1, storage.requests.size());
        assertEquals(before, storage.liveStates.getLast());
        assertEmptyBook(storage.requests.getLast());
        assertEmptyBook(model.getAddressBook());
        assertEmptyBook(storage.readAddressBook().orElseThrow());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEarlierHistoryIsAvailable();
    }

    @Test
    public void execute_unconfirmedAndInvalidClear_doesNotAttemptSaving() throws Exception {
        populateBookWithHistoryAndDisplayState();
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        int attemptsBefore = storage.requests.size();
        storage.failure = new IOException("Storage unavailable");

        assertEquals(String.format(ClearCommand.MESSAGE_CONFIRMATION, before.getPersonList().size()),
                logic.execute("clear").getFeedbackToUser());
        for (String command : List.of("clear extra", "clear --CONFIRM", "clear --confirm extra")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
        }

        assertEquals(attemptsBefore, storage.requests.size());
        assertEquals(before, model.getAddressBook());
        assertEquals(displayedBefore, model.getFilteredPersonList());
    }

    @Test
    public void execute_partialTemporaryWriteFailure_preservesSavedAndLiveData() throws Exception {
        populateBookWithHistoryAndDisplayState();
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        byte[] savedBefore = Files.readAllBytes(storage.getAddressBookFilePath());
        storage.failure = new IOException("Interrupted write");
        storage.writePartialData = true;

        assertThrows(CommandException.class, () -> logic.execute("clear --confirm"));

        assertArrayEquals(savedBefore, Files.readAllBytes(storage.getAddressBookFilePath()));
        assertEquals(before, model.getAddressBook());
        assertEquals(displayedBefore, model.getFilteredPersonList());
        storage.failure = null;
        storage.writePartialData = false;
        logic.execute("list");
        assertEquals(before, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_listWriteFailure_preservesSavedDataAndShowsAllContacts() throws Exception {
        populateBookWithHistoryAndDisplayState();
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] savedBefore = Files.readAllBytes(storage.getAddressBookFilePath());
        storage.failure = new IOException("Interrupted write");
        storage.writePartialData = true;

        assertThrows(CommandException.class, () -> logic.execute("list"));

        assertArrayEquals(savedBefore, Files.readAllBytes(storage.getAddressBookFilePath()));
        assertEquals(before, model.getAddressBook());
        assertEquals(before.getPersonList(), model.getFilteredPersonList());
        storage.failure = null;
        storage.writePartialData = false;
        logic.execute("list");
        assertEquals(before, storage.readAddressBook().orElseThrow());
        assertEarlierHistoryIsAvailable();
    }

    @Test
    public void execute_realReadOnlyFile_preservesDataAndAllowsRecoveryAndRetry() throws Exception {
        logic.execute("depart --add Engineering");
        logic.execute("add -n Alice -p 91234567 -d Engineering");
        AddressBook before = new AddressBook(model.getAddressBook());
        byte[] savedBefore = Files.readAllBytes(storage.getAddressBookFilePath());

        try (AutoCloseable restorePermission = preventWrites()) {
            assumeFalse(Files.isWritable(storage.getAddressBookFilePath()),
                    "This account can bypass file write restrictions.");
            CommandException error = assertThrows(CommandException.class, () -> logic.execute("clear --confirm"));
            assertTrue(error.getMessage().contains(ClearCommand.MESSAGE_SAVE_FAILURE_RECOVERY));
            assertEquals(before, model.getAddressBook());
            assertArrayEquals(savedBefore, Files.readAllBytes(storage.getAddressBookFilePath()));
        }

        logic.execute("list");
        assertEquals(before, storage.readAddressBook().orElseThrow());
        logic.execute("clear --confirm");
        assertEmptyBook(model.getAddressBook());
        assertEmptyBook(storage.readAddressBook().orElseThrow());
    }

    private void assertFailedClearPreservesState(IOException failure) throws Exception {
        populateBookWithHistoryAndDisplayState();
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        byte[] savedBefore = Files.readAllBytes(storage.getAddressBookFilePath());
        int attemptsBefore = storage.requests.size();
        storage.failure = failure;

        CommandException error = assertThrows(CommandException.class, () -> logic.execute("clear --confirm"));

        String errorFormat = failure instanceof AccessDeniedException
                ? LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT : LogicManager.FILE_OPS_ERROR_FORMAT;
        assertEquals(String.format(errorFormat, failure.getMessage()) + "\n"
                + ClearCommand.MESSAGE_SAVE_FAILURE_RECOVERY, error.getMessage());
        assertSame(failure, error.getCause());
        assertEquals(attemptsBefore + 1, storage.requests.size());
        assertEmptyBook(storage.requests.getLast());
        assertEquals(before, storage.liveStates.getLast());
        assertEquals(before, model.getAddressBook());
        assertEquals(displayedBefore, model.getFilteredPersonList());
        assertArrayEquals(savedBefore, Files.readAllBytes(storage.getAddressBookFilePath()));

        storage.failure = null;
        logic.execute("list");
        assertEquals(before, storage.readAddressBook().orElseThrow());
        assertEarlierHistoryIsAvailable();
    }

    private void populateBookWithHistoryAndDisplayState() throws Exception {
        logic.execute("depart --add Engineering");
        logic.execute("depart --add Sales");
        logic.execute("depart --add Unused");
        logic.execute("add -n Zoe -p 91234567 -d Engineering -t zeta");
        logic.execute("add -n Alice -p 92345678 -d Engineering -t alpha");
        logic.execute("add -n Hidden -p 93456789 -d Sales");
        logic.execute("add -n Deleted -p 94567890 -d Engineering");
        logic.execute("delete 4");
        logic.execute("depart --delete Sales");
        logic.execute("find Zoe Alice");
        model.sortFilteredPersonList(SortField.TAGS);
        assertEquals(List.of("Alice", "Zoe"), model.getFilteredPersonList().stream()
                .map(person -> person.getName().fullName).toList());
    }

    private void assertEarlierHistoryIsAvailable() throws Exception {
        assertEquals(String.format(UndoCommand.MESSAGE_DEPARTMENT_RESTORED, "Sales"),
                logic.execute("undo").getFeedbackToUser());
        assertEquals("Deleted", model.getLastDeletedPerson().getName().fullName);
        logic.execute("undo");
        assertEquals("Deleted", model.getAddressBook().getPersonList().getLast().getName().fullName);
        assertFalse(model.hasDeletedPerson());
        assertEquals(new Department("Engineering"),
                model.getAddressBook().getPersonList().getLast().getDepartment().orElseThrow());
    }

    private AutoCloseable preventWrites() throws IOException {
        Path path = storage.getAddressBookFilePath();
        PosixFileAttributeView posix = Files.getFileAttributeView(path, PosixFileAttributeView.class);
        if (posix != null) {
            Set<PosixFilePermission> original = posix.readAttributes().permissions();
            Set<PosixFilePermission> readOnly = new HashSet<>(original);
            readOnly.removeAll(Set.of(PosixFilePermission.OWNER_WRITE, PosixFilePermission.GROUP_WRITE,
                    PosixFilePermission.OTHERS_WRITE));
            posix.setPermissions(readOnly);
            return () -> posix.setPermissions(original);
        }
        DosFileAttributeView dos = Files.getFileAttributeView(path, DosFileAttributeView.class);
        assumeTrue(dos != null, "This filesystem does not expose file write permissions.");
        boolean original = dos.readAttributes().isReadOnly();
        dos.setReadOnly(true);
        return () -> dos.setReadOnly(original);
    }

    private void assertEmptyBook(ReadOnlyAddressBook addressBook) {
        assertTrue(addressBook.getPersonList().isEmpty());
        assertTrue(addressBook.getDepartmentList().isEmpty());
    }

    private class RecordingStorage extends JsonAddressBookStorage {
        private final List<AddressBook> requests = new ArrayList<>();
        private final List<AddressBook> liveStates = new ArrayList<>();
        private IOException failure;
        private boolean writePartialData;

        RecordingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            requests.add(new AddressBook(addressBook));
            liveStates.add(new AddressBook(model.getAddressBook()));
            if (failure != null) {
                if (writePartialData) {
                    new AtomicFileWriter() {
                        @Override
                        protected void writeTemporaryFile(Path temporary, String content) throws IOException {
                            Files.writeString(temporary, "{");
                            throw failure;
                        }
                    }.write(getAddressBookFilePath(), "{}");
                }
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }
}
