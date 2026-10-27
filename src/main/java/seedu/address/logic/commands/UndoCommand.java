package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.help.HelpPage;
import seedu.address.model.Model;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;

/**
 * Restores, or discards without restoring, the most recently deleted contact or department in this session.
 */
public class UndoCommand extends Command {

    public static final String COMMAND_WORD = "undo";

    /** Detailed usage, supported options and examples for this command. */
    public static final HelpPage HELP_PAGE = new HelpPage(COMMAND_WORD, String.join("\n",
            "Restores the most recently deleted contact or department in this session.",
            "Usage: undo [--discard-latest]",
            "",
            "Repeated calls restore earlier deletions, newest first.",
            "Restoration fails if there is no deletion to undo or a same-name contact shares a phone or email.",
            "A restored contact must have a phone number or email address.",
            "The restored contact is appended to the stored list; all contacts are shown in stored order.",
            "Undoes delete and depart --delete in reverse order, not clear, add or edit.",
            "Run undo --discard-latest to drop the most recent deletion without restoring it.",
            "Only --discard-latest is accepted. A discarded deletion can no longer be restored.",
            "",
            "Examples:",
            "  undo",
            "  undo --discard-latest"));

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Restores the most recently deleted contact or department in this session.\n"
            + "Use " + COMMAND_WORD + " --discard-latest to drop it from the history without restoring it.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Restored deleted person: %1$s";
    public static final String MESSAGE_NO_DELETION = "There are no deletions to undo in this session.";
    public static final String MESSAGE_DUPLICATE_PERSON =
            "Cannot restore the deleted person because this person already exists: %1$s\n"
            + "Edit that contact's name, phone or email so it no longer matches, then run undo again, "
            + "or run undo --discard-latest to drop this deletion.";
    public static final String MESSAGE_MISSING_CONTACT_INFORMATION =
            "Cannot restore the deleted person because it has no phone number or email address: %1$s\n"
            + "Run undo --discard-latest to drop this deletion, then add the contact again if needed.";

    public static final String MESSAGE_DEPARTMENT_RESTORED = "Restored deleted department: %1$s";
    public static final String MESSAGE_DEPARTMENT_EXISTS =
            "Cannot restore the deleted department because it already exists: %1$s";

    public static final String MESSAGE_PERSON_DISCARDED =
            "Discarded deleted person from undo history: %1$s\nIt can no longer be restored.";
    public static final String MESSAGE_DEPARTMENT_DISCARDED =
            "Discarded deleted department from undo history: %1$s\nIt can no longer be restored.";

    private final boolean isDiscarding;

    /**
     * Creates a command that restores the most recent deletion.
     */
    public UndoCommand() {
        this(false);
    }

    /**
     * Creates an undo command that either restores the most recent deletion or discards it without restoring it.
     */
    public UndoCommand(boolean isDiscarding) {
        this.isDiscarding = isDiscarding;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (isDiscarding) {
            return discardLastDeletion(model);
        }

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
        if (!personToRestore.hasContactInformation()) {
            throw new CommandException(
                    String.format(MESSAGE_MISSING_CONTACT_INFORMATION, Messages.format(personToRestore)));
        }
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

    private CommandResult discardLastDeletion(Model model) throws CommandException {
        Department department = model.getLastDeletedDepartment();
        if (department == null && !model.hasDeletedPerson()) {
            throw new CommandException(MESSAGE_NO_DELETION);
        }
        String message = department != null
                ? String.format(MESSAGE_DEPARTMENT_DISCARDED, department)
                : String.format(MESSAGE_PERSON_DISCARDED, Messages.format(model.getLastDeletedPerson()));
        model.discardLastDeletion();
        return new CommandResult(message);
    }

    /**
     * Returns false when discarding, which changes only the in-memory history and not the saved data.
     */
    @Override
    public boolean requiresStorageSave() {
        return !isDiscarding;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof UndoCommand otherCommand && isDiscarding == otherCommand.isDiscarding);
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(isDiscarding);
    }
}
