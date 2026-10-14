package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.help.HelpPage;
import seedu.address.model.Model;
import seedu.address.model.SortField;

/**
 * Sorts the currently displayed contacts in ascending order without changing stored data.
 */
public class SortCommand extends Command {
    public static final String COMMAND_WORD = "sort";

    /** Detailed usage, supported options and examples for this command. */
    public static final HelpPage HELP_PAGE = new HelpPage(COMMAND_WORD, String.join("\n",
            "Sorts currently displayed contacts in ascending, case-insensitive order.",
            "Usage: sort --by FIELD",
            "",
            "Options:",
            "  -b, --by  Required field: department, dept, tag or tags.",
            "",
            "Preserves the active filter and stored contact order.",
            "Contacts with no sorting field appear last; equal keys retain their relative order.",
            "Tag sorting uses each contact's alphabetically smallest tag, ignoring case.",
            "",
            "Examples:",
            "  sort --by department",
            "  sort -b tags"));

    public static final String MESSAGE_USAGE = "sort -b/--by <department|dept|tag|tags>";
    public static final String MESSAGE_SUCCESS = "Sorted %1$d contacts by %2$s.";
    public static final String MESSAGE_EMPTY = "No contacts found to sort.";
    public static final String MESSAGE_INVALID_DATA =
            "Unable to sort contacts because some stored contact data is invalid.";

    private final SortField field;

    /**
     * Creates a command that sorts by the specified field.
     */
    public SortCommand(SortField field) {
        this.field = requireNonNull(field);
    }

    /**
     * Sorts the displayed contacts by the selected field in ascending order.
     * Preserves the active filter and stored contact order.
     *
     * @param model Model containing the contacts to sort.
     * @return Feedback containing the displayed contact count and selected field, or an empty-list message.
     * @throws CommandException If a displayed contact contains invalid sorting data.
     * @throws NullPointerException If {@code model} is null.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
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

    /**
     * Returns false because sorting changes only the display order and does not require a storage save.
     */
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
