package seedu.address.logic.commands;

import seedu.address.logic.help.HelpPage;
import seedu.address.model.Model;

/**
 * Terminates the program.
 */
public class ExitCommand extends Command {

    public static final String COMMAND_WORD = "exit";

    /** Detailed usage, supported options and examples for this command. */
    public static final HelpPage HELP_PAGE = new HelpPage(COMMAND_WORD, String.join("\n",
            "Closes the application and its help and contact-details windows.",
            "Usage: exit",
            "",
            "Extra parameters are ignored.",
            "",
            "Examples:",
            "  exit"));

    public static final String MESSAGE_EXIT_ACKNOWLEDGEMENT = "Exiting Address Book as requested ...";

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(MESSAGE_EXIT_ACKNOWLEDGEMENT, false, true);
    }

}
