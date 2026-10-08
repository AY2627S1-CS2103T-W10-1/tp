package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.SortCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class SortLogicIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private final ModelManager model = new ModelManager();
    private final Person alex = new PersonBuilder().withName("Alex").withTags("zulu").build();
    private final Person beatrice = new PersonBuilder().withName("Beatrice").withTags("alpha").build();

    private Logic createLogic(boolean shouldRejectStorageAccess) {
        JsonAddressBookStorage contacts = new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json")) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                if (shouldRejectStorageAccess) {
                    throw new AssertionError("Sort must not write to storage.");
                }
                super.saveAddressBook(addressBook);
            }
        };
        return new LogicManager(model, new StorageManager(contacts,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
    }

    @Test
    public void execute_sort_preservesFilterAndFileWithoutSaving() throws Exception {
        model.addPerson(alex);
        model.addPerson(beatrice);
        model.addPerson(new PersonBuilder().withName("Hidden").build());
        model.updateFilteredPersonList(person -> !person.getName().fullName.equals("Hidden"));
        Path path = temporaryFolder.resolve("contacts.json");
        Files.writeString(path, "Storage is not consulted during sorting.");
        Logic logic = createLogic(true);
        CommandResult result = logic.execute("sort --by tag");
        assertEquals(List.of(beatrice, alex), logic.getFilteredPersonList());
        assertEquals("Storage is not consulted during sorting.", Files.readString(path));
        assertEquals("Contacts sorted by tags:\n"
                + "1. Beatrice | Department: Not assigned | Tags: alpha\n"
                + "2. Alex | Department: Not assigned | Tags: zulu", result.getFeedbackToUser());
    }

    @Test
    public void execute_sortThenEdit_targetsDisplayedContactAndPersistsCanonicalOrder() throws Exception {
        model.addPerson(alex);
        model.addPerson(beatrice);
        Logic logic = createLogic(false);
        logic.execute("sort -b tags");
        logic.execute("edit 1 n/Updated");
        Person edited = new PersonBuilder(beatrice).withName("Updated").build();
        assertEquals(List.of(alex, edited), logic.getFilteredPersonList());
        assertEquals(List.of(alex, edited), new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"))
                .readAddressBook().orElseThrow().getPersonList());
    }

    @Test
    public void execute_sortThenListOrFind_resetsDisplayOrder() throws Exception {
        model.addPerson(alex);
        model.addPerson(beatrice);
        Logic logic = createLogic(false);
        logic.execute("sort -b tags");
        logic.execute("list");
        assertEquals(List.of(alex, beatrice), logic.getFilteredPersonList());
        logic.execute("sort -b tags");
        logic.execute("find Alex Beatrice");
        assertEquals(List.of(alex, beatrice), logic.getFilteredPersonList());
    }

    @Test
    public void execute_invalidSort_keepsCurrentOrderAndDoesNotSave() {
        model.addPerson(alex);
        model.addPerson(beatrice);
        Logic logic = createLogic(true);
        ParseException error = assertThrows(ParseException.class, () -> logic.execute("sort -b tags --descending"));
        assertEquals(SortCommandParser.MESSAGE_DESCENDING, error.getMessage());
        assertEquals(List.of(alex, beatrice), logic.getFilteredPersonList());
    }

    @Test
    public void execute_emptySort_succeedsWithoutSaving() throws Exception {
        assertEquals(SortCommand.MESSAGE_EMPTY, createLogic(true).execute("sort -b dept").getFeedbackToUser());
    }
}
