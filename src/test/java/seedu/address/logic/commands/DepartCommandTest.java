package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;

class DepartCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    void execute_validIndex_replacesDepartment() {
        Department department = new Department("Engineering");
        DepartCommand command = new DepartCommand(INDEX_FIRST_PERSON, "1", department);
        Person original = model.getFilteredPersonList().get(0);
        Person updated = new Person(original.getName(), original.getPhone(), original.getEmail(),
                original.getAddress(), original.getTags(), department);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(original, updated);

        assertCommandSuccess(command, model, String.format(DepartCommand.MESSAGE_SUCCESS, Messages.format(updated)),
                expectedModel);
    }

    @Test
    void execute_invalidIndex_throwsCommandException() {
        Index missingIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DepartCommand command = new DepartCommand(missingIndex, "99", new Department("Engineering"));
        assertCommandFailure(command, model, String.format(DepartCommand.MESSAGE_CONTACT_NOT_FOUND, "99"));
    }

    @Test
    void equals() {
        DepartCommand command = new DepartCommand(INDEX_FIRST_PERSON, "1", new Department("Engineering"));
        assertTrue(command.equals(command));
        assertTrue(command.equals(new DepartCommand(INDEX_FIRST_PERSON, "1", new Department("Engineering"))));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ClearCommand()));
        assertFalse(command.equals(new DepartCommand(Index.fromOneBased(2), "2", new Department("Engineering"))));
        assertFalse(command.equals(new DepartCommand(INDEX_FIRST_PERSON, "1", new Department("Sales"))));
    }
}
