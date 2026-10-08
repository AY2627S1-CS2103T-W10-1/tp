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
import seedu.address.model.SortField;
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

    @Test
    public void execute_departmentSort_ordersRealDepartmentsAndPreservesStoredData() throws Exception {
        Person engineering = new PersonBuilder(alex).withDepartment("Engineering").build();
        Person marketing = new PersonBuilder(beatrice).withDepartment("Marketing").build();
        Person sameDepartment = new PersonBuilder().withName("Chen").withDepartment("engineering").build();
        Person missing = new PersonBuilder().withName("Dana").build();
        model.addPerson(marketing);
        model.addPerson(missing);
        model.addPerson(sameDepartment);
        model.addPerson(engineering);
        Logic logic = createLogic(true);
        CommandResult result = logic.execute("sort -b department");
        assertEquals(List.of(sameDepartment, engineering, marketing, missing), logic.getFilteredPersonList());
        assertEquals(List.of(marketing, missing, sameDepartment, engineering), model.getAddressBook().getPersonList());
        assertEquals("Contacts sorted by department:\n"
                + "1. Chen | Department: engineering | Tags: None\n"
                + "2. Alex | Department: Engineering | Tags: zulu\n"
                + "3. Beatrice | Department: Marketing | Tags: alpha\n"
                + "4. Dana | Department: Not assigned | Tags: None", result.getFeedbackToUser());
    }

    @Test
    public void execute_sortThenDepartAndView_resolvesSortedIndices() throws Exception {
        model.addPerson(new PersonBuilder(alex).withDepartment("Marketing").build());
        model.addPerson(new PersonBuilder(beatrice).withDepartment("Engineering").build());
        Logic logic = createLogic(false);
        logic.execute("sort -b dept");
        assertEquals("Showing details of contact 1: Beatrice", logic.execute("view 1").getFeedbackToUser());
        logic.execute("depart 1 --set Support");
        Person updated = new PersonBuilder(beatrice).withDepartment("Support").build();
        assertEquals(updated, model.getAddressBook().getPersonList().get(1));
        assertEquals("Marketing", model.getAddressBook().getPersonList().get(0).getDepartment().orElseThrow().value);
        logic.execute("sort --by department");
        assertEquals("Showing details of contact 1: Alex", logic.execute("view 1").getFeedbackToUser());
    }

    @Test
    public void execute_sortDeleteUndo_restoresDepartmentAndResetsDisplayOrder() throws Exception {
        Person engineering = new PersonBuilder(beatrice).withDepartment("Engineering").build();
        model.addPerson(alex);
        model.addPerson(engineering);
        Logic logic = createLogic(false);
        logic.execute("sort -b dept");
        logic.execute("delete 1");
        assertEquals(List.of(alex), logic.getFilteredPersonList());
        logic.execute("undo");
        assertEquals(List.of(alex, engineering), logic.getFilteredPersonList());
        assertEquals(engineering, new JsonAddressBookStorage(temporaryFolder.resolve("contacts.json"))
                .readAddressBook().orElseThrow().getPersonList().get(1));
    }

    @Test
    public void execute_switchSortFields_preservesCurrentOrderOfDepartmentTies() throws Exception {
        Person first = new PersonBuilder(alex).withDepartment("Engineering").build();
        Person second = new PersonBuilder(beatrice).withDepartment("engineering").build();
        model.addPerson(first);
        model.addPerson(second);
        Logic logic = createLogic(false);
        logic.execute("sort -b tags");
        logic.execute("sort -b department");
        assertEquals(List.of(second, first), logic.getFilteredPersonList());
        model.updateFilteredPersonList(person -> person.equals(first));
        model.sortFilteredPersonList(SortField.DEPARTMENT);
        assertEquals(List.of(first), logic.getFilteredPersonList());
    }
}
