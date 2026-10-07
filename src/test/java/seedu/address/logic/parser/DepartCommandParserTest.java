package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DepartCommand;
import seedu.address.model.person.Department;

class DepartCommandParserTest {
    private final DepartCommandParser parser = new DepartCommandParser();

    @Test
    void parse_validArguments_returnsCommand() throws Exception {
        assertEquals(new DepartCommand(Index.fromOneBased(1), "1", new Department("Engineering")),
                parser.parse(" 1 --set Engineering"));
    }

    @Test
    void parse_invalidArguments_throwsParseException() {
        assertParseFailure(parser, " zero --set Engineering",
                DepartCommandParser.MESSAGE_INVALID_CONTACT_ID);
        assertParseFailure(parser, " 1 --set ", DepartCommandParser.MESSAGE_EMPTY_DEPARTMENT);
        assertParseFailure(parser, " 1 --set !", Department.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " 999999999999999999999 --set Engineering",
                DepartCommandParser.MESSAGE_INVALID_CONTACT_ID);
        assertParseFailure(parser, " 1 --set Engineering --set Sales",
                DepartCommandParser.MESSAGE_EMPTY_DEPARTMENT);
    }
}
