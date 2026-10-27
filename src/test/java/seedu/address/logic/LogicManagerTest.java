package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_listCommandsAfterFiltering_restoreAllContacts() throws Exception {
        model.addPerson(AMY);
        model.addPerson(new PersonBuilder().withName("Bob").build());
        List<Person> storedBefore = List.copyOf(model.getAddressBook().getPersonList());

        for (String command : List.of("list", "ls")) {
            logic.execute("find Amy");
            assertEquals(List.of(AMY), model.getFilteredPersonList());

            CommandResult result = logic.execute(command);

            assertEquals(ListCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
            assertEquals(storedBefore, model.getFilteredPersonList());
            assertEquals(storedBefore, model.getAddressBook().getPersonList());
        }
    }

    @Test
    public void execute_listCommandsWithParameters_preserveFilterAndContacts() throws Exception {
        model.addPerson(AMY);
        model.addPerson(new PersonBuilder().withName("Bob").build());
        logic.execute("find Amy");
        List<Person> storedBefore = List.copyOf(model.getAddressBook().getPersonList());
        String expectedMessage = "The list command does not accept any parameters. Usage: list | ls";

        for (String command : List.of("list 3", "ls --sort")) {
            assertThrows(ParseException.class, expectedMessage, () -> logic.execute(command));
            assertEquals(List.of(AMY), model.getFilteredPersonList());
            assertEquals(storedBefore, model.getAddressBook().getPersonList());
        }
    }

    @Test
    public void execute_help_doesNotSaveEvenWhenStorageCannotWrite() throws Exception {
        model.addPerson(AMY);
        JsonAddressBookStorage blockedStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("blocked.json")) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) {
                        throw new AssertionError("Help must not save the address book");
                    }
                };
        logic = new LogicManager(model, new StorageManager(blockedStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        for (String input : List.of("help", "?", "help depart", "? ls")) {
            assertTrue(logic.execute(input).getHelpPage().isPresent());
            assertEquals(List.of(AMY), model.getAddressBook().getPersonList());
        }
        assertEquals(HelpCatalog.getOverview(), logic.execute("help").getHelpPage().orElseThrow());
    }

    @Test
    public void execute_undoDiscardLatest_doesNotSaveEvenWhenStorageCannotWrite() throws Exception {
        model.addPerson(AMY);
        model.deletePerson(AMY);
        JsonAddressBookStorage blockedStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("blocked.json")) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) {
                        throw new AssertionError("Discarding undo history must not save the address book");
                    }
                };
        logic = new LogicManager(model, new StorageManager(blockedStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        logic.execute("undo --discard-latest");
        assertFalse(model.hasDeletedPerson());
        assertEquals(List.of(), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_unconfirmedOrInvalidClear_doesNotSaveOrChangeContacts() throws Exception {
        model.addPerson(AMY);
        JsonAddressBookStorage blockedStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("blocked.json")) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) {
                        throw new AssertionError("Unconfirmed or invalid clear must not save");
                    }
                };
        logic = new LogicManager(model, new StorageManager(blockedStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));

        assertEquals(String.format(ClearCommand.MESSAGE_CONFIRMATION, 1),
                logic.execute("clear").getFeedbackToUser());
        for (String input : List.of("clear 1", "clear --confirm extra", "clear --CONFIRM")) {
            assertThrows(ParseException.class,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE), () ->
                            logic.execute(input));
        }
        assertEquals(List.of(AMY), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_clearConfirmation_persistsOnlyConfirmedDeletion() throws Exception {
        model.addPerson(AMY);
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("clear.json"));
        addressBookStorage.saveAddressBook(model.getAddressBook());
        logic = new LogicManager(model, new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));

        logic.execute("clear");
        assertEquals(List.of(AMY), addressBookStorage.readAddressBook().orElseThrow().getPersonList());

        assertThrows(ParseException.class, () -> logic.execute("clear 1"));
        assertEquals(List.of(AMY), addressBookStorage.readAddressBook().orElseThrow().getPersonList());

        assertEquals(ClearCommand.MESSAGE_SUCCESS, logic.execute("clear --confirm").getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertTrue(addressBookStorage.readAddressBook().orElseThrow().getPersonList().isEmpty());
        assertFalse(model.hasDeletedPerson());
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_exportSucceedsWhenAddressBookCannotBeSaved() throws Exception {
        model.addPerson(AMY);
        String fileName = "tp-export-test-" + UUID.randomUUID() + ".csv";
        Path csvPath = Path.of(fileName).toAbsolutePath();
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("blockedAddressBook.json")) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                        throw DUMMY_AD_EXCEPTION;
                    }
                };
        StorageManager storage = new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        logic = new LogicManager(model, storage);

        try {
            CommandResult result = logic.execute("export --csv " + fileName);
            assertEquals(String.format("Exported 1 contacts to %s.", fileName), result.getFeedbackToUser());
            assertEquals(2, Files.readAllLines(csvPath).size());
            assertEquals(List.of(AMY), model.getAddressBook().getPersonList());
        } finally {
            Files.deleteIfExists(csvPath);
        }
    }

    @Test
    public void execute_exportPartialContacts_preservesMissingFieldsAndFilter() throws Exception {
        logic.execute("add --name Alice --email alice@example.com");
        logic.execute("add --name Bob --phone 91234567 --tag /client");
        logic.execute("find --tag client");
        List<Person> storedBefore = List.copyOf(model.getAddressBook().getPersonList());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        assertEquals(List.of(storedBefore.get(1)), displayedBefore);
        Path dataPath = temporaryFolder.resolve("addressBook.json");
        String jsonBefore = Files.readString(dataPath);
        String fileName = "tp-export-test-" + UUID.randomUUID() + ".csv";
        Path csvPath = Path.of(fileName).toAbsolutePath();

        try {
            CommandResult result = logic.execute("EXPORT -c " + fileName);
            assertEquals("Exported 2 contacts to " + fileName + ".", result.getFeedbackToUser());
            assertEquals("Name,Phone,Email,Department,Address,Tags\r\n"
                    + "Alice,,alice@example.com,,,\r\nBob,91234567,,,,client\r\n",
                    Files.readString(csvPath));
            assertEquals(storedBefore, model.getAddressBook().getPersonList());
            assertEquals(displayedBefore, model.getFilteredPersonList());
            assertEquals(jsonBefore, Files.readString(dataPath));
        } finally {
            Files.deleteIfExists(csvPath);
        }
    }

    @Test
    public void execute_viewCommand_savesUnchangedContactsAndPreservesFilter() throws Exception {
        model.addPerson(AMY);
        model.addPerson(new PersonBuilder().withName("Bob").build());
        model.updateFilteredPersonList(AMY::equals);
        List<Person> storedBefore = List.copyOf(model.getAddressBook().getPersonList());
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        Path dataPath = temporaryFolder.resolve("addressBook.json");
        assertFalse(Files.exists(dataPath));

        CommandResult result = logic.execute("view 1");

        assertEquals("Showing details of contact 1: Amy Bee", result.getFeedbackToUser());
        assertTrue(result.getContactDetails().orElseThrow().contains("Name: Amy Bee\n"));
        assertEquals(storedBefore, model.getAddressBook().getPersonList());
        assertEquals(displayedBefore, model.getFilteredPersonList());
        assertEquals(model.getAddressBook(), new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow());
    }

    @Test
    public void execute_viewCommand_reportsStandardSaveErrorWhenStorageCannotWrite() throws Exception {
        model.addPerson(AMY);
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("blockedAddressBook.json")) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                        throw DUMMY_AD_EXCEPTION;
                    }
                };
        StorageManager storage = new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("blockedUserPrefs.json")));
        logic = new LogicManager(model, storage);

        assertThrows(CommandException.class, String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT,
                DUMMY_AD_EXCEPTION.getMessage()), () -> logic.execute("view 1"));
        assertEquals(List.of(AMY), model.getAddressBook().getPersonList());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
