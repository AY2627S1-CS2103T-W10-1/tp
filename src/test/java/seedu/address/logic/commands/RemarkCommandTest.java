package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.RemarkCommandParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;
import seedu.address.testutil.PersonBuilder;

public class RemarkCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addAndReplaceRemark_preservesOtherFields() throws Exception {
        Person original = model.getFilteredPersonList().get(0);
        new RemarkCommandParser().parse(" 1 r/Likes to swim").execute(model);
        assertEquals(new PersonBuilder(original).withRemark("Likes to swim").build(),
                model.getFilteredPersonList().get(0));

        CommandResult result = new RemarkCommandParser().parse(" 1 r/Likes to read").execute(model);
        assertEquals(new PersonBuilder(original).withRemark("Likes to read").build(),
                model.getFilteredPersonList().get(0));
        assertEquals(String.format(RemarkCommand.MESSAGE_REMARK_SUCCESS, original.getName(), "Likes to read"),
                result.getFeedbackToUser());
    }

    @Test
    public void execute_emptyRemark_clearsRemark() throws Exception {
        for (String input : new String[] {" 1", " 1 r/"}) {
            new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Existing remark")).execute(model);
            new RemarkCommandParser().parse(input).execute(model);
            assertEquals(new Remark(""), model.getFilteredPersonList().get(0).getRemark());
        }
    }

    @Test
    public void execute_filteredList_updatesDisplayedPerson() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Updated")).execute(model);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(new PersonBuilder(original).withRemark("Updated").build(),
                model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index invalidIndex = Index.fromZeroBased(model.getFilteredPersonList().size());
        assertCommandFailure(new RemarkCommand(invalidIndex, new Remark("test")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        assertCommandFailure(new RemarkCommand(INDEX_SECOND_PERSON, new Remark("test")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }
}
