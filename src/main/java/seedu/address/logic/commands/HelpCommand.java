package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.logic.help.HelpPage;
import seedu.address.model.Model;

/**
 * Opens the help window containing the available commands.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    /** Detailed usage, supported options and examples for this command. */
    public static final HelpPage HELP_PAGE = new HelpPage(COMMAND_WORD, String.join("\n",
            "Shows the command overview or detailed help for one command.",
            "Usage: help [COMMAND]",
            "       ? [COMMAND]",
            "",
            "Aliases: ?",
            "Command names are case-insensitive; ls and ? also work as help topics.",
            "Unknown commands and more than one argument are rejected.",
            "The Help menu and F1 show the overview. Press unmodified q to close the window.",
            "",
            "Examples:",
            "  help",
            "  help depart",
            "  ? sort",
            "  help ls"));

    public static final String COMMAND_ALIAS = "?";
    public static final String MESSAGE_USAGE = "help [COMMAND] or ? [COMMAND]\n"
            + "Shows available commands, or usage, options and examples for one command.\n"
            + "Example: help depart";
    public static final String SHOWING_HELP_MESSAGE = "Opened help window.";

    private final HelpPage page;

    /**
     * Creates a command that opens the command overview.
     */
    public HelpCommand() {
        this(HelpCatalog.getOverview());
    }

    /**
     * Creates a command that opens the specified help page.
     *
     * @param page Overview or command manual to display.
     * @throws NullPointerException If {@code page} is null.
     */
    public HelpCommand(HelpPage page) {
        this.page = requireNonNull(page);
    }

    @Override
    public CommandResult execute(Model model) {
        return CommandResult.forHelp(SHOWING_HELP_MESSAGE, page);
    }

    /**
     * Help only displays instructions and never requires an address book save.
     */
    @Override
    public boolean requiresStorageSave() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof HelpCommand otherCommand && page.equals(otherCommand.page));
    }

    @Override
    public int hashCode() {
        return page.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("page", page).toString();
    }
}
