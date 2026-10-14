package seedu.address.logic.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Tokenizes arguments string of the form: {@code preamble <prefix> value <prefix> value ...}<br>
 *     e.g. {@code some preamble text -t 11.00 --tag 12.00 -k -m July}  where prefixes are {@code -t -k -m}
 *     and {@code --tag} is an alias of {@code -t}.<br>
 * 1. The arguments string is split into words at whitespace. A prefix is only recognised as a whole word.<br>
 * 2. A word that starts with a single or double quote extends to the matching quote, and is never treated as
 *    a prefix, e.g. the value of {@code -a} in {@code -a "Blk 5 -t 3"} is {@code Blk 5 -t 3}.<br>
 * 3. The words of a value are joined with single spaces. An argument's value can be an empty string
 *    e.g. the value of {@code -k} in the above example.<br>
 * 4. An argument may be repeated and all its values will be accumulated e.g. the value of {@code -t}
 *    in the above example. For a prefix where {@link Prefix#isEachWordAValue()}, each word is a separate value.<br>
 * 5. An unquoted word starting with {@code --} that is not one of the given prefixes is rejected.<br>
 */
public class ArgumentTokenizer {

    public static final String MESSAGE_UNMATCHED_QUOTE = "Unmatched quote in: %1$s";
    public static final String MESSAGE_TEXT_AFTER_QUOTE = "A closing quote must be followed by a space: %1$s";
    public static final String MESSAGE_UNKNOWN_OPTION = "Unknown option: %1$s";

    private static final String LONG_OPTION_START = "--";

    /**
     * Tokenizes an arguments string and returns an {@code ArgumentMultimap} object that maps prefixes to their
     * respective argument values. Only the given prefixes will be recognized in the arguments string.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix> value <prefix> value ...}
     * @param prefixes   Prefixes to tokenize the arguments string with
     * @return           ArgumentMultimap object that maps prefixes to their arguments
     * @throws ParseException if a quote is unmatched or an unknown {@code --} option is present
     */
    public static ArgumentMultimap tokenize(String argsString, Prefix... prefixes) throws ParseException {
        ArgumentMultimap argMultimap = new ArgumentMultimap();
        Prefix currentPrefix = new Prefix(""); // the preamble
        List<String> currentValues = new ArrayList<>();

        for (Word word : splitWords(argsString)) {
            Optional<Prefix> matchedPrefix = word.isQuoted() ? Optional.empty() : findPrefix(word.text(), prefixes);
            if (matchedPrefix.isPresent()) {
                storeValues(argMultimap, currentPrefix, currentValues);
                currentPrefix = matchedPrefix.get();
                currentValues = new ArrayList<>();
            } else if (!word.isQuoted() && word.text().startsWith(LONG_OPTION_START)) {
                throw new ParseException(String.format(MESSAGE_UNKNOWN_OPTION, word.text()));
            } else {
                currentValues.add(word.text());
            }
        }
        storeValues(argMultimap, currentPrefix, currentValues);

        return argMultimap;
    }

    /**
     * Returns the prefix that has {@code text} as one of its forms, if any.
     */
    private static Optional<Prefix> findPrefix(String text, Prefix... prefixes) {
        return Arrays.stream(prefixes)
                .filter(prefix -> prefix.getAllForms().contains(text))
                .findFirst();
    }

    /**
     * Stores the words that followed {@code prefix} in {@code argMultimap}.
     */
    private static void storeValues(ArgumentMultimap argMultimap, Prefix prefix, List<String> values) {
        if (prefix.isEachWordAValue() && !values.isEmpty()) {
            values.forEach(value -> argMultimap.put(prefix, value));
        } else {
            argMultimap.put(prefix, String.join(" ", values));
        }
    }

    /**
     * Splits {@code argsString} into words at whitespace. A word starting with a quote extends to the matching
     * quote and must be followed by whitespace or the end of {@code argsString}. Quotes inside an unquoted
     * word are kept as ordinary characters, e.g. {@code O'Brien}.
     */
    private static List<Word> splitWords(String argsString) throws ParseException {
        List<Word> words = new ArrayList<>();
        int position = 0;
        while (position < argsString.length()) {
            char first = argsString.charAt(position);
            if (Character.isWhitespace(first)) {
                position++;
                continue;
            }

            if (first == '"' || first == '\'') {
                int closingQuote = argsString.indexOf(first, position + 1);
                if (closingQuote == -1) {
                    throw new ParseException(String.format(MESSAGE_UNMATCHED_QUOTE, argsString.substring(position)));
                }
                int end = closingQuote + 1;
                if (end < argsString.length() && !Character.isWhitespace(argsString.charAt(end))) {
                    throw new ParseException(String.format(MESSAGE_TEXT_AFTER_QUOTE, argsString.substring(position)));
                }
                words.add(new Word(argsString.substring(position + 1, closingQuote), true));
                position = end;
            } else {
                int end = position;
                while (end < argsString.length() && !Character.isWhitespace(argsString.charAt(end))) {
                    end++;
                }
                words.add(new Word(argsString.substring(position, end), false));
                position = end;
            }
        }
        return words;
    }

    /**
     * Represents a word in an arguments string, and whether it was written in quotes.
     */
    private record Word(String text, boolean isQuoted) {
    }

}
