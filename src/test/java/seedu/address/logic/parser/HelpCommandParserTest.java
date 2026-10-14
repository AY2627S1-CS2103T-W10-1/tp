package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.logic.parser.exceptions.ParseException;

public class HelpCommandParserTest {
    private final HelpCommandParser parser = new HelpCommandParser();

    @Test
    public void parse_emptyArguments_returnsOverview() throws Exception {
        for (String args : List.of("", " ", "\t  ")) {
            assertEquals(new HelpCommand(), parser.parse(args));
        }
    }

    @Test
    public void parse_knownNamesAndAliases_returnsManual() throws Exception {
        AddressBookParser dispatcher = new AddressBookParser();
        for (var page : HelpCatalog.getPages()) {
            String name = page.title().toUpperCase(Locale.ROOT);
            HelpCommand expected = new HelpCommand(page);
            assertEquals(expected, parser.parse("  " + name + "\t "));
            assertEquals(expected, dispatcher.parseCommand("HeLp " + name));
            assertEquals(expected, dispatcher.parseCommand("? " + name));
        }
        assertEquals(parser.parse("list"), parser.parse("LS"));
        assertEquals(parser.parse("help"), parser.parse("?"));
    }

    @Test
    public void parse_unknownName_reportsUnknownTopic() {
        for (String args : List.of("3", "missing", "--tag")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args));
            assertEquals(String.format(HelpCommandParser.MESSAGE_UNKNOWN_HELP_COMMAND, args), error.getMessage());
        }
    }

    @Test
    public void parse_multipleArguments_reportsUsage() {
        for (String args : List.of("add edit", "depart --set", "ls\tlist")) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args));
            assertEquals(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), error.getMessage());
        }
    }
    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

}
