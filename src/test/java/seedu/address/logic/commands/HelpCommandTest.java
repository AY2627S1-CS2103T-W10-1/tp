package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.HelpCommand.SHOWING_HELP_MESSAGE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.help.HelpCatalog;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.SortField;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

public class HelpCommandTest {
    private Model model = new ModelManager();
    private Model expectedModel = new ModelManager();

    @Test
    public void execute_help_success() {
        CommandResult expectedResult = CommandResult.forHelp(SHOWING_HELP_MESSAGE, HelpCatalog.getOverview());
        assertCommandSuccess(new HelpCommand(), model, expectedResult, expectedModel);
    }

    @Test
    public void execute_manual_preservesContactsFilterSortAndDeletionHistory() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person deleted = model.getFilteredPersonList().getFirst();
        model.deletePerson(deleted);
        model.updateFilteredPersonList(person -> !person.getName().fullName.contains("Carl"));
        model.sortFilteredPersonList(SortField.TAGS);
        List<Person> stored = List.copyOf(model.getAddressBook().getPersonList());
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());

        for (var page : HelpCatalog.getPages()) {
            HelpCommand command = new HelpCommand(page);
            assertEquals(page, command.execute(model).getHelpPage().orElseThrow());
            assertFalse(command.requiresStorageSave());
            assertEquals(stored, model.getAddressBook().getPersonList());
            assertEquals(displayed, model.getFilteredPersonList());
            assertEquals(deleted, model.getLastDeletedPerson());
        }
        model.updateFilteredPersonList(person -> true);
        assertEquals(stored, model.getFilteredPersonList());
    }

    @Test
    public void equals_comparesSelectedPage() {
        assertEquals(new HelpCommand(), new HelpCommand(HelpCatalog.getOverview()));
        assertEquals(new HelpCommand().hashCode(), new HelpCommand(HelpCatalog.getOverview()).hashCode());
        assertNotEquals(new HelpCommand(), new HelpCommand(DepartCommand.HELP_PAGE));
        assertNotEquals(new HelpCommand(), null);
        assertNotEquals(new HelpCommand(), "help");
    }
    @Test
    public void equals_sameInstance_returnsTrue() {
        HelpCommand command = new HelpCommand();
        assertTrue(command.equals(command));
    }

    @Test
    public void constructor_nullPage_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new HelpCommand(null));
    }

    @Test
    public void toString_includesSelectedPage() {
        HelpCommand command = new HelpCommand(DepartCommand.HELP_PAGE);
        assertEquals(HelpCommand.class.getCanonicalName() + "{page=" + DepartCommand.HELP_PAGE + "}",
                command.toString());
    }

}
