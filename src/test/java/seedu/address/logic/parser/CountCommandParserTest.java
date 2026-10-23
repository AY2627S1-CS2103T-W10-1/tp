package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CountCommand;
import seedu.address.logic.commands.HelpCommand;

public class CountCommandParserTest {
    private final CountCommandParser parser = new CountCommandParser();

    @Test
    public void parse_supportedScopes_acceptsSurroundingWhitespace() {
        for (String args : List.of("", "  ", "\t")) {
            assertParseSuccess(parser, args, new CountCommand(false));
        }
        for (String args : List.of("--all", " --all ", "\t--all\t")) {
            assertParseSuccess(parser, args, new CountCommand(true));
        }
    }

    @Test
    public void parse_unknownOrRepeatedArguments_rejectsWholeInput() {
        for (String args : List.of("1", "all", "--ALL", "--all extra", "--all --all", "-a", "--all=true")) {
            assertParseFailure(parser, args, String.format(MESSAGE_INVALID_COMMAND_FORMAT, CountCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void applicationParser_routesCountAndItsHelpPage() throws Exception {
        AddressBookParser applicationParser = new AddressBookParser();
        assertEquals(new CountCommand(false), applicationParser.parseCommand("CoUnT"));
        assertEquals(new CountCommand(true), applicationParser.parseCommand("COUNT --all"));
        assertEquals(new HelpCommand(CountCommand.HELP_PAGE), applicationParser.parseCommand("help count"));
    }
}
