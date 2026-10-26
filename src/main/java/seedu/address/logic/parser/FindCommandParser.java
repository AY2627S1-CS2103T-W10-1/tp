package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.TagsMatchPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {
    public static final String MESSAGE_EMPTY_TAG = "Tag cannot be empty: "
            + "Please provide a valid tag after '--tag'.";
    public static final String MESSAGE_UNKNOWN_OPTION = "Invalid find option: '%s'. "
            + "Use find --tag TAG [TAG ...] [--tag TAG ...] or find KEYWORD [MORE_KEYWORDS].";
    private static final Prefix TAG_OPTION = new Prefix("--tag").withEachWordAsValue();

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

        if (ArgumentTokenizer.splitWords(trimmedArgs).stream()
                .anyMatch(word -> !word.isQuoted() && word.text().equals(TAG_OPTION.getPrefix()))) {
            ArgumentMultimap tagArguments;
            try {
                tagArguments = ArgumentTokenizer.tokenize(trimmedArgs, TAG_OPTION);
            } catch (ParseException pe) {
                throw toFindParseException(pe);
            }
            if (!tagArguments.getPreamble().isEmpty()) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }
            List<String> rawTags = tagArguments.getAllValues(TAG_OPTION);
            if (rawTags.stream().anyMatch(String::isEmpty)) {
                throw new ParseException(MESSAGE_EMPTY_TAG);
            }
            List<String> tags = new ArrayList<>();
            for (String rawTag : rawTags) {
                String normalizedTag = ParserUtil.parseTagForSearch(rawTag).tagName;
                if (!tags.contains(normalizedTag)) {
                    tags.add(normalizedTag);
                }
            }
            return new FindCommand(new TagsMatchPredicate(tags));
        }

        String normalizedNames = ArgumentTokenizer.splitWords(trimmedArgs).stream()
                .map(ArgumentTokenizer.Word::text)
                .collect(java.util.stream.Collectors.joining(" ")).trim();
        if (normalizedNames.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        String[] nameKeywords = normalizedNames.split("\\s+");
        for (String keyword : nameKeywords) {
            // Option-like input must not silently fall back to a name search.
            if (keyword.startsWith("-") || keyword.startsWith("\u2013") || keyword.startsWith("\u2014")) {
                throw new ParseException(String.format(MESSAGE_UNKNOWN_OPTION, keyword));
            }
        }

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(nameKeywords)));
    }

    private static ParseException toFindParseException(ParseException exception) {
        // Quote structure was already validated by splitWords, so tokenize can only reject an unknown option here.
        String option = exception.getMessage().substring("Unknown option: ".length());
        return new ParseException(String.format(MESSAGE_UNKNOWN_OPTION, option));
    }

}
