package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.SortCommand;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.SortField;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class SortContactLoadTest {
    @TempDir
    public Path temporaryFolder;

    private Model loadContacts(JsonAddressBookStorage contacts) {
        StorageManager storage = new StorageManager(contacts,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        return new MainApp().initModelManager(storage, new UserPrefs());
    }

    @Test
    public void loadUnreadableContacts_sortReportsNoContacts() throws Exception {
        JsonAddressBookStorage contacts = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")) {
            @Override
            public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
                throw new DataLoadingException(new IOException("Cannot read contacts."));
            }
        };
        Model model = loadContacts(contacts);
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(SortCommand.MESSAGE_EMPTY, new SortCommand(SortField.TAGS).execute(model).getFeedbackToUser());
    }

    @Test
    public void loadMalformedJson_sortReportsNoContactsAndPreservesFile() throws Exception {
        Path path = temporaryFolder.resolve("contacts.json");
        Files.writeString(path, "{ malformed json");
        Model model = loadContacts(new JsonAddressBookStorage(path));
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(SortCommand.MESSAGE_EMPTY, new SortCommand(SortField.TAGS).execute(model).getFeedbackToUser());
        assertEquals("{ malformed json", Files.readString(path));
    }

    @Test
    public void loadInvalidContact_sortReportsNoContacts() throws Exception {
        Path path = temporaryFolder.resolve("contacts.json");
        Files.writeString(path, "{\"persons\":[{\"name\":\"Alex\",\"phone\":\"invalid\"}]}");
        Model model = loadContacts(new JsonAddressBookStorage(path));
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(SortCommand.MESSAGE_EMPTY, new SortCommand(SortField.TAGS).execute(model).getFeedbackToUser());
    }

    @Test
    public void loadEmptyContacts_sortReportsNoContacts() throws Exception {
        Path path = temporaryFolder.resolve("contacts.json");
        Files.writeString(path, "{\"persons\":[]}");
        Model model = loadContacts(new JsonAddressBookStorage(path));
        assertEquals(SortCommand.MESSAGE_EMPTY, new SortCommand(SortField.TAGS).execute(model).getFeedbackToUser());
    }

    @Test
    public void loadMissingFile_usesSamplesAndAllowsSort() throws Exception {
        Model model = loadContacts(new JsonAddressBookStorage(temporaryFolder.resolve("missing.json")));
        assertEquals("Sorted " + model.getFilteredPersonList().size() + " contacts by department.",
                new SortCommand(SortField.DEPARTMENT).execute(model).getFeedbackToUser());
    }

    @Test
    public void loadMalformedJson_addAndSave_sortAllowsRecoveredContacts() throws Exception {
        Path path = temporaryFolder.resolve("contacts.json");
        Files.writeString(path, "{ malformed json");
        assertRecoveryAllowsSort(new JsonAddressBookStorage(path));
    }

    @Test
    public void loadUnreadableContacts_addAndSave_sortAllowsRecoveredContacts() throws Exception {
        JsonAddressBookStorage contacts = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")) {
            @Override
            public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
                throw new DataLoadingException(new IOException("Cannot read contacts."));
            }
        };
        assertRecoveryAllowsSort(contacts);
    }

    private void assertRecoveryAllowsSort(JsonAddressBookStorage contacts) throws Exception {
        Model model = loadContacts(contacts);
        LogicManager logic = new LogicManager(model, new StorageManager(contacts,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
        logic.execute("add n/Alex p/91234567 e/alex@example.com a/First Street t/zulu");
        logic.execute("add n/Beatrice p/92345678 e/beatrice@example.com a/Second Street t/alpha");
        List<Person> storedOrder = List.copyOf(model.getAddressBook().getPersonList());
        Path path = contacts.getAddressBookFilePath();
        assertEquals(storedOrder, new JsonAddressBookStorage(path).readAddressBook().orElseThrow().getPersonList());
        String savedData = Files.readString(path);

        assertEquals("Sorted 2 contacts by tags.", logic.execute("sort -b tags").getFeedbackToUser());
        assertEquals(List.of(storedOrder.get(1), storedOrder.get(0)), model.getFilteredPersonList());
        assertEquals(storedOrder, model.getAddressBook().getPersonList());
        assertEquals(savedData, Files.readString(path));
    }
}
