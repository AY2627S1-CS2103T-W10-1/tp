package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.SortField;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

class DepartCommandTest {
    private final ModelManager model = new ModelManager();
    private final Department engineering = new Department("Engineering");
    private final Department sales = new Department("Sales");

    @Test
    void execute_listIncludesUnusedDepartments_preservesDisplay() throws Exception {
        assertEquals(DepartCommand.MESSAGE_EMPTY, DepartCommand.list().execute(model).getFeedbackToUser());
        Person first = new PersonBuilder().withName("Alice").withDepartment("Sales").build();
        Person second = new PersonBuilder().withName("Bob").withDepartment("Engineering").build();
        model.addPerson(first);
        model.addPerson(second);
        model.addDepartment(new Department("Unused"));
        model.sortFilteredPersonList(SortField.DEPARTMENT);
        List<Person> display = List.copyOf(model.getFilteredPersonList());
        AddressBook before = new AddressBook(model.getAddressBook());
        assertEquals("Departments:\nSales\nEngineering\nUnused",
                DepartCommand.list().execute(model).getFeedbackToUser());
        assertEquals(display, model.getFilteredPersonList());
        model.updateFilteredPersonList(person -> person.equals(first));
        assertTrue(DepartCommand.list().execute(model).getFeedbackToUser().contains("Engineering"));
        assertEquals(List.of(first), model.getFilteredPersonList());
        assertEquals(before, model.getAddressBook());
        assertFalse(DepartCommand.list().requiresStorageSave());
    }

    @Test
    void execute_addAndRename_updatesHiddenContactsAndUndoHistory() throws Exception {
        DepartCommand.add(engineering).execute(model);
        Person alice = new PersonBuilder().withName("Alice").withDepartment("Engineering").build();
        Person bob = new PersonBuilder().withName("Bob").withDepartment("Engineering").build();
        Person chen = new PersonBuilder().withName("Chen").withDepartment("Engineering").build();
        model.addPerson(alice);
        model.addPerson(bob);
        model.addPerson(chen);
        model.deletePerson(chen);
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Alice"));
        DepartCommand.rename(engineering, sales).execute(model);
        assertEquals(List.of(sales), model.getAddressBook().getDepartmentList());
        assertEquals(List.of(new PersonBuilder(alice).withDepartment("Sales").build(),
                new PersonBuilder(bob).withDepartment("Sales").build()), model.getAddressBook().getPersonList());
        assertEquals(1, model.getFilteredPersonList().size());
        model.restoreLastDeletedPerson();
        assertEquals(new PersonBuilder(chen).withDepartment("Sales").build(),
                model.getAddressBook().getPersonList().getLast());
        assertFalse(model.hasDepartment(engineering));
    }

    @Test
    void execute_invalidCatalogueChanges_leaveModelUnchanged() throws Exception {
        DepartCommand.add(engineering).execute(model);
        DepartCommand.add(sales).execute(model);
        assertCommandFailure(DepartCommand.add(engineering), model,
                String.format(DepartCommand.MESSAGE_DUPLICATE, engineering));
        assertCommandFailure(DepartCommand.rename(engineering, sales), model,
                String.format(DepartCommand.MESSAGE_DUPLICATE, sales));
        Department unknown = new Department("Unknown");
        assertCommandFailure(DepartCommand.rename(unknown, sales), model,
                String.format(DepartCommand.MESSAGE_UNKNOWN, unknown));
        DepartCommand.rename(engineering, engineering).execute(model);
        assertEquals(List.of(engineering, sales), model.getAddressBook().getDepartmentList());
    }

    @Test
    void equals_comparesOperationAndBothNames() {
        assertEquals(DepartCommand.list(), DepartCommand.list());
        assertEquals(DepartCommand.add(engineering), DepartCommand.add(engineering));
        assertEquals(DepartCommand.rename(engineering, sales), DepartCommand.rename(engineering, sales));
        assertNotEquals(DepartCommand.list(), DepartCommand.add(engineering));
        assertNotEquals(DepartCommand.add(sales), DepartCommand.add(engineering));
        assertNotEquals(DepartCommand.rename(sales, engineering), DepartCommand.rename(engineering, sales));
        assertNotEquals(DepartCommand.rename(engineering, sales), DepartCommand.rename(engineering, engineering));
        assertNotEquals(null, DepartCommand.list());
        assertTrue(DepartCommand.add(engineering).requiresStorageSave());
    }
}
