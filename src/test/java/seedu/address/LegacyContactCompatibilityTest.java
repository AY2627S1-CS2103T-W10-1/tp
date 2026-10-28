package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Address;
import seedu.address.model.person.Department;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises real application startup, commands and file persistence with previously valid contacts. */
public class LegacyContactCompatibilityTest {
    private static final Path FIXTURE = Path.of("src", "test", "data", "LegacyContactCompatibilityTest",
            "contacts.json");

    @TempDir
    public Path folder;

    @Test
    public void legacyFileWithoutCatalogue_loadSaveAddAndRestart_preservesEveryContact() throws Exception {
        assertRoundTrip(false);
    }

    @Test
    public void fileWithCatalogue_loadSaveAddAndRestart_preservesEveryContact() throws Exception {
        assertRoundTrip(true);
    }

    private void assertRoundTrip(boolean includeCatalogue) throws Exception {
        StorageManager storage = storageWithLegacyContacts(includeCatalogue);
        Path path = storage.getAddressBookFilePath();
        String originalBytes = Files.readString(path);
        Model model = load(storage);
        assertEquals(expectedContacts(), model.getAddressBook().getPersonList());
        assertEquals(originalBytes, Files.readString(path));
        LogicManager logic = new LogicManager(model, storage);

        // Even a successful list command saves JSON. It must not replace the old file with empty data.
        logic.execute("list");
        Model restarted = load(storage);
        assertEquals(expectedContacts(), restarted.getAddressBook().getPersonList());

        new LogicManager(restarted, storage).execute("add --name New Contact --phone 94444444");
        Model restartedAgain = load(storage);
        List<Person> allContacts = restartedAgain.getAddressBook().getPersonList();
        assertEquals(6, allContacts.size());
        assertEquals(expectedContacts(), allContacts.subList(0, 5));
        assertEquals("New Contact", allContacts.getLast().getName().fullName);
        List<Department> expectedDepartments = new ArrayList<>(List.of(new Department("Engineering"),
                new Department("Sales")));
        if (includeCatalogue) {
            expectedDepartments.add(new Department("Unused"));
        }
        assertEquals(expectedDepartments, restartedAgain.getAddressBook().getDepartmentList());
        assertEquals(restartedAgain.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void legacyConflicts_doNotBypassNewAddEditOrContactUndoRules() throws Exception {
        StorageManager storage = storageWithLegacyContacts(true);
        Model model = load(storage);
        assertEquals(expectedContacts(), model.getAddressBook().getPersonList());
        LogicManager logic = new LogicManager(model, storage);
        String before = Files.readString(storage.getAddressBookFilePath());

        for (String input : List.of("add --name Alice --phone 91234567 --email new@example.com",
                "add --name Alice --phone 95555555 --email two@example.com")) {
            assertEquals(AddCommand.MESSAGE_DUPLICATE_PERSON,
                    assertThrows(CommandException.class, () -> logic.execute(input)).getMessage());
        }
        assertThrows(ParseException.class, () -> logic.execute("add --name Name Only"));
        for (String input : List.of("edit 1 --email new@example.com", "edit 1 --address New Road")) {
            assertEquals(EditCommand.MESSAGE_DUPLICATE_PERSON,
                    assertThrows(CommandException.class, () -> logic.execute(input)).getMessage());
        }
        assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(expectedContacts(), model.getAddressBook().getPersonList());

        logic.execute("delete 1");
        String afterDelete = Files.readString(storage.getAddressBookFilePath());
        assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertTrue(model.hasDeletedPerson());
        assertEquals(expectedContacts().getFirst(), model.getLastDeletedPerson());
        assertEquals(afterDelete, Files.readString(storage.getAddressBookFilePath()));

        // Resolve the conflicting surviving contact before retrying the retained deletion.
        logic.execute("edit 1 --name Alicia");
        logic.execute("undo");
        assertFalse(model.hasDeletedPerson());
        assertEquals(5, model.getAddressBook().getPersonList().size());
        assertEquals(expectedContacts().getFirst(), model.getAddressBook().getPersonList().getLast());
        assertEquals(model.getAddressBook(), load(storage).getAddressBook());
    }

    @Test
    public void departmentRenameDeleteAndUndo_preserveLegacyContactIdentities() throws Exception {
        StorageManager storage = storageWithLegacyContacts(true);
        Model model = load(storage);
        assertEquals(expectedContacts(), model.getAddressBook().getPersonList());
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("depart --edit Engineering Research");
        List<Person> renamed = expectedContacts().stream()
                .map(person -> person.withRenamedDepartment(new Department("Engineering"), new Department("Research")))
                .toList();
        assertEquals(renamed, model.getAddressBook().getPersonList());
        logic.execute("depart --delete Research");
        assertEquals(renamed.stream().map(person -> person.withoutDepartment(new Department("Research"))).toList(),
                model.getAddressBook().getPersonList());
        logic.execute("undo");
        assertEquals(renamed, model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), load(storage).getAddressBook());
    }

    @Test
    public void contactUndo_sharedEmailWithDifferentPhone_rejectsAndKeepsDataAndHistory() throws Exception {
        StorageManager storage = storageWithLegacyContacts(true);
        Model model = load(storage);
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("delete 3");
        String before = Files.readString(storage.getAddressBookFilePath());
        assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertEquals(expectedContacts().get(2), model.getLastDeletedPerson());
        assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
        logic.execute("edit 2 --name Alicia");
        logic.execute("undo");
        assertFalse(model.hasDeletedPerson());
        assertEquals(expectedContacts().get(2), model.getAddressBook().getPersonList().getLast());
        assertEquals(model.getAddressBook(), load(storage).getAddressBook());
    }

    @Test
    public void contactUndo_legacyNameOnlyContact_rejectsAndKeepsDataAndHistory() throws Exception {
        StorageManager storage = storageWithLegacyContacts(true);
        Model model = load(storage);
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("delete 5");
        String before = Files.readString(storage.getAddressBookFilePath());
        assertEquals(String.format(UndoCommand.MESSAGE_MISSING_CONTACT_INFORMATION,
                Messages.format(expectedContacts().getLast())),
                assertThrows(CommandException.class, () -> logic.execute("undo")).getMessage());
        assertEquals(expectedContacts().getLast(), model.getLastDeletedPerson());
        assertEquals(before, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(expectedContacts().subList(0, 4), model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), load(storage).getAddressBook());
    }

    private StorageManager storageWithLegacyContacts(boolean includeCatalogue) throws Exception {
        Path path = folder.resolve("contacts.json");
        String json = Files.readString(FIXTURE).replace("\r\n", "\n");
        if (!includeCatalogue) {
            json = json.replace("  \"departments\": [\"Engineering\", \"Sales\", \"Unused\"],\n", "");
        }
        Files.writeString(path, json);
        return new StorageManager(new JsonAddressBookStorage(path),
                new JsonUserPrefsStorage(folder.resolve("preferences.json")));
    }

    private Model load(StorageManager storage) {
        return new MainApp().initModelManager(storage, new UserPrefs());
    }

    private List<Person> expectedContacts() {
        return List.of(
                new PersonBuilder().withName("Alice").withPhone("91234567").withEmail("one@example.com")
                        .withAddress("First Road").withDepartment("Engineering").withTags("friend").build(),
                new PersonBuilder().withName("Alice").withPhone("91234567").withEmail("two@example.com")
                        .withAddress("Second Road").withDepartment("Engineering").withTags("client").build(),
                new PersonBuilder().withName("Alice").withPhone("87654321").withEmail("two@example.com")
                        .withAddress("Third Road").withDepartment("Sales").withTags("work").build(),
                new PersonBuilder().withName("Bob").withPhone("93333333").withEmail("bob@example.com")
                        .withAddress("Other Road").withTags().build(),
                new Person(new Name("Archive"), Phone.notProvided(), Email.notProvided(),
                        Address.notProvided(), Set.of()));
    }
}
