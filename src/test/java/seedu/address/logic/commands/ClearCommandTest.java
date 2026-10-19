package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.SortField;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

public class ClearCommandTest {

    @Test
    public void execute_unconfirmedEmptyAddressBook_showsWarning() {
        Model model = new ModelManager();
        assertCommandSuccess(new ClearCommand(), model,
                String.format(ClearCommand.MESSAGE_CONFIRMATION, 0), new ModelManager());
    }

    @Test
    public void execute_unconfirmedFilteredAndSortedAddressBook_preservesState() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.deletePerson(ALICE);
        int storedCount = model.getAddressBook().getPersonList().size();
        model.sortFilteredPersonList(SortField.TAGS);
        AddressBook storedBefore = new AddressBook(model.getAddressBook());
        List<Person> sortedBefore = List.copyOf(model.getFilteredPersonList());

        CommandResult result = new ClearCommand().execute(model);

        assertEquals(String.format(ClearCommand.MESSAGE_CONFIRMATION, storedCount), result.getFeedbackToUser());
        assertEquals(sortedBefore, model.getFilteredPersonList());
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        List<Person> filteredBefore = List.copyOf(model.getFilteredPersonList());

        result = new ClearCommand().execute(model);

        assertEquals(String.format(ClearCommand.MESSAGE_CONFIRMATION, storedCount), result.getFeedbackToUser());
        assertEquals(storedBefore, model.getAddressBook());
        assertEquals(filteredBefore, model.getFilteredPersonList());
        assertEquals(ALICE, model.getLastDeletedPerson());
    }

    @Test
    public void execute_confirmedEmptyAddressBook_success() {
        Model model = new ModelManager();
        assertCommandSuccess(new ClearCommand(true), model, ClearCommand.MESSAGE_SUCCESS, new ModelManager());
        assertFalse(model.hasDeletedPerson());
    }

    @Test
    public void execute_confirmedFilteredAddressBook_clearsAllContacts() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Model expectedModel = new ModelManager();
        assertCommandSuccess(new ClearCommand(true), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
        assertFalse(model.hasDeletedPerson());
    }

    @Test
    public void execute_confirmedAddressBook_preservesEarlierDeletion() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.deletePerson(ALICE);

        new ClearCommand(true).execute(model);

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(ALICE, model.getLastDeletedPerson());

        new UndoCommand().execute(model);

        assertEquals(List.of(ALICE), model.getAddressBook().getPersonList());
        assertFalse(model.hasDeletedPerson());
    }

    @Test
    public void requiresStorageSave_onlyConfirmedCommandSaves() {
        assertFalse(new ClearCommand().requiresStorageSave());
        assertTrue(new ClearCommand(true).requiresStorageSave());
    }

    @Test
    public void equals() {
        ClearCommand command = new ClearCommand();
        assertTrue(command.equals(command));
        assertTrue(command.equals(new ClearCommand(false)));
        assertTrue(new ClearCommand(true).equals(new ClearCommand(true)));
        assertFalse(command.equals(new ClearCommand(true)));
        assertFalse(command.equals(new UndoCommand()));
        assertFalse(command.equals(null));
    }
}
