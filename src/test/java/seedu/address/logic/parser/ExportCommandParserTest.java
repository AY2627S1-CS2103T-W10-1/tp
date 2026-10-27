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
        assertParseSuccess(parser, "--csv contact's.csv", new ExportCommand("contact's.csv"));
        assertParseSuccess(parser, "--csv \"contact's.csv\"", new ExportCommand("contact's.csv"));
        assertParseSuccess(parser, "--csv 联系人.csv", new ExportCommand("联系人.csv"));
        assertParseSuccess(parser, "-c", new ExportCommand("contacts.csv"));
        assertParseSuccess(parser, "-c team.csv", new ExportCommand("team.csv"));
        assertParseSuccess(parser, "-c \"my contacts.csv\"", new ExportCommand("my contacts.csv"));
        assertParseSuccess(parser, "-c 'my contacts.csv'  ", new ExportCommand("my contacts.csv"));
    }

    @Test
    public void parse_invalidSyntax_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExportCommand.MESSAGE_USAGE);
        for (String args : new String[]{"", "team.csv", "--json", "--csvx", "-csv", "-cx", "-c --csv",
            "--csv --csv", "--csv --json",
            "--csv a.csv b.csv", "--csv \"a.csv", "--csv '", "--csv \"a.csv\" extra", "--csv \"a\"b.csv\""}) {
            assertParseFailure(parser, args, expected);
        }
    }

    @Test
    public void parse_quotedFilenameWithTrailingArguments_failure() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExportCommand.MESSAGE_USAGE);
        for (String option : new String[]{"--csv", "-c"}) {
            for (String quote : new String[]{"'", "\""}) {
                String arguments = option + " " + quote + "first.csv" + quote;
                for (String suffix : new String[]{" " + quote + "second.csv" + quote,
                    " --json " + quote + "second.csv" + quote, " second.csv", " --csv", " -c",
                    "\t" + quote + "second.csv" + quote, "\n" + quote + "second.csv" + quote,
                    quote + "second.csv" + quote}) {
                    assertParseFailure(parser, arguments + suffix, expected);
                }
            }
        }
    }

    @Test
    public void parse_missingFilenameStem_explainsRequiredName() {
        assertFilenameFailure("Invalid file name: provide a name before the lowercase .csv extension.",
                "\"\"", "''", ".csv");
    }

    @Test
    public void parse_wrongExtension_explainsLowercaseCsv() {
        assertFilenameFailure("Invalid file name: the extension must be lowercase .csv.",
                "contacts.txt", "Team.CSV", "a", "abcd");
    }

    @Test
    public void parse_path_explainsFilenameOnly() {
        assertFilenameFailure("Invalid file name: folder paths are not allowed. Provide a filename only.",
                "../team.csv", "folder/team.csv", "folder\\team.csv", "\"../my contacts.csv\"");
    }

    @Test
    public void parse_invalidCharacters_explainsForbiddenCharacters() {
        assertFilenameFailure("Invalid file name: remove reserved characters (< > : \" | ? *) and control characters.",
                "a:b.csv", "a?.csv", "a*.csv", "a|b.csv", "a<b.csv", "a>b.csv", "a\u0000.csv",
                "\"a\t.csv\"", "\"a\n.csv\"", "'a\"b.csv'");
    }

    @Test
    public void parse_reservedNames_identifiesReservedStem() {
        for (String stem : new String[]{"CON", "con", "nul", "COM1", "LPT²", "LPT9", "COM³"}) {
            assertFilenameFailure("Invalid file name: '" + stem + "' is a reserved device name. Choose another name.",
                    stem + ".csv", stem + ".more.csv", "\"" + stem + " .csv\"");
        }
    }

    @Test
    public void parse_surroundingWhitespace_explainsTrimming() {
        assertFilenameFailure("Invalid file name: remove leading or trailing whitespace.",
                "\" a.csv\"", "'a.csv '", "\"\u2003a.csv\"", "\"a.csv\u2003\"");
    }

    private void assertFilenameFailure(String message, String... fileNames) {
        for (String option : new String[]{"--csv", "-c"}) {
            for (String fileName : fileNames) {
                assertParseFailure(parser, option + " " + fileName, message);
            }
        }
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
