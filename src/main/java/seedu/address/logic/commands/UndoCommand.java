package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.help.HelpPage;
import seedu.address.model.Model;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;

/**
 * Restores the most recently deleted contact or department in this session.
 */
public class UndoCommand extends Command {

    public static final String COMMAND_WORD = "undo";

    /** Detailed usage, supported options and examples for this command. */
    public static final HelpPage HELP_PAGE = new HelpPage(COMMAND_WORD, String.join("\n",
            "Restores the most recently deleted contact or department in this session.",
            "Usage: undo",
            "",
            "Accepts no parameters. Repeated calls restore earlier deletions, newest first.",
            "Restoration fails if there is no deletion to undo or a same-name contact shares a phone or email.",
            "The restored contact is appended to the stored list; all contacts are shown in stored order.",
            "Undoes delete and depart --delete in reverse order, not clear, add or edit.",
            "",
            "Examples:",
            "  undo"));

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Restores the most recently deleted contact or department in this session.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Restored deleted person: %1$s";
    public static final String MESSAGE_NO_DELETION = "There are no deletions to undo in this session.";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "Cannot restore the deleted person because this person already exists: %1$s";

    public static final String MESSAGE_DEPARTMENT_RESTORED = "Restored deleted department: %1$s";
    public static final String MESSAGE_DEPARTMENT_EXISTS =
            "Cannot restore the deleted department because it already exists: %1$s";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Department department = model.getLastDeletedDepartment();
        if (department != null) {
            if (model.hasDepartment(department)) {
                throw new CommandException(String.format(MESSAGE_DEPARTMENT_EXISTS, department));
            }
            model.restoreLastDeletedDepartment();
            return new CommandResult(String.format(MESSAGE_DEPARTMENT_RESTORED, department));
        }
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
