package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the displayed index for a ViewCommand.
 */
public class ViewCommandParser implements Parser<ViewCommand> {

    public static final String MESSAGE_INVALID_INDEX = "Invalid index: must be a positive integer.";

    @Override
    public ViewCommand parse(String args) throws ParseException {
        requireNonNull(args);
        try {
            Index index = ParserUtil.parseIndex(args);
            return new ViewCommand(index);
        } catch (ParseException e) {
            throw new ParseException(MESSAGE_INVALID_INDEX + "\n" + ViewCommand.MESSAGE_USAGE, e);
        }
    }
}
