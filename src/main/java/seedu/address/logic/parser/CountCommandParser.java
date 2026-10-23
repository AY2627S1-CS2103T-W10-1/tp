package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.CountCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the optional scope of a count command.
 */
public class CountCommandParser implements Parser<CountCommand> {
    @Override
    public CountCommand parse(String args) throws ParseException {
        String scope = requireNonNull(args).trim();
        if (scope.isEmpty()) {
            return new CountCommand(false);
        }
        if (scope.equals("--all")) {
            return new CountCommand(true);
        }
        throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, CountCommand.MESSAGE_USAGE));
    }
}
