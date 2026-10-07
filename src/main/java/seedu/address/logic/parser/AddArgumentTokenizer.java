package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Tokenizes add's long options while keeping quoted values intact. */
final class AddArgumentTokenizer {
    private static final Map<String, Prefix> OPTIONS = Map.of(
            "--name", PREFIX_NAME, "--phone", PREFIX_PHONE, "--email", PREFIX_EMAIL,
            "--address", PREFIX_ADDRESS, "--department", PREFIX_DEPARTMENT, "--tag", PREFIX_TAG);

    private AddArgumentTokenizer() {
    }

    /** Rejects unknown options, duplicate single-value options and malformed quotation. */
    static ArgumentMultimap tokenize(String args) throws ParseException {
        List<Word> words = splitWords(args);
        ArgumentMultimap result = new ArgumentMultimap();
        int position = 0;
        while (position < words.size()) {
            Word option = words.get(position++);
            Prefix prefix = option.quoted() ? null : OPTIONS.get(option.value());
            if (prefix == null) {
                throw invalidFormat();
            }
            if (!prefix.equals(PREFIX_TAG) && result.getValue(prefix).isPresent()) {
                throw new ParseException(Messages.getErrorMessageForDuplicatePrefixes(new Prefix(option.value())));
            }
            List<String> values = new ArrayList<>();
            while (position < words.size() && !words.get(position).isOption()) {
                values.add(words.get(position++).value());
            }
            if (prefix.equals(PREFIX_TAG) && !values.isEmpty()) {
                values.forEach(value -> result.put(prefix, value));
            } else {
                result.put(prefix, String.join(" ", values));
            }
        }
        return result;
    }

    private static List<Word> splitWords(String args) throws ParseException {
        List<Word> words = new ArrayList<>();
        int position = 0;
        while (position < args.length()) {
            if (Character.isWhitespace(args.charAt(position))) {
                position++;
                continue;
            }
            char first = args.charAt(position);
            boolean quoted = first == '"' || first == '\'';
            int start = quoted ? ++position : position;
            if (quoted) {
                while (position < args.length() && args.charAt(position) != first) {
                    position++;
                }
                if (position == args.length()) {
                    throw invalidFormat();
                }
                words.add(new Word(args.substring(start, position++), true));
                if (position < args.length() && !Character.isWhitespace(args.charAt(position))) {
                    throw invalidFormat();
                }
            } else {
                while (position < args.length() && !Character.isWhitespace(args.charAt(position))) {
                    if (args.charAt(position) == '"' || args.charAt(position) == '\'') {
                        throw invalidFormat();
                    }
                    position++;
                }
                words.add(new Word(args.substring(start, position), false));
            }
        }
        return words;
    }

    private static ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    private record Word(String value, boolean quoted) {
        boolean isOption() {
            return !quoted && value.startsWith("--");
        }
    }
}
