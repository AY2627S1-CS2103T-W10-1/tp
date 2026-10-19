package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses arguments for the clear command.
 */
public class ClearCommandParser implements Parser<ClearCommand> {

    /**
     * Parses blank arguments or an explicit confirmation flag.
     *
     * @throws ParseException if the arguments are neither blank nor exactly {@code --confirm}.
     */
    @Override
    public ClearCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            return new ClearCommand();
        }
        if (trimmedArgs.equals("--confirm")) {
            return new ClearCommand(true);
        }
        throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE));
    }
}
