package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class PartialContactIntegrationTest {
    @TempDir
    public Path folder;

    private ModelManager model;
    private JsonAddressBookStorage addressBookStorage;
    private LogicManager logic;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        addressBookStorage = new JsonAddressBookStorage(folder.resolve("contacts.json"));
        logic = logicFor(model, addressBookStorage);
    }

    @Test
    public void add_nameOnly_storesMissingFieldsAndRoundTrips() throws Exception {
        CommandResult result = logic.execute("add --name \"Alice Tan\"");
        assertEquals("Contact added successfully: Alice Tan | Phone: Not provided | Email: Not provided"
                + " | Department: Not provided | Address: Not provided | Tags: None", result.getFeedbackToUser());
        Person person = model.getFilteredPersonList().getFirst();
        assertFalse(person.getPhone().isProvided());
        assertFalse(person.getEmail().isProvided());
        assertFalse(person.getAddress().isProvided());
        assertTrue(person.getDepartment().isEmpty());
        assertTrue(person.getTags().isEmpty());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
        assertTrue(Files.readString(addressBookStorage.getAddressBookFilePath()).contains("\"phone\" : \"\""));
    }

    @Test
    public void add_sameNamesDifferentPhoneOrEmail_acceptsAndRejectsDuplicateIdentity() throws Exception {
        logic.execute("add --name Alice");
        logic.execute("add --name Alice --phone 91234567");
        logic.execute("add --name Alice --email alice@example.com");
        String storedBefore = Files.readString(addressBookStorage.getAddressBookFilePath());
        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, ()
                -> logic.execute("add --name Alice --department Sales --tag client"));
        assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, ()
                -> logic.execute("add --name Alice --phone 91234567 --address Main Road"));
        assertEquals(3, model.getFilteredPersonList().size());
        assertEquals(storedBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void editAndDepart_afterReload_completePartialContactWithoutLosingDepartment() throws Exception {
        logic.execute("add --name Chen Wei --department Engineering --tag intern new-client");
        ModelManager reloaded = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager resumed = logicFor(reloaded, addressBookStorage);
        resumed.execute("edit 1 p/91234567 e/chen@example.com a/Main Road");
        Person completed = reloaded.getFilteredPersonList().getFirst();
        assertEquals("Engineering", completed.getDepartment().orElseThrow().value);
        assertEquals("91234567", completed.getPhone().value);
        assertEquals("chen@example.com", completed.getEmail().value);
        assertEquals("Main Road", completed.getAddress().value);
        assertEquals(2, completed.getTags().size());
        resumed.execute("depart 1 --set R&D Engineering");
        assertEquals("R&D Engineering",
                reloaded.getFilteredPersonList().getFirst().getDepartment().orElseThrow().value);
        assertEquals(reloaded.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void edit_identityCollision_rejectsWithoutChangingData() throws Exception {
        logic.execute("add --name Alice --phone 91234567");
        logic.execute("add --name Alice --phone 87654321");
        String storedBefore = Files.readString(addressBookStorage.getAddressBookFilePath());
        assertThrows(CommandException.class, EditCommand.MESSAGE_DUPLICATE_PERSON, ()
                -> logic.execute("edit 2 p/91234567"));
        assertEquals("87654321", model.getFilteredPersonList().get(1).getPhone().value);
        assertEquals(storedBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void add_invalidArguments_preservesModelAndStorage() throws Exception {
        logic.execute("add --name Bob Lee --phone 91234567");
        String storedBefore = Files.readString(addressBookStorage.getAddressBookFilePath());
        assertThrows(ParseException.class, () -> logic.execute("add --name Alice --email broken"));
        assertThrows(ParseException.class, () -> logic.execute("add --name Alice --unknown value"));
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(storedBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void add_storageFailure_reportsFailureWithoutReturningSuccess() {
        JsonAddressBookStorage failing = new JsonAddressBookStorage(folder.resolve("failed.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("storage unavailable");
            }
        };
        LogicManager failedLogic = logicFor(model, failing);
        assertThrows(CommandException.class, "Could not save data due to the following error: storage unavailable", ()
                -> failedLogic.execute("add --name Alice"));
        assertFalse(Files.exists(failing.getAddressBookFilePath()));
    }

    private LogicManager logicFor(ModelManager target, JsonAddressBookStorage storage) {
        return new LogicManager(target, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
    }
}
