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
        logic.execute("add --name Alice --phone 95555555 --department Engineering");
        logic.execute("add --name Bob --phone 95555555 --department Engineering");
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
        logic.execute("add --phone 95555555 --name Alice --department Engineering");
        AddressBook before = new AddressBook(model.getAddressBook());
        String bytes = Files.readString(storage.getAddressBookFilePath());
        for (String command : List.of("edit 1 --department Unknown --phone 91234567",
                "edit 1 --department engineering", "add --phone 95555555 --name Bob --department Unknown",
                "depart --add Engineering", "depart --edit Unknown Sales", "depart --edit Engineering Sales")) {
            assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertEquals(before, model.getAddressBook());
            assertEquals(bytes, Files.readString(storage.getAddressBookFilePath()));
        }
        for (String command : List.of("edit 1 --department", "edit 1 -d Engineering --department Sales",
                "edit 1 -d !", "depart --list extra", "depart --add Sales --list", "depart 1 --set Sales")) {
            assertThrows(ParseException.class, () ->
                            logic.execute(command), command);
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
        logic.execute("add --phone 95555555 --name Alice --department Engineering");
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
        logic.execute("add --phone 95555555 --name Alice --department Engineering");
        logic.execute("delete 1");
        logic.execute("depart --edit Engineering Sales");
        logic.execute("undo");
        assertEquals("Sales", model.getFilteredPersonList().getFirst().getDepartment().orElseThrow().value);
        assertFalse(model.hasDepartment(new Department("Engineering")));
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        logic.execute("clear --confirm");
        assertTrue(model.getAddressBook().getDepartmentList().isEmpty());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void addAndEdit_normalizeDepartmentBeforeCatalogueLookup() throws Exception {
        for (String name : List.of("abc", "Customer  Support", "a".repeat(50))) {
            logic.execute("depart --add \"" + name + "\"");
            for (String option : List.of("--department", "-d")) {
                for (String input : List.of("\"" + name + "\"", "\"  " + name + "  \"")) {
                    logic.execute("add --phone 95555555 --name Alice " + option + " " + input);
                    assertEquals(name, model.getFilteredPersonList().getFirst()
                            .getDepartment().orElseThrow().value);
                    logic.execute("add --phone 95555555 --name Bob");
                    logic.execute("edit 2 " + option + " " + input);
                    assertEquals(name, model.getFilteredPersonList().get(1)
                            .getDepartment().orElseThrow().value);
                    assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
                    logic.execute("delete 2");
                    logic.execute("delete 1");
                }
            }
        }
    }

    @Test
    void addAndEdit_invalidNormalizedDepartment_preserveState() throws Exception {
        logic.execute("depart --add abc");
        logic.execute("add --phone 95555555 --name Alice --department abc");
        AddressBook before = new AddressBook(model.getAddressBook());
        String bytes = Files.readString(storage.getAddressBookFilePath());
        for (String option : List.of("--department", "-d")) {
            for (String prefix : List.of("add --phone 95555555 --name Bob ", "edit 1 --phone 91234567 ")) {
                for (String input : List.of("   ", " a ", " ! ", " " + "a".repeat(51) + " ")) {
                    ParseException exception = assertThrows(ParseException.class, () ->
                            logic.execute(prefix + option + " \"" + input + "\""));
                    assertEquals(Department.MESSAGE_CONSTRAINTS, exception.getMessage());
                    assertEquals(before, model.getAddressBook());
                    assertEquals(bytes, Files.readString(storage.getAddressBookFilePath()));
                }
                CommandException exception = assertThrows(CommandException.class, () ->
                        logic.execute(prefix + option + " \" Unknown \""));
                assertTrue(exception.getMessage().contains("Unknown"));
                assertEquals(before, model.getAddressBook());
                assertEquals(bytes, Files.readString(storage.getAddressBookFilePath()));
            }
        }
    }

    @Test
    void paddedCatalogueNames_supportAddEditRenameAndUndo() throws Exception {
        logic.execute("depart --add \" abc \"");
        assertEquals(List.of(new Department("abc")), model.getAddressBook().getDepartmentList());
        assertThrows(CommandException.class, () -> logic.execute("depart --add abc"));
        logic.execute("add --phone 95555555 --name Alice --department \" abc \"");
        logic.execute("add --phone 95555555 --name Bob");
        logic.execute("edit 2 --department \" abc \"");
        logic.execute("delete 2");
        logic.execute("depart --edit \" abc \" \" Customer  Support \"");
        logic.execute("undo");
        for (Person person : model.getAddressBook().getPersonList()) {
            assertEquals("Customer  Support", person.getDepartment().orElseThrow().value);
        }
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void deleteDepartment_clearsHiddenAssignmentsAndPreservesUndoHistory() throws Exception {
        logic.execute("depart --add abc");
        logic.execute("depart --add Other");
        logic.execute("add --phone 95555555 --name Alice --department abc --tag friend");
        logic.execute("add --phone 95555555 --name Bob --department abc");
        logic.execute("add --phone 95555555 --name Carol --department Other");
        logic.execute("add --phone 95555555 --name Dana --department abc");
        logic.execute("add --phone 95555555 --name Eve --department Other");
        logic.execute("delete 5");
        logic.execute("delete 4");
        List<Person> before = List.copyOf(model.getAddressBook().getPersonList());
        logic.execute("find Alice");
        assertEquals("Deleted department: abc. Associated contacts now have no department.",
                logic.execute("depart --delete \" abc \"").getFeedbackToUser());
        assertEquals(1, model.getFilteredPersonList().size());
        assertFalse(model.hasDepartment(new Department("abc")));
        assertEquals(before.stream().map(person -> person.withoutDepartment(new Department("abc"))).toList(),
                model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        assertEquals("Restored deleted department: abc", logic.execute("undo").getFeedbackToUser());
        assertEquals(before, model.getAddressBook().getPersonList());
        logic.execute("undo");
        assertEquals("Dana", model.getAddressBook().getPersonList().getLast().getName().fullName);
        assertEquals(new Department("abc"),
                model.getAddressBook().getPersonList().getLast().getDepartment().orElseThrow());
        logic.execute("undo");
        assertEquals("Eve", model.getAddressBook().getPersonList().getLast().getName().fullName);
        assertEquals(new Department("Other"),
                model.getAddressBook().getPersonList().getLast().getDepartment().orElseThrow());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void deleteDepartment_invalidRequestsPreserveDataAndUndo() throws Exception {
        logic.execute("depart --add abc");
        logic.execute("add --phone 95555555 --name Alice --department abc");
        logic.execute("delete 1");
        AddressBook before = new AddressBook(model.getAddressBook());
        String bytes = Files.readString(storage.getAddressBookFilePath());
        for (String command : List.of("depart --delete", "depart --delete abc extra",
                "depart --delete !", "depart --delete \" \"", "depart --delete abc --list")) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(before, model.getAddressBook());
            assertEquals(bytes, Files.readString(storage.getAddressBookFilePath()));
        }
        CommandException failure = assertThrows(CommandException.class, () ->
                logic.execute("depart --delete ABC"));
        assertEquals("Department does not exist: ABC. Use depart --list or depart --add DEPARTMENT.",
                failure.getMessage());
        assertEquals(before, model.getAddressBook());
        assertEquals(bytes, Files.readString(storage.getAddressBookFilePath()));
        logic.execute("undo");
        assertEquals(new Department("abc"), model.getFilteredPersonList().getFirst().getDepartment().orElseThrow());
    }

    @Test
    void undoDepartment_keepsLaterEditsAndRejectsCatalogueConflict() throws Exception {
        logic.execute("depart --add abc");
        logic.execute("depart --add Other");
        logic.execute("add --phone 95555555 --name Alice --department abc");
        logic.execute("add --phone 95555555 --name Bob --department abc");
        logic.execute("depart --delete abc");
        logic.execute("edit 1 --name Alicia --phone 91234567");
        logic.execute("edit 2 --department Other");
        logic.execute("depart --add abc");
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> logic.execute("undo"));
        assertEquals(before, model.getAddressBook());
        // Rename the conflicting entry without adding another deletion to the undo stack.
        logic.execute("depart --edit abc Spare");
        logic.execute("undo");
        Person alice = model.getAddressBook().getPersonList().getFirst();
        assertEquals("Alicia", alice.getName().fullName);
        assertEquals("91234567", alice.getPhone().value);
        assertEquals(new Department("abc"), alice.getDepartment().orElseThrow());
        assertEquals(new Department("Other"),
                model.getAddressBook().getPersonList().get(1).getDepartment().orElseThrow());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    void undoDepartment_interleavedDeletionsAndUnusedDepartment() throws Exception {
        logic.execute("depart --add abc");
        logic.execute("add --phone 95555555 --name Alice --department abc");
        logic.execute("depart --delete abc");
        logic.execute("delete 1");
        logic.execute("undo");
        assertTrue(model.getAddressBook().getPersonList().getFirst().getDepartment().isEmpty());
        logic.execute("undo");
        assertEquals(new Department("abc"),
                model.getAddressBook().getPersonList().getFirst().getDepartment().orElseThrow());
        logic.execute("depart --add Unused");
        logic.execute("depart --delete Unused");
        logic.execute("undo");
        assertTrue(model.hasDepartment(new Department("Unused")));
        assertThrows(CommandException.class, () -> logic.execute("undo"));
    }

    @Test
    void undoDepartment_preservesUnrelatedEditsWithContactDeletionHistory() throws Exception {
        logic.execute("depart --add abc");
        logic.execute("add --phone 95555555 --name Alice --department abc");
        logic.execute("add --phone 95555555 --name Bob");
        logic.execute("add --phone 95555555 --name Carol");
        logic.execute("delete 3");
        logic.execute("depart --delete abc");
        logic.execute("edit 2 --phone 91234567");
        logic.execute("undo");
        assertEquals("91234567", model.getAddressBook().getPersonList().get(1).getPhone().value);
        assertTrue(model.getAddressBook().getPersonList().get(1).getDepartment().isEmpty());
        assertEquals(new Department("abc"),
                model.getAddressBook().getPersonList().getFirst().getDepartment().orElseThrow());
        logic.execute("undo");
        assertEquals("Carol", model.getAddressBook().getPersonList().getLast().getName().fullName);
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private LogicManager logicFor(ModelManager target) {
        return new LogicManager(target, new StorageManager(storage,
                new JsonUserPrefsStorage(folder.resolve("preferences.json"))));
    }
}
