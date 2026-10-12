package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code UndoCommand}.
 */
public class UndoCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_afterDelete_restoresPerson() {
        model.deletePerson(ALICE);

        // a new ModelManager starts with no deletion history, matching the model after the undo
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(ALICE);

        String expectedMessage = String.format(UndoCommand.MESSAGE_SUCCESS, Messages.format(ALICE));
        assertCommandSuccess(new UndoCommand(), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_showsAllPersons() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.deletePerson(personToDelete);

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(personToDelete);

        String expectedMessage = String.format(UndoCommand.MESSAGE_SUCCESS, Messages.format(personToDelete));
        assertCommandSuccess(new UndoCommand(), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_multipleDeletes_restoresMostRecentFirst() throws Exception {
        model.deletePerson(ALICE);
        model.deletePerson(BENSON);

        new UndoCommand().execute(model);

        assertTrue(model.hasPerson(BENSON));
        assertFalse(model.hasPerson(ALICE));
        assertEquals(ALICE, model.getLastDeletedPerson());
    }

    @Test
    public void execute_noDeletion_throwsCommandException() {
        assertCommandFailure(new UndoCommand(), model, UndoCommand.MESSAGE_NO_DELETION);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandExceptionAndKeepsDeletion() {
        model.deletePerson(ALICE);
        model.addPerson(ALICE);

        String expectedMessage = String.format(UndoCommand.MESSAGE_DUPLICATE_PERSON, Messages.format(ALICE));
        assertCommandFailure(new UndoCommand(), model, expectedMessage);

        // assertCommandFailure does not check the deletion history
        assertEquals(ALICE, model.getLastDeletedPerson());
    }

    @Test
    public void equals() {
        UndoCommand undoCommand = new UndoCommand();

        // same object -> returns true
        assertTrue(undoCommand.equals(undoCommand));

        // different UndoCommand -> returns true
        assertTrue(undoCommand.equals(new UndoCommand()));

        // different types -> returns false
        assertFalse(undoCommand.equals(new ClearCommand()));

        // null -> returns false
        assertFalse(undoCommand.equals(null));
    }
}
