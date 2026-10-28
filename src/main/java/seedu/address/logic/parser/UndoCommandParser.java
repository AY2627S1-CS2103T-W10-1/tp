package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new UndoCommand object
 */
public class UndoCommandParser implements Parser<UndoCommand> {

    /**
     * Parses blank arguments, or the flag that discards the most recent deletion instead of restoring it.
     *
     * @throws ParseException if the arguments are neither blank nor exactly {@code --discard-latest}.
     */
    public UndoCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            return new UndoCommand();
        }
        if (trimmedArgs.equals("--discard-latest")) {
            return new UndoCommand(true);
        }
        throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UndoCommand.MESSAGE_USAGE));
    }

}
