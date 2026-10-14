package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ListCommandParserTest {

    private final ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_blankArgs_returnsListCommand() throws Exception {
        for (String args : List.of("", "   ", "\t", " \t ")) {
            assertNotNull(parser.parse(args));
        }
    }

    @Test
    public void parse_nonBlankArgs_throwsParseException() {
        String expectedMessage = "The list command does not accept any parameters. Usage: list | ls";
        for (String args : List.of("3", " name", " --sort", "\tname", " 3 ")) {
            assertParseFailure(parser, args, expectedMessage);
        }
    }
}
