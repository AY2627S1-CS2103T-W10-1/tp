package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DepartCommand;
import seedu.address.model.person.Department;

class DepartCommandParserTest {
    private final DepartCommandParser parser = new DepartCommandParser();

    @Test
    void parse_validOperations_success() throws Exception {
        assertEquals(DepartCommand.list(), parser.parse(" --list "));
        assertEquals(DepartCommand.add(new Department("Engineering")), parser.parse(" --add Engineering"));
        assertEquals(DepartCommand.add(new Department("R&D Engineering")),
                parser.parse(" --add \"R&D Engineering\""));
        assertEquals(DepartCommand.rename(new Department("Engineering"), new Department("R&D Engineering")),
                parser.parse(" --edit Engineering \"R&D Engineering\""));
        assertEquals(DepartCommand.rename(new Department("R&D Engineering"), new Department("Sales")),
                parser.parse(" --edit 'R&D Engineering' Sales"));
    }

    @Test
    void parse_invalidStructure_rejected() {
        String expected = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DepartCommand.MESSAGE_USAGE);
        for (String input : List.of("", "1 --set Engineering", "--list extra", "--list --list",
                "--list --add Sales", "--add", "--add Sales extra", "--edit Sales", "--edit Sales HR extra",
                "--edit Sales --list", "--unknown", "--ADD Sales", "'--list'", "--add \"Sales",
                "--add \"Sales\"extra", "--add Sales --add HR")) {
            assertParseFailure(parser, input, expected);
        }
    }

    @Test
    void parse_invalidNames_rejected() {
        for (String name : List.of("!", "A", "A".repeat(51))) {
            assertParseFailure(parser, "--add " + name, Department.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, "--edit Sales " + name, Department.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, "--add \"\"", DepartCommandParser.MESSAGE_EMPTY_DEPARTMENT);
        assertParseFailure(parser, "--edit Sales \"  \"", DepartCommandParser.MESSAGE_EMPTY_DEPARTMENT);
    }
}
