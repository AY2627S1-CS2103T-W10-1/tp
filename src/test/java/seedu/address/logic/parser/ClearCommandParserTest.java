package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ClearCommand;

public class ClearCommandParserTest {

    private final ClearCommandParser parser = new ClearCommandParser();

    @Test
    public void parse_blankArgs_returnsUnconfirmedCommand() {
        assertParseSuccess(parser, "", new ClearCommand());
        assertParseSuccess(parser, " \t ", new ClearCommand());
    }

    @Test
    public void parse_confirmationFlag_returnsConfirmedCommand() {
        assertParseSuccess(parser, "--confirm", new ClearCommand(true));
        assertParseSuccess(parser, " \t--confirm  ", new ClearCommand(true));
    }

    @Test
    public void parse_otherArgs_throwsParseException() {
        for (String args : List.of("1", "--CONFIRM", "--confirm extra", "--confirm --confirm",
                "extra --confirm", "--confirm=true", "\"--confirm\"")) {
            assertParseFailure(parser, args,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE));
        }
    }
}
