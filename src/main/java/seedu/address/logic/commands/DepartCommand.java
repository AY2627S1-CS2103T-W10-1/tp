package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;

/** Assigns a department to a contact. */
public class DepartCommand extends Command {
    public static final String COMMAND_WORD = "depart";
    public static final String MESSAGE_USAGE = "depart CONTACT_ID --set DEPARTMENT";
    public static final String MESSAGE_CONTACT_NOT_FOUND = "Contact non-existent: No contact found with ID '%1$s'.";
    public static final String MESSAGE_SUCCESS = "Updated contact: %1$s";

    private final Index index;
    private final String contactId;
    private final Department department;

    public DepartCommand(Index index, String contactId, Department department) {
        this.index = requireNonNull(index);
        this.contactId = requireNonNull(contactId);
        this.department = requireNonNull(department);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> contacts = model.getFilteredPersonList();
        if (index.getZeroBased() >= contacts.size()) {
            throw new CommandException(String.format(MESSAGE_CONTACT_NOT_FOUND, contactId));
        }

        Person contact = contacts.get(index.getZeroBased());
        Person updatedContact = new Person(contact.getName(), contact.getPhone(), contact.getEmail(),
                contact.getAddress(), contact.getTags(), department);
        model.setPerson(contact, updatedContact);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(updatedContact)));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof DepartCommand otherCommand
                && index.equals(otherCommand.index) && department.equals(otherCommand.department));
    }
}
