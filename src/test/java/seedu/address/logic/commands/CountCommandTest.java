package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;

public class CountCommandTest {
    @Test
    public void execute_filteredList_distinguishesDisplayedAndStoredAndPreservesModel() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.updateFilteredPersonList(person -> person.equals(ALICE));
        AddressBook original = new AddressBook(model.getAddressBook());
        List<Person> displayed = List.copyOf(model.getFilteredPersonList());

        assertEquals(new CommandResult("Contacts in displayed list: 1."), new CountCommand(false).execute(model));
        assertEquals(new CommandResult("Contacts stored: 7."), new CountCommand(true).execute(model));
        assertEquals(original, model.getAddressBook());
        assertEquals(displayed, model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyFilter_doesNotMistakeHiddenContactsForEmptyAddressBook() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.updateFilteredPersonList(person -> false);
        assertEquals(new CommandResult("Contacts in displayed list: 0."), new CountCommand(false).execute(model));
        assertEquals(new CommandResult("Contacts stored: 7."), new CountCommand(true).execute(model));
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_emptyAddressBook_reportsZeroForBothScopes() {
        Model model = new ModelManager();
        assertEquals(new CommandResult("Contacts in displayed list: 0."), new CountCommand(false).execute(model));
        assertEquals(new CommandResult("Contacts stored: 0."), new CountCommand(true).execute(model));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CountCommand(false).execute(null));
    }

    @Test
    public void count_neverRequestsStorageSave() {
        assertFalse(new CountCommand(false).requiresStorageSave());
        assertFalse(new CountCommand(true).requiresStorageSave());
    }

    @Test
    public void equals_distinguishesScopeAndRejectsOtherCommandTypes() {
        CountCommand displayed = new CountCommand(false);
        assertEquals(displayed, displayed);
        assertEquals(displayed, new CountCommand(false));
        assertEquals(new CountCommand(true), new CountCommand(true));
        assertFalse(displayed.equals(new CountCommand(true)));
        assertFalse(displayed.equals(new ListCommand()));
        assertFalse(displayed.equals(null));
    }

    @Test
    public void toString_includesScopeForDiagnostics() {
        assertEquals(CountCommand.class.getCanonicalName() + "{isAllContacts=false}",
                new CountCommand(false).toString());
        assertEquals(CountCommand.class.getCanonicalName() + "{isAllContacts=true}",
                new CountCommand(true).toString());
    }
}
