package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.help.HelpPage;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";

    /** Detailed usage, supported options and examples for this command. */
    public static final HelpPage HELP_PAGE = new HelpPage(COMMAND_WORD, String.join("\n",
            "Deletes all contacts from the address book.",
            "Usage: clear",
            "",
            "Extra parameters are ignored. Clearing the address book cannot be undone.",
            "Earlier individual deletions remain available to undo in this session.",
            "",
            "Examples:",
            "  clear"));

    public static final String MESSAGE_SUCCESS = "Address book has been cleared!";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
