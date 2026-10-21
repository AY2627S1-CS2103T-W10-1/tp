package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.logic.help.HelpPage;
import seedu.address.model.Model;

/**
 * Lists all persons in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    /** Detailed usage, supported options and examples for this command. */
    public static final HelpPage HELP_PAGE = new HelpPage(COMMAND_WORD, String.join("\n",
            "Shows all contacts and removes the active filter and display sorting.",
            "Usage: list",
            "",
            "Aliases: ls",
            "Accepts no parameters. Contacts return to stored order.",
            "",
            "Examples:",
            "  list",
            "  ls"));

    public static final String COMMAND_ALIAS = "ls";

    public static final String MESSAGE_USAGE =
            "The list command does not accept any parameters. Usage: list | ls";

    public static final String MESSAGE_SUCCESS = "Listed all persons.";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
