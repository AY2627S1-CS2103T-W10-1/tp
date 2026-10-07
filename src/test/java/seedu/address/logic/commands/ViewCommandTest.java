package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ViewCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndex_showsAllDetailsWithoutChangingModel() throws Exception {
        Person person = new PersonBuilder().withTags("vendor", "client").build();
        model.addPerson(person);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        int index = model.getFilteredPersonList().size();

        CommandResult result = new ViewCommand(Index.fromOneBased(index)).execute(model);

        assertEquals("Contact details:\nName: Amy Bee\nPhone: 85355255\nEmail: amy@gmail.com\n"
                + "Address: 123, Jurong West Ave 6, #08-111\nTags: client, vendor", result.getFeedbackToUser());
        assertTrue(result.isShowDetails());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertEquals(expectedModel, model);
    }

    @Test
    public void execute_filteredList_usesDisplayedIndexAndPreservesFilter() throws Exception {
        Person target = model.getFilteredPersonList().get(2);
        model.updateFilteredPersonList(target::equals);
        List<Person> displayedBefore = List.copyOf(model.getFilteredPersonList());
        List<Person> storedBefore = List.copyOf(model.getAddressBook().getPersonList());

        CommandResult result = new ViewCommand(Index.fromOneBased(1)).execute(model);

        assertTrue(result.getFeedbackToUser().contains("Name: " + target.getName() + "\n"));
        assertEquals(displayedBefore, model.getFilteredPersonList());
        assertEquals(storedBefore, model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_sortedListWithSameNames_selectsExactDisplayedRow() throws Exception {
        Person first = new PersonBuilder().withName("Jack").withPhone("12345678").build();
        Person second = new PersonBuilder().withName("Jack").withPhone("87654321").build();
        // Models with future sorting/duplicate-name support still expose the displayed list through this API.
        Model displayedModel = new ModelManager() {
            @Override
            public ObservableList<Person> getFilteredPersonList() {
                return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(second, first));
            }
        };

        CommandResult result = new ViewCommand(Index.fromOneBased(1)).execute(displayedModel);
        assertTrue(result.getFeedbackToUser().contains("Phone: 87654321\n"));
        assertEquals(List.of(second, first), displayedModel.getFilteredPersonList());
    }

    @Test
    public void execute_noTags_displaysNone() throws Exception {
        model.addPerson(new PersonBuilder().withTags().build());
        CommandResult result = new ViewCommand(Index.fromOneBased(model.getFilteredPersonList().size()))
                .execute(model);
        assertTrue(result.getFeedbackToUser().endsWith("Tags: None"));
    }

    @Test
    public void execute_longFields_doesNotTruncate() throws Exception {
        String longName = "Alice ".repeat(100).trim();
        String longAddress = "Long address ".repeat(100).trim();
        model.addPerson(new PersonBuilder().withName(longName).withAddress(longAddress).build());
        CommandResult result = new ViewCommand(Index.fromOneBased(model.getFilteredPersonList().size()))
                .execute(model);
        assertTrue(result.getFeedbackToUser().contains("Name: " + longName + "\n"));
        assertTrue(result.getFeedbackToUser().contains("Address: " + longAddress + "\n"));
    }

    @Test
    public void execute_outOfRange_reportsDisplayedRange() {
        int size = model.getFilteredPersonList().size();
        assertThrows(CommandException.class, String.format(ViewCommand.MESSAGE_INVALID_CONTACT_ID, size), ()
            -> new ViewCommand(Index.fromOneBased(size + 1)).execute(model));
    }

    @Test
    public void execute_indexOnlyInUnfilteredList_throwsCommandException() {
        model.updateFilteredPersonList(person -> person.equals(model.getAddressBook().getPersonList().get(0)));
        assertThrows(CommandException.class, String.format(ViewCommand.MESSAGE_INVALID_CONTACT_ID, 1), ()
            -> new ViewCommand(Index.fromOneBased(2)).execute(model));
    }

    @Test
    public void execute_emptyFilteredList_reportsNoContact() {
        model.updateFilteredPersonList(person -> false);
        assertThrows(CommandException.class, ViewCommand.MESSAGE_EMPTY_LIST, ()
            -> new ViewCommand(Index.fromOneBased(1)).execute(model));
    }

    @Test
    public void execute_emptyAddressBook_reportsNoContact() {
        assertThrows(CommandException.class, ViewCommand.MESSAGE_EMPTY_LIST, ()
            -> new ViewCommand(Index.fromOneBased(1)).execute(new ModelManager()));
    }

    @Test
    public void constructor_nullIndex_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ViewCommand(Index.fromOneBased(1)).execute(null));
    }

    @Test
    public void isReadOnly_returnsTrue() {
        assertTrue(new ViewCommand(Index.fromOneBased(1)).isReadOnly());
        assertFalse(new DeleteCommand(Index.fromOneBased(1)).isReadOnly());
    }

    @Test
    public void equals() {
        ViewCommand first = new ViewCommand(Index.fromOneBased(1));
        assertEquals(first, first);
        assertEquals(first, new ViewCommand(Index.fromOneBased(1)));
        assertFalse(first.equals(new ViewCommand(Index.fromOneBased(2))));
        assertFalse(first.equals(null));
        assertFalse(first.equals(new ListCommand()));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        assertEquals(ViewCommand.class.getCanonicalName() + "{targetIndex=" + index + "}",
                new ViewCommand(index).toString());
    }
}
