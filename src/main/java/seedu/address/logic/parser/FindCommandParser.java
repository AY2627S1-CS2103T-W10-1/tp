package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.TagsMatchPredicate;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {
    public static final String MESSAGE_EMPTY_TAG = "Tag cannot be empty: "
            + "Please provide a valid tag after '--tag'.";
    public static final String MESSAGE_INVALID_TAG = "Invalid Tag: '%s' must be 1-30 characters using only "
            + "letters, numbers, hyphens (-), and underscores (_), with no spaces.";
    public static final String MESSAGE_UNKNOWN_OPTION = "Invalid find option: '%s'. "
            + "Use find --tag TAG [--tag TAG ...] or find KEYWORD [MORE_KEYWORDS].";
    private static final Pattern TAG_OPTION = Pattern.compile("(?<!\\S)--tag(?=\\s|$)");

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        String[] tagArguments = TAG_OPTION.split(trimmedArgs, -1);
        if (tagArguments.length > 1) {
            if (!tagArguments[0].isEmpty()) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }
            List<String> tags = new ArrayList<>();
            for (int i = 1; i < tagArguments.length; i++) {
                String tag = tagArguments[i].trim();
                if (tag.isEmpty()) {
                    throw new ParseException(MESSAGE_EMPTY_TAG);
                }
                if (!Tag.isValidTagName(tag)) {
                    throw new ParseException(String.format(MESSAGE_INVALID_TAG, tag));
                }
                tags.add(tag);
            }
            return new FindCommand(new TagsMatchPredicate(tags));
        }

        String[] nameKeywords = trimmedArgs.split("\\s+");
        for (String keyword : nameKeywords) {
            // Option-like input must not silently fall back to a name search.
            if (keyword.startsWith("-") || keyword.startsWith("\u2013") || keyword.startsWith("\u2014")) {
                throw new ParseException(String.format(MESSAGE_UNKNOWN_OPTION, keyword));
            }
        }

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(nameKeywords)));
    }

}
