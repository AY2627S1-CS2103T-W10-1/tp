package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ViewCommand;

public class ViewCommandParserTest {

    private final ViewCommandParser parser = new ViewCommandParser();

    @Test
    public void parse_positiveContactIdWithWhitespace_success() {
        assertParseSuccess(parser, "1", new ViewCommand(Index.fromOneBased(1)));
        assertParseSuccess(parser, " \t2 \t", new ViewCommand(Index.fromOneBased(2)));
        assertParseSuccess(parser, "2147483647", new ViewCommand(Index.fromOneBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_invalidInputs_reportsUsage() {
        String expectedMessage = ViewCommandParser.MESSAGE_INVALID_CONTACT_ID + "\n" + ViewCommand.MESSAGE_USAGE;
        String[] invalidInputs = {"", " ", "0", "-1", "+1", "1.0", "abc", "1 2", "1 extra",
            "1\n2", "2147483648", "999999999999999999999999"};
        for (String input : invalidInputs) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parseCommand_view_routesToViewCommand() throws Exception {
        assertEquals(new ViewCommand(Index.fromOneBased(2)), new AddressBookParser().parseCommand("view 2"));
    }
}
