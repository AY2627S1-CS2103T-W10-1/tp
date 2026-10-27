package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.TagsMatchPredicate;
import seedu.address.model.tag.Tag;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_unknownOption_throwsParseException() {
        for (String option : List.of("--tags", "--Tag", "--TAG", "-tag", "-t", "---tag", "--tag=friends",
                "--tagfriends", "--", "\u2013tag", "\u2014tag")) {
            String expectedMessage = String.format(FindCommandParser.MESSAGE_UNKNOWN_OPTION, option);
            assertParseFailure(parser, option + " friends", expectedMessage);
            assertParseFailure(parser, "Alice " + option + " friends", expectedMessage);
        }
    }

    @Test
    public void parse_unknownOptionAfterTag_throwsParseException() {
        for (String option : List.of("--tags", "--TAG", "--tag=family")) {
            assertParseFailure(parser, "--tag friends " + option,
                    String.format(FindCommandParser.MESSAGE_UNKNOWN_OPTION, option));
        }
    }

    @Test
    public void parse_hyphensInNamesAndTags_success() {
        assertParseSuccess(parser, "Anne-Marie",
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Anne-Marie"))));
        assertParseSuccess(parser, "--tag -friends_1",
                new FindCommand(new TagsMatchPredicate(List.of("-friends_1"))));
    }

    @Test
    public void parse_tags_returnsFindCommand() {
        assertParseSuccess(parser, "--tag client", new FindCommand(new TagsMatchPredicate(List.of("client"))));
        assertParseSuccess(parser, " \t--tag Client-1\t--tag important_2  ",
                new FindCommand(new TagsMatchPredicate(List.of("Client-1", "important_2"))));
        assertParseSuccess(parser, "--tag client important_2",
                new FindCommand(new TagsMatchPredicate(List.of("client", "important_2"))));
        String longestTag = "a".repeat(30);
        assertParseSuccess(parser, "--tag " + longestTag,
                new FindCommand(new TagsMatchPredicate(List.of(longestTag))));
        String legacyLongTag = "a".repeat(31);
        assertParseSuccess(parser, "--tag " + legacyLongTag,
                new FindCommand(new TagsMatchPredicate(List.of(legacyLongTag))));
    }

    @Test
    public void parse_tagsUsesEditNormalization_returnsFindCommand() {
        assertParseSuccess(parser, "--tag #client /important",
                new FindCommand(new TagsMatchPredicate(List.of("client", "important"))));
        assertParseSuccess(parser, "--tag \"--tag\"",
                new FindCommand(new TagsMatchPredicate(List.of("--tag"))));
        assertParseSuccess(parser, "--tag client client",
                new FindCommand(new TagsMatchPredicate(List.of("client"))));
    }

    @Test
    public void parse_missingTag_throwsParseException() {
        for (String args : List.of("--tag", "--tag   ", "--tag --tag client", "--tag client --tag")) {
            assertParseFailure(parser, args, FindCommandParser.MESSAGE_EMPTY_TAG);
        }
    }

    @Test
    public void parse_invalidTag_throwsParseException() {
        assertParseFailure(parser, "--tag bad!", Tag.MESSAGE_CHARACTER_CONSTRAINTS);
        assertParseFailure(parser, "--tag client --tag bad!", Tag.MESSAGE_CHARACTER_CONSTRAINTS);
        assertParseFailure(parser, "Alice --tag client",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_quotedNames_returnsSameKeywords() {
        FindCommand expected = new FindCommand(new NameContainsKeywordsPredicate(List.of("Alex", "Yeoh")));
        assertParseSuccess(parser, "\"Alex Yeoh\"", expected);
        assertParseSuccess(parser, "'Alex Yeoh'", expected);
        assertParseSuccess(parser, "\"Alex\" Yeoh", expected);
    }

    @Test
    public void parse_emptyQuotedName_throwsParseException() {
        assertParseFailure(parser, "\"\"",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_unmatchedNameQuote_throwsParseException() {
        assertParseFailure(parser, "\"Alex",
                String.format(ArgumentTokenizer.MESSAGE_UNMATCHED_QUOTE, "\"Alex"));
    }

}
