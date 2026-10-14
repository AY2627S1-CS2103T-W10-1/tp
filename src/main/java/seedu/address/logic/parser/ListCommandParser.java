package seedu.address.logic.parser;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for listing all contacts.
 */
public class ListCommandParser implements Parser<ListCommand> {

    /**
     * Parses blank arguments and returns a command that lists all contacts.
     *
     * @param args Arguments supplied after the command word.
     * @return A command that lists all contacts.
     * @throws ParseException if any nonblank arguments are supplied.
     */
    @Override
    public ListCommand parse(String args) throws ParseException {
        if (!args.isBlank()) {
            throw new ParseException(ListCommand.MESSAGE_USAGE);
        }
        return new ListCommand();
    }
}
