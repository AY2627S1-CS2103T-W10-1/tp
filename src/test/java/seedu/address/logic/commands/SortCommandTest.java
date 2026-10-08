package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.SortField;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class SortCommandTest {
    private final ModelManager model = new ModelManager();
    private final Person alex = new PersonBuilder().withName("Alex").withTags("mentor", "Zulu").build();
    private final Person beatrice = new PersonBuilder().withName("Beatrice").withTags("client").build();

    @Test
    public void equals() {
        SortCommand command = new SortCommand(SortField.TAGS);
        assertTrue(command.equals(command));
        assertTrue(command.equals(new SortCommand(SortField.TAGS)));
        assertFalse(command.equals(new SortCommand(SortField.DEPARTMENT)));
        assertFalse(command.equals(null));
        assertFalse(command.equals(new ListCommand()));
    }

    @Test
    public void execute_tags_formatsNumberedContactsWithMissingDepartments() throws Exception {
        model.addPerson(alex);
        model.addPerson(beatrice);
        CommandResult result = new SortCommand(SortField.TAGS).execute(model);
        assertEquals("Contacts sorted by tags:\n"
                + "1. Beatrice | Department: Not assigned | Tags: client\n"
                + "2. Alex | Department: Not assigned | Tags: mentor, Zulu", result.getFeedbackToUser());
        assertEquals(List.of(alex, beatrice), model.getAddressBook().getPersonList());
        assertFalse(new SortCommand(SortField.TAGS).requiresStorageSave());
    }

    @Test
    public void execute_department_keepsCurrentOrderAndFormatsMissingTags() throws Exception {
        Person empty = new PersonBuilder().withName("No Tags").build();
        model.addPerson(empty);
        assertEquals("Contacts sorted by department:\n1. No Tags | Department: Not assigned | Tags: None",
                new SortCommand(SortField.DEPARTMENT).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_emptyFilteredList_returnsEmptyMessage() throws Exception {
        model.addPerson(alex);
        model.updateFilteredPersonList(person -> false);
        assertEquals(SortCommand.MESSAGE_EMPTY, new SortCommand(SortField.TAGS).execute(model).getFeedbackToUser());
        assertEquals(List.of(alex), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_deleteAfterSort_targetsFirstDisplayedContact() throws Exception {
        model.addPerson(alex);
        model.addPerson(beatrice);
        new SortCommand(SortField.TAGS).execute(model);
        new DeleteCommand(Index.fromOneBased(1)).execute(model);
        assertEquals(List.of(alex), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_invalidTag_returnsSpecifiedErrorAndPreservesOrder() {
        Set<Tag> tags = new HashSet<>();
        tags.add(null);
        Person invalid = new Person(alex.getName(), alex.getPhone(), alex.getEmail(), alex.getAddress(), tags);
        model.addPerson(invalid);
        for (SortField field : SortField.values()) {
            CommandException error = assertThrows(CommandException.class, () -> new SortCommand(field).execute(model));
            assertEquals(SortCommand.MESSAGE_INVALID_DATA, error.getMessage());
            assertEquals(List.of(invalid), model.getFilteredPersonList());
        }
    }
}
