package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Email;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Regression checks for long email input through real commands and JSON persistence.
 */
public class LongEmailWorkflowTest {
    @TempDir
    public Path folder;

    private ModelManager model;
    private Logic logic;
    private JsonAddressBookStorage contacts;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        contacts = new JsonAddressBookStorage(folder.resolve("contacts.json"));
        logic = new LogicManager(model, new StorageManager(contacts,
                new JsonUserPrefsStorage(folder.resolve("prefs.json"))));
    }

    @Test
    public void execute_longValidEmails_addEditAndReloadWithoutDataLoss() throws Exception {
        String localEmail = "a.".repeat(5000) + "a@example.com";
        logic.execute("add --name Alice --email " + localEmail);
        assertEquals(localEmail, model.getAddressBook().getPersonList().getFirst().getEmail().value);
        assertEquals(localEmail, contacts.readAddressBook().orElseThrow().getPersonList().getFirst().getEmail().value);

        String domainEmail = "a@" + "a.".repeat(5000) + "example.com";
        logic.execute("edit 1 --email " + domainEmail);
        assertEquals(domainEmail, model.getAddressBook().getPersonList().getFirst().getEmail().value);
        assertEquals(model.getAddressBook().getPersonList(), contacts.readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_longInvalidEmails_reportNormalErrorsAndPreserveState() throws Exception {
        logic.execute("add --name Alice --email alice@example.com");
        logic.execute("find Alice");
        String saved = Files.readString(contacts.getAddressBookFilePath());
        var displayed = List.copyOf(model.getFilteredPersonList());

        for (String invalidEmail : List.of("a.".repeat(5000) + "@example.com",
                "a@" + "a.".repeat(5000), "a@" + "a-".repeat(5000) + "!")) {
            for (String command : List.of("add --name Bob --email ", "edit 1 --email ")) {
                assertThrows(ParseException.class, Email.MESSAGE_CONSTRAINTS, () ->
                        logic.execute(command + invalidEmail));
                assertEquals(saved, Files.readString(contacts.getAddressBookFilePath()));
                assertEquals(displayed, model.getFilteredPersonList());
                assertEquals(displayed, model.getAddressBook().getPersonList());
            }
        }
        assertTrue(logic.execute("list").getFeedbackToUser().contains("Listed"));
        assertEquals(displayed, contacts.readAddressBook().orElseThrow().getPersonList());
    }
}
