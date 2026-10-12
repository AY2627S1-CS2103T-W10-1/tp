package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Restores the most recently deleted person in this session.
 */
public class UndoCommand extends Command {

    public static final String COMMAND_WORD = "undo";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Restores the most recently deleted person in this session.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Restored deleted person: %1$s";
    public static final String MESSAGE_NO_DELETION = "There are no deletions to undo in this session.";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "Cannot restore the deleted person because this person already exists: %1$s";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (!model.hasDeletedPerson()) {
            throw new CommandException(MESSAGE_NO_DELETION);
        }

        Person personToRestore = model.getLastDeletedPerson();
        if (model.hasPerson(personToRestore)) {
            Person existingPerson = model.getAddressBook().getPersonList().stream()
                    .filter(personToRestore::isSamePerson)
                    .findFirst()
                    .orElseThrow();
            throw new CommandException(String.format(MESSAGE_DUPLICATE_PERSON, Messages.format(existingPerson)));
        }

        model.restoreLastDeletedPerson();
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(personToRestore)));
    }

    @Override
    public boolean equals(Object other) {
        // instanceof handles nulls
        return other instanceof UndoCommand;
    }
}
