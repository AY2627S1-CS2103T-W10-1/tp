package seedu.address.logic.commands;

import seedu.address.model.Model;

/**
 * Opens the help window containing the available commands.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Shows program usage instructions.\n"
            + "Example: " + COMMAND_WORD;

    public static final String SHOWING_HELP_MESSAGE = "Opened help window.";

    public static final String HELP_TEXT = String.join("\n",
            AddCommand.COMMAND_WORD + " — Adds a contact to the address book.",
            EditCommand.COMMAND_WORD + " — Changes a contact's details.",
            DeleteCommand.COMMAND_WORD + " — Deletes a contact.",
            UndoCommand.COMMAND_WORD + " — Restores the most recently deleted contact.",
            FindCommand.COMMAND_WORD + " — Finds contacts by name.",
            ListCommand.COMMAND_WORD + " — Shows all contacts.",
            ViewCommand.COMMAND_WORD + " — Shows a contact's full details.",
            DepartCommand.COMMAND_WORD + " — Sets a contact's department.",
            ClearCommand.COMMAND_WORD + " — Deletes all contacts.",
            COMMAND_WORD + " — Shows this command list.",
            ExitCommand.COMMAND_WORD + " — Closes the application.");

    @Override
    public CommandResult execute(Model model) {
        return new CommandResult(SHOWING_HELP_MESSAGE, true, false);
    }
}
