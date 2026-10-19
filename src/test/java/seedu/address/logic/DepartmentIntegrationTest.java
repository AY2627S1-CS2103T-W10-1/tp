package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

class DepartmentIntegrationTest {
    @TempDir
    private Path folder;
    private ModelManager model;
    private JsonAddressBookStorage storage;
    private LogicManager logic;

    @BeforeEach
    void setUp() {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(folder.resolve("contacts.json"));
        logic = logicFor(model);
    }

    @Test
    void catalogueAndAssignments_roundTripIncludingUnusedNames() throws Exception {
        logic.execute("depart --add Engineering");
        logic.execute("depart --add \"Customer Support\"");
        logic.execute("add --name Alice --department Engineering");
        logic.execute("add --name Bob --department Engineering");
        logic.execute("find Alice");
        logic.execute("depart --edit Engineering \"Research & Development\"");
        assertEquals(1, model.getFilteredPersonList().size());
        for (Person person : model.getAddressBook().getPersonList()) {
            assertEquals("Research & Development", person.getDepartment().orElseThrow().value);
        }
        ModelManager reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(model.getAddressBook(), reloaded.getAddressBook());
        assertEquals(List.of(new Department("Research & Development"), new Department("Customer Support")),
                reloaded.getAddressBook().getDepartmentList());
        LogicManager resumed = logicFor(reloaded);
        resumed.execute("edit 1 -d \"Customer Support\" -p 91234567");
        resumed.execute("edit 1 -e alice@example.com");
        assertEquals("Customer Support", reloaded.getFilteredPersonList().getFirst()
                .getDepartment().orElseThrow().value);
        assertEquals("Research & Development", reloaded.getFilteredPersonList().get(1)
                .getDepartment().orElseThrow().value);
        assertEquals(reloaded.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void invalidCommands_preserveAllFieldsCatalogueAndStoredBytes() throws Exception {
        logic.execute("depart --add Engineering");
        logic.execute("depart --add Sales");
        logic.execute("add --name Alice --department Engineering");
        AddressBook before = new AddressBook(model.getAddressBook());
        String bytes = Files.readString(storage.getAddressBookFilePath());
        for (String command : List.of("edit 1 --department Unknown --phone 91234567",
                "edit 1 --department engineering", "add --name Bob --department Unknown",
                "depart --add Engineering", "depart --edit Unknown Sales", "depart --edit Engineering Sales")) {
            assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertEquals(before, model.getAddressBook());
            assertEquals(bytes, Files.readString(storage.getAddressBookFilePath()));
        }
        for (String command : List.of("edit 1 --department", "edit 1 -d Engineering --department Sales",
                "edit 1 -d !", "depart --list extra", "depart --add Sales --list", "depart 1 --set Sales")) {
            assertThrows(ParseException.class, () -> logic.execute(command), command);
            assertEquals(before, model.getAddressBook());
            assertEquals(bytes, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    void list_isReadOnlyAndIncludesUnusedDepartments() throws Exception {
        assertTrue(logic.execute("depart --list").getFeedbackToUser().contains("No departments"));
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
        logic.execute("depart --add Engineering");
        logic.execute("depart --add Unused");
        logic.execute("add --name Alice --department Engineering");
        logic.execute("find Nobody");
        String bytes = Files.readString(storage.getAddressBookFilePath());
        var modified = Files.getLastModifiedTime(storage.getAddressBookFilePath());
        assertEquals("Departments:\nEngineering\nUnused", logic.execute("depart --list").getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertEquals(bytes, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(modified, Files.getLastModifiedTime(storage.getAddressBookFilePath()));
    }

    @Test
    void renameThenUndo_doesNotRestoreObsoleteDepartment() throws Exception {
        logic.execute("depart --add Engineering");
        logic.execute("add --name Alice --department Engineering");
        logic.execute("delete 1");
        logic.execute("depart --edit Engineering Sales");
        logic.execute("undo");
        assertEquals("Sales", model.getFilteredPersonList().getFirst().getDepartment().orElseThrow().value);
        assertFalse(model.hasDepartment(new Department("Engineering")));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        logic.execute("clear");
        assertTrue(model.getAddressBook().getDepartmentList().isEmpty());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private LogicManager logicFor(ModelManager target) {
        return new LogicManager(target, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
    }
}
