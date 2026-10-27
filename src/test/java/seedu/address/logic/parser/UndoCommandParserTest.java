package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UndoCommand;

public class UndoCommandParserTest {

    private UndoCommandParser parser = new UndoCommandParser();

    @Test
    public void parse_emptyArgs_returnsUndoCommand() {
        assertParseSuccess(parser, "", new UndoCommand());
        assertParseSuccess(parser, "   ", new UndoCommand());
    }

    @Test
    public void parse_discardLatest_returnsDiscardingUndoCommand() {
        assertParseSuccess(parser, "--discard-latest", new UndoCommand(true));
        assertParseSuccess(parser, "  --discard-latest  ", new UndoCommand(true));
    }

    @Test
    public void parse_nonEmptyArgs_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, UndoCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "2", expectedMessage);
        assertParseFailure(parser, "--discard", expectedMessage);
        assertParseFailure(parser, "-d", expectedMessage);
        assertParseFailure(parser, "--DISCARD-LATEST", expectedMessage);
        assertParseFailure(parser, "--discard-latest 2", expectedMessage);
    }
}
