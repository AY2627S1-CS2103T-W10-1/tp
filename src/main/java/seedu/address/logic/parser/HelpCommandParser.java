package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.logic.help.HelpPage;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses an optional command name or alias for the help command.
 */
public class HelpCommandParser implements Parser<HelpCommand> {
    public static final String MESSAGE_UNKNOWN_HELP_COMMAND =
            "Unknown help command: %s. Use help or ? to list available commands.";

    /**
     * Parses zero arguments for the overview or one case-insensitive command name for its manual.
     *
     * @param args Arguments following {@code help} or {@code ?}.
     * @return A command displaying the requested page.
     * @throws ParseException If the name is unknown or more than one argument is supplied.
     * @throws NullPointerException If {@code args} is null.
     */
    @Override
    public HelpCommand parse(String args) throws ParseException {
        String name = requireNonNull(args).trim();
        if (name.isEmpty()) {
            return new HelpCommand();
        }
        if (name.split("\\s+").length != 1) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }
        HelpPage page = HelpCatalog.findPage(name).orElseThrow(() ->
                new ParseException(String.format(MESSAGE_UNKNOWN_HELP_COMMAND, name)));
        return new HelpCommand(page);
    }
}
