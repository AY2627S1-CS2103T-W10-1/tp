package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

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
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
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
    public void add_emailOnly_storesMissingFieldsAndRoundTrips() throws Exception {
        CommandResult result = logic.execute("add --name \"Alice Tan\" --email alice@example.com");
        assertEquals("Contact added successfully: Alice Tan | Phone: Not provided | Email: alice@example.com"
                + " | Department: Not provided | Address: Not provided | Tags: None", result.getFeedbackToUser());
        Person person = model.getFilteredPersonList().getFirst();
        assertFalse(person.getPhone().isProvided());
        assertTrue(person.getEmail().isProvided());
        assertFalse(person.getAddress().isProvided());
        assertTrue(person.getDepartment().isEmpty());
        assertTrue(person.getTags().isEmpty());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
        assertTrue(Files.readString(addressBookStorage.getAddressBookFilePath()).contains("\"phone\" : \"\""));
    }

    @Test
    public void add_sameNamesDifferentPhoneOrEmail_acceptsAndRejectsDuplicateIdentity() throws Exception {
        logic.execute("add --name Alice --phone 91234567 --email alice@example.com");
        logic.execute("add --name Alice --phone 87654321 --email other@example.com");
        logic.execute("add --name Alice --phone 91111111");
        logic.execute("add --name Alice --email third@example.com");
        logic.execute("add --name Bob --phone 91234567 --email alice@example.com");
        String storedBefore = Files.readString(addressBookStorage.getAddressBookFilePath());
        for (String command : List.of("add --name Alice --phone 91234567 --email new@example.com",
                "add --name Alice --phone 92222222 --email alice@example.com",
                "add --name Alice --phone 91111111 --address Main Road",
                "add --name Alice --email third@example.com --tag client")) {
            assertThrows(CommandException.class, AddCommand.MESSAGE_DUPLICATE_PERSON, () -> logic.execute(command));
        }
        assertEquals(5, model.getFilteredPersonList().size());
        assertEquals(storedBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void editAndDepart_afterReload_completePartialContactWithoutLosingDepartment() throws Exception {
        logic.execute("depart --add Engineering");
        logic.execute("add --name Chen Wei --phone 91234567 --department Engineering --tag intern new-client");
        ModelManager reloaded = new ModelManager(addressBookStorage.readAddressBook().orElseThrow(), new UserPrefs());
        LogicManager resumed = logicFor(reloaded, addressBookStorage);
        resumed.execute("edit 1 -p 91234567 -e chen@example.com -a Main Road");
        Person completed = reloaded.getFilteredPersonList().getFirst();
        assertEquals("Engineering", completed.getDepartment().orElseThrow().value);
        assertEquals("91234567", completed.getPhone().value);
        assertEquals("chen@example.com", completed.getEmail().value);
        assertEquals("Main Road", completed.getAddress().value);
        assertEquals(2, completed.getTags().size());
        resumed.execute("depart --add \"R&D Engineering\"");
        resumed.execute("edit 1 --department \"R&D Engineering\"");
        assertEquals("R&D Engineering",
                reloaded.getFilteredPersonList().getFirst().getDepartment().orElseThrow().value);
        assertEquals(reloaded.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void edit_identityCollision_rejectsWithoutChangingData() throws Exception {
        logic.execute("add --name Alice --phone 91234567 --email alice@example.com");
        logic.execute("add --name Alice --phone 87654321 --email other@example.com");
        String storedBefore = Files.readString(addressBookStorage.getAddressBookFilePath());
        for (String command : List.of("edit 2 -p 91234567", "edit 2 -e alice@example.com")) {
            assertThrows(CommandException.class, EditCommand.MESSAGE_DUPLICATE_PERSON, () -> logic.execute(command));
        }
        assertEquals("87654321", model.getFilteredPersonList().get(1).getPhone().value);
        assertEquals(storedBefore, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void add_withoutPhoneOrEmail_preservesModelAndStoredBytes() throws Exception {
        logic.execute("add --name Bob --email bob@example.com");
        String before = Files.readString(addressBookStorage.getAddressBookFilePath());
        for (String command : List.of("add --name Alice", "add -n Alice -a Main Road",
                "add --name Alice --department Sales --tag client")) {
            assertThrows(ParseException.class, Person.MESSAGE_CONTACT_INFORMATION_REQUIRED, ()
                    -> logic.execute(command));
        }
        assertEquals(1, model.getAddressBook().getPersonList().size());
        assertEquals(before, Files.readString(addressBookStorage.getAddressBookFilePath()));
    }

    @Test
    public void edit_legacyNameOnlyContact_requiresCompletingContactInformation() throws Exception {
        model.addPerson(new Person(new Name("Alice"), Phone.notProvided(), Email.notProvided(),
                Address.notProvided(), Set.of()));
        addressBookStorage.saveAddressBook(model.getAddressBook());
        String before = Files.readString(addressBookStorage.getAddressBookFilePath());
        assertThrows(CommandException.class, Person.MESSAGE_CONTACT_INFORMATION_REQUIRED, ()
                -> logic.execute("edit 1 --address Main Road"));
        assertEquals(before, Files.readString(addressBookStorage.getAddressBookFilePath()));
        logic.execute("edit 1 --email alice@example.com --address Main Road");
        assertTrue(model.getFilteredPersonList().getFirst().hasContactInformation());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void undo_matchesEitherContactFieldAndKeepsHistoryUntilConflictIsResolved() throws Exception {
        logic.execute("add --name Alice --phone 91234567 --email alice@example.com");
        logic.execute("delete 1");
        logic.execute("add --name Alice --phone 91234567 --email other@example.com");
        String before = Files.readString(addressBookStorage.getAddressBookFilePath());
        assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertTrue(model.hasDeletedPerson());
        assertEquals(before, Files.readString(addressBookStorage.getAddressBookFilePath()));
        logic.execute("edit 1 --phone 87654321");
        assertTrue(logic.execute("undo").getFeedbackToUser().startsWith("Restored deleted person:"));
        assertFalse(model.hasDeletedPerson());
        assertEquals(2, model.getFilteredPersonList().size());
        assertEquals(model.getAddressBook(), addressBookStorage.readAddressBook().orElseThrow());
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
                -> failedLogic.execute("add --name Alice --phone 91234567"));
        assertFalse(Files.exists(failing.getAddressBookFilePath()));
    }

    private LogicManager logicFor(ModelManager target, JsonAddressBookStorage storage) {
        return new LogicManager(target, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
    }
}
