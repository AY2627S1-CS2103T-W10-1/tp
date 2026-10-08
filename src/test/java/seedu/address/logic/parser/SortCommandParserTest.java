package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand;
import seedu.address.model.SortField;

public class SortCommandParserTest {
    private final SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_allOptionsAndAliases_success() throws Exception {
        for (String option : new String[] {"-b", "--by"}) {
            for (String alias : new String[] {"department", "dept", "tags", "tag"}) {
                SortField field = alias.startsWith("dep") ? SortField.DEPARTMENT : SortField.TAGS;
                assertEquals(new SortCommand(field), parser.parse("  " + option + "\t" + alias + "  "));
            }
        }
    }

    @Test
    public void parse_missingOptionOrValue_failure() {
        for (String args : new String[] {"", "  ", "-b", "--by"}) {
            assertParseFailure(parser, args, SortCommandParser.MESSAGE_MISSING_OPTION);
        }
    }

    @Test
    public void parse_invalidField_failure() {
        for (String args : new String[] {"-b name", "--by Department", "-b descending"}) {
            assertParseFailure(parser, args, SortCommandParser.MESSAGE_INVALID_FIELD);
        }
    }

    @Test
    public void parse_unsupportedSyntax_failure() {
        for (String args : new String[] {"department", "--unknown tags", "--by=tags", "-btags",
            "--bytags", "-b tags extra", "-b tags --by tags", "-b --unknown", "--by \"tags\"",
            "--by 'tags'", "\u2013by tags"}) {
            assertParseFailure(parser, args, SortCommandParser.MESSAGE_UNSUPPORTED_ARGUMENT);
        }
    }

    @Test
    public void parse_descendingToken_takesPrecedence() {
        for (String args : new String[] {"-b department --descending", "--descending", "--bad --descending"}) {
            assertParseFailure(parser, args, SortCommandParser.MESSAGE_DESCENDING);
        }
    }

    @Test
    public void parseCommand_sort_dispatchesToSortParser() throws Exception {
        assertEquals(new SortCommand(SortField.TAGS), new AddressBookParser().parseCommand("sort --by tags"));
    }
}
