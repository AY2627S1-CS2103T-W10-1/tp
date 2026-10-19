package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DepartCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.TagsMatchPredicate;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddressBookParserTest {

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseCommand_commandWordIgnoresCase_argumentsPreserveCase() throws Exception {
        for (String commandWord : List.of("find", "FIND", "FiNd")) {
            assertEquals(new FindCommand(new TagsMatchPredicate(List.of("Friends"))),
                    parser.parseCommand(commandWord + " --tag Friends"));
            assertThrows(ParseException.class,
                    String.format(FindCommandParser.MESSAGE_UNKNOWN_OPTION, "--TAG"), () ->
                            parser.parseCommand(commandWord + " --TAG Friends"));
        }
        assertTrue(parser.parseCommand("LiSt") instanceof ListCommand);
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), parser.parseCommand("DELETE 1"));
    }

    @Test
    public void parseCommand_add() throws Exception {
        Person person = new PersonBuilder().build();
        AddCommand command = (AddCommand) parser.parseCommand(PersonUtil.getAddCommand(person));
        assertEquals(new AddCommand(person), command);
    }

    @Test
    public void parseCommand_clear() throws Exception {
        assertEquals(new ClearCommand(), parser.parseCommand("clear"));
        assertEquals(new ClearCommand(true), parser.parseCommand("clear --confirm"));
        assertEquals(new ClearCommand(true), parser.parseCommand("CLEAR --confirm"));
        assertThrows(ParseException.class,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE), () ->
                        parser.parseCommand("clear 3"));
    }

    @Test
    public void parseCommand_delete() throws Exception {
        DeleteCommand command = (DeleteCommand) parser.parseCommand(
                DeleteCommand.COMMAND_WORD + " " + INDEX_FIRST_PERSON.getOneBased());
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), command);
    }

    @Test
    public void parseCommand_depart() throws Exception {
        assertTrue(parser.parseCommand("depart 1 --set Engineering") instanceof DepartCommand);
    }

    @Test
    public void parseCommand_edit() throws Exception {
        Person person = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(person).build();
        EditCommand command = (EditCommand) parser.parseCommand(EditCommand.COMMAND_WORD + " "
                + INDEX_FIRST_PERSON.getOneBased() + " " + PersonUtil.getEditPersonDescriptorDetails(descriptor));
        assertEquals(new EditCommand(INDEX_FIRST_PERSON, descriptor), command);
    }

    @Test
    public void parseCommand_exit() throws Exception {
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD) instanceof ExitCommand);
        assertTrue(parser.parseCommand(ExitCommand.COMMAND_WORD + " 3") instanceof ExitCommand);
    }

    @Test
    public void parseCommand_export() throws Exception {
        assertEquals(new ExportCommand("contacts.csv"), parser.parseCommand("export --csv"));
        assertEquals(new ExportCommand("contacts.csv"), parser.parseCommand("export -c"));
        assertEquals(new ExportCommand("team.csv"), parser.parseCommand("export --csv team.csv"));
    }

    @Test
    public void parseCommand_find() throws Exception {
        List<String> keywords = List.of("foo", "bar", "baz");
        FindCommand command = (FindCommand) parser.parseCommand(
                FindCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(keywords)), command);
    }

    @Test
    public void parseCommand_help() throws Exception {
        assertTrue(parser.parseCommand(HelpCommand.COMMAND_WORD) instanceof HelpCommand);
        assertTrue(parser.parseCommand("?") instanceof HelpCommand);
        assertEquals(new HelpCommand(DepartCommand.HELP_PAGE), parser.parseCommand("help depart"));
        assertThrows(ParseException.class, () -> parser.parseCommand("help 3"));
    }

    @Test
    public void parseCommand_list() throws Exception {
        for (String input : List.of("list", "ls", "  list  ", "  ls  ", "\tlist\t", "\tls\t")) {
            assertTrue(parser.parseCommand(input) instanceof ListCommand);
        }
    }

    @Test
    public void parseCommand_listWithParameters_throwsParseException() {
        String expectedMessage = "The list command does not accept any parameters. Usage: list | ls";
        for (String input : List.of("list 3", "ls 3", "list name", "ls --sort", " list 3 ", "ls\tname",
                "LIST name", "LS 3", "LiSt --sort", "lS\tname")) {
            assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(input));
        }
    }

    @Test
    public void parseCommand_listWithDifferentCase_success() throws Exception {
        for (String input : List.of("LIST", "List", "LiSt", "LS", "Ls", "lS")) {
            assertTrue(parser.parseCommand(input) instanceof ListCommand);
        }
    }

    @Test
    public void parseCommand_undo() throws Exception {
        assertEquals(new UndoCommand(), parser.parseCommand(UndoCommand.COMMAND_WORD));
    }

    @Test
    public void parseCommand_uppercaseCommandWord() throws Exception {
        assertTrue(parser.parseCommand("LIST") instanceof ListCommand);
        assertTrue(parser.parseCommand("HELP") instanceof HelpCommand);
        assertEquals(new DeleteCommand(INDEX_FIRST_PERSON), parser.parseCommand("DELETE 1"));
    }

    @Test
    public void parseCommand_mixedCaseCommandWord() throws Exception {
        assertTrue(parser.parseCommand("List") instanceof ListCommand);
        assertTrue(parser.parseCommand("eXiT") instanceof ExitCommand);
        assertEquals(new UndoCommand(), parser.parseCommand("Undo"));
    }

    @Test
    public void parseCommand_mixedCaseCommandWord_argumentsUnchanged() throws Exception {
        FindCommand command = (FindCommand) parser.parseCommand("FIND Alice BOB");
        assertEquals(new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "BOB"))), command);
    }

    @Test
    public void parseCommand_unrecognisedInput_throwsParseException() {
        assertThrows(ParseException.class, String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE), ()
            -> parser.parseCommand(""));
    }

    @Test
    public void parseCommand_unknownCommand_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknownCommand"));
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("LISTS"));
    }
}
