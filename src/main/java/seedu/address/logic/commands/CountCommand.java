package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.help.HelpPage;
import seedu.address.model.Model;

/**
 * Counts displayed or stored contacts without changing the address book or filter.
 */
public class CountCommand extends Command {
    public static final String COMMAND_WORD = "count";
    public static final String MESSAGE_USAGE = "count [--all]\n"
            + "Counts displayed contacts, or all stored contacts with --all.\n"
            + "Example: count --all";
    public static final String MESSAGE_DISPLAYED = "Contacts in displayed list: %1$d.";
    public static final String MESSAGE_ALL = "Contacts stored: %1$d.";
    public static final HelpPage HELP_PAGE = new HelpPage(COMMAND_WORD, String.join("\n",
            "Counts contacts without changing the current list or saving data.",
            "Usage: count [--all]",
            "",
            "Options:",
            "  --all  Count every stored contact, including contacts hidden by a filter.",
            "",
            "Without --all, counts only contacts in the currently displayed list.",
            "An empty list has zero contacts. Extra arguments and repeated flags are rejected.",
            "",
            "Examples:",
            "  count",
            "  count --all"));

    private final boolean isAllContacts;

    /**
     * Creates a command for either all stored contacts or the displayed list.
     */
    public CountCommand(boolean isAllContacts) {
        this.isAllContacts = isAllContacts;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        int count = isAllContacts ? model.getAddressBook().getPersonList().size()
                : model.getFilteredPersonList().size();
        return new CommandResult(String.format(isAllContacts ? MESSAGE_ALL : MESSAGE_DISPLAYED, count));
    }

    @Override
    public boolean requiresStorageSave() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof CountCommand otherCommand
                && isAllContacts == otherCommand.isAllContacts);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("isAllContacts", isAllContacts).toString();
    }
}
