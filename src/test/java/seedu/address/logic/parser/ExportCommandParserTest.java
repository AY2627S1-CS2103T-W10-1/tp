package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ExportCommand;

/**
 * Tests CSV export parsing, output or command behavior.
 */
public class ExportCommandParserTest {
    private final ExportCommandParser parser = new ExportCommandParser();

    @Test
    public void parse_validArguments_success() {
        assertParseSuccess(parser, " --csv ", new ExportCommand("contacts.csv"));
        assertParseSuccess(parser, "--csv team.csv", new ExportCommand("team.csv"));
        assertParseSuccess(parser, "--csv\tteam.csv", new ExportCommand("team.csv"));
        assertParseSuccess(parser, "--csv \"my contacts.csv\"", new ExportCommand("my contacts.csv"));
        assertParseSuccess(parser, "--csv 'my contacts.csv'", new ExportCommand("my contacts.csv"));
        assertParseSuccess(parser, "--csv 联系人.csv", new ExportCommand("联系人.csv"));
    }

    @Test
    public void parse_invalidSyntax_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExportCommand.MESSAGE_USAGE);
        for (String args : new String[]{"", "team.csv", "--json", "--csvx", "--csv --csv", "--csv --json",
            "--csv a.csv b.csv", "--csv \"a.csv", "--csv '", "--csv \"a.csv\" extra"}) {
            assertParseFailure(parser, args, expected);
        }
    }

    @Test
    public void parse_invalidFilename_failure() {
        for (String fileName : new String[]{"\"\"", "''", ".csv", "contacts.txt", "contacts.CSV", "../team.csv",
            "folder/team.csv", "folder\\team.csv", "a:b.csv", "a?.csv", "a*.csv", "a|b.csv", "a<b.csv",
            "a>b.csv", "\"a\"b.csv\"", "CON.csv", "nul.more.csv", "COM1.csv", "LPT².csv", "\" a.csv\""}) {
            assertParseFailure(parser, "--csv " + fileName, ExportCommand.MESSAGE_INVALID_FILE_NAME);
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
