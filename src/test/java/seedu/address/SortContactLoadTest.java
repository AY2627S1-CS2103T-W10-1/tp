package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ContactLoadStatus;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.SortField;
import seedu.address.model.UserPrefs;
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

    private void assertSortFailure(Model model, String message) {
        CommandException error = assertThrows(CommandException.class, () ->
                new SortCommand(SortField.TAGS).execute(model));
        assertEquals(message, error.getMessage());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void loadUnreadableContacts_sortReportsLoadFailure() {
        JsonAddressBookStorage contacts = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")) {
            @Override
            public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
                throw new DataLoadingException(new IOException("Cannot read contacts."));
            }
        };
        Model model = loadContacts(contacts);
        assertEquals(ContactLoadStatus.UNREADABLE, model.getContactLoadStatus());
        assertSortFailure(model, SortCommand.MESSAGE_LOAD_FAILURE);
    }

    @Test
    public void loadMalformedJson_sortReportsInvalidData() throws Exception {
        Path path = temporaryFolder.resolve("contacts.json");
        Files.writeString(path, "{ malformed json");
        Model model = loadContacts(new JsonAddressBookStorage(path));
        assertEquals(ContactLoadStatus.INVALID, model.getContactLoadStatus());
        assertSortFailure(model, SortCommand.MESSAGE_INVALID_DATA);
        assertEquals("{ malformed json", Files.readString(path));
    }

    @Test
    public void loadInvalidContact_sortReportsInvalidData() throws Exception {
        Path path = temporaryFolder.resolve("contacts.json");
        Files.writeString(path, "{\"persons\":[{\"name\":\"Alex\",\"phone\":\"invalid\"}]}");
        Model model = loadContacts(new JsonAddressBookStorage(path));
        assertEquals(ContactLoadStatus.INVALID, model.getContactLoadStatus());
        assertSortFailure(model, SortCommand.MESSAGE_INVALID_DATA);
    }

    @Test
    public void loadEmptyContacts_sortReportsNoContacts() throws Exception {
        Path path = temporaryFolder.resolve("contacts.json");
        Files.writeString(path, "{\"persons\":[]}");
        Model model = loadContacts(new JsonAddressBookStorage(path));
        assertEquals(ContactLoadStatus.READY, model.getContactLoadStatus());
        assertEquals(SortCommand.MESSAGE_EMPTY, new SortCommand(SortField.TAGS).execute(model).getFeedbackToUser());
    }

    @Test
    public void loadMissingFile_usesSamplesAndAllowsSort() throws Exception {
        Model model = loadContacts(new JsonAddressBookStorage(temporaryFolder.resolve("missing.json")));
        assertEquals(ContactLoadStatus.READY, model.getContactLoadStatus());
        assertTrue(new SortCommand(SortField.DEPARTMENT).execute(model).getFeedbackToUser()
                .startsWith("Contacts sorted by department:"));
    }
}
