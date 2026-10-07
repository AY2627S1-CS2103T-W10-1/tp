package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses CONTACT_ID, the contact's one-based position in the displayed list, for a ViewCommand.
 */
public class ViewCommandParser implements Parser<ViewCommand> {

    public static final String MESSAGE_INVALID_CONTACT_ID = "Invalid Contact ID: Must be a positive integer.";

    @Override
    public ViewCommand parse(String args) throws ParseException {
        requireNonNull(args);
        try {
            Index index = ParserUtil.parseIndex(args);
            return new ViewCommand(index);
        } catch (ParseException e) {
            throw new ParseException(MESSAGE_INVALID_CONTACT_ID + "\n" + ViewCommand.MESSAGE_USAGE, e);
        }
    }
}
