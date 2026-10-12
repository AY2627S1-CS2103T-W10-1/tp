package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ContactLoadStatus;
import seedu.address.model.Model;
import seedu.address.model.SortField;

/**
 * Sorts the currently displayed contacts in ascending order without changing stored data.
 */
public class SortCommand extends Command {
    public static final String COMMAND_WORD = "sort";
    public static final String MESSAGE_USAGE = "sort -b/--by <department|dept|tag|tags>";
    public static final String MESSAGE_SUCCESS = "Sorted %1$d contacts by %2$s.";
    public static final String MESSAGE_EMPTY = "No contacts found to sort.";
    public static final String MESSAGE_LOAD_FAILURE =
            "Unable to load contacts for sorting. Please restart the application or check the data file.";
    public static final String MESSAGE_INVALID_DATA =
            "Unable to sort contacts because some stored contact data is invalid.";

    private final SortField field;

    /**
     * Creates a command that sorts by the specified field.
     */
    public SortCommand(SortField field) {
        this.field = requireNonNull(field);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (model.getContactLoadStatus() == ContactLoadStatus.UNREADABLE) {
            throw new CommandException(MESSAGE_LOAD_FAILURE);
        }
        if (model.getContactLoadStatus() == ContactLoadStatus.INVALID) {
            throw new CommandException(MESSAGE_INVALID_DATA);
        }
        if (model.getFilteredPersonList().isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY);
        }

        try {
            model.sortFilteredPersonList(field);
        } catch (IllegalArgumentException e) {
            throw new CommandException(MESSAGE_INVALID_DATA, e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                model.getFilteredPersonList().size(), field.getDisplayName()));
    }

    @Override
    public boolean requiresStorageSave() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof SortCommand otherCommand && field == otherCommand.field);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("field", field)
                .toString();
    }
}
