package seedu.address.logic.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tokenizes arguments string of the form: {@code preamble <prefix> value <prefix> value ...}<br>
 *     e.g. {@code some preamble text -t 11.00 --tag 12.00 -k -m July}  where prefixes are {@code -t -k -m}
 *     and {@code --tag} is an alias of {@code -t}.<br>
 * 1. An argument's value can be an empty string e.g. the value of {@code -k} in the above example.<br>
 * 2. Leading and trailing whitespaces of an argument value will be discarded.<br>
 * 3. An argument may be repeated and all its values will be accumulated e.g. the value of {@code -t}
 *    in the above example.<br>
 * 4. A prefix is only recognised as a whole word, i.e. it must be preceded by a whitespace and followed by
 *    a whitespace or the end of the arguments string.<br>
 */
public class ArgumentTokenizer {

    /**
     * Tokenizes an arguments string and returns an {@code ArgumentMultimap} object that maps prefixes to their
     * respective argument values. Only the given prefixes will be recognized in the arguments string.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix> value <prefix> value ...}
     * @param prefixes   Prefixes to tokenize the arguments string with
     * @return           ArgumentMultimap object that maps prefixes to their arguments
     */
    public static ArgumentMultimap tokenize(String argsString, Prefix... prefixes) {
        List<PrefixPosition> positions = findAllPrefixPositions(argsString, prefixes);
        return extractArguments(argsString, positions);
    }

    /**
     * Finds all zero-based prefix positions in the given arguments string.
     *
     * @param argsString Arguments string of the form: {@code preamble <prefix> value <prefix> value ...}
     * @param prefixes   Prefixes to find in the arguments string
     * @return           List of zero-based prefix positions in the given arguments string
     */
    private static List<PrefixPosition> findAllPrefixPositions(String argsString, Prefix... prefixes) {
        return Arrays.stream(prefixes)
                .flatMap(prefix -> findPrefixPositions(argsString, prefix).stream())
                .collect(Collectors.toList());
    }

    /**
     * Finds the positions of every form (primary and aliases) of {@code prefix}. Each position is recorded
     * against {@code prefix} itself, so all forms map to the same key.
     *
     * @see #findAllPrefixPositions(String, Prefix...)
     */
    private static List<PrefixPosition> findPrefixPositions(String argsString, Prefix prefix) {
        List<PrefixPosition> positions = new ArrayList<>();

        for (String form : prefix.getAllForms()) {
            int prefixPosition = findPrefixPosition(argsString, form, 0);
            while (prefixPosition != -1) {
                positions.add(new PrefixPosition(prefix, prefixPosition, form.length()));
                prefixPosition = findPrefixPosition(argsString, form, prefixPosition);
            }
        }

        return positions;
    }

    /**
     * Returns the index of the first occurrence of {@code prefix} in
     * {@code argsString} starting from index {@code fromIndex}. An occurrence
     * is valid if there is a whitespace before {@code prefix} and a whitespace
     * or the end of {@code argsString} after it. Returns -1 if no such
     * occurrence can be found.
     *
     * E.g if {@code argsString} = "-e hi-p 900", {@code prefix} = "-p" and
     * {@code fromIndex} = 0, this method returns -1 as there are no valid
     * occurrences of "-p" with whitespace before it. Likewise, "-e hi -p900"
     * has no valid occurrence as "-p" is not followed by a whitespace. However,
     * if {@code argsString} = "-e hi -p 900", {@code prefix} = "-p" and
     * {@code fromIndex} = 0, this method returns 6.
     */
    private static int findPrefixPosition(String argsString, String prefix, int fromIndex) {
        int prefixIndex = argsString.indexOf(" " + prefix, fromIndex);
        while (prefixIndex != -1) {
            int prefixStart = prefixIndex + 1; // +1 as offset for whitespace
            int prefixEnd = prefixStart + prefix.length();
            if (prefixEnd == argsString.length() || Character.isWhitespace(argsString.charAt(prefixEnd))) {
                return prefixStart;
            }
            prefixIndex = argsString.indexOf(" " + prefix, prefixStart);
        }
        return -1;
    }

    /**
     * Extracts prefixes and their argument values, and returns an {@code ArgumentMultimap} object that maps the
     * extracted prefixes to their respective arguments. Prefixes are extracted based on their zero-based positions in
     * {@code argsString}.
     *
     * @param argsString      Arguments string of the form: {@code preamble <prefix> value <prefix> value ...}
     * @param prefixPositions Zero-based positions of all prefixes in {@code argsString}
     * @return                ArgumentMultimap object that maps prefixes to their arguments
     */
    private static ArgumentMultimap extractArguments(String argsString, List<PrefixPosition> prefixPositions) {

        // Sort by start position
        prefixPositions.sort((prefix1, prefix2) -> prefix1.getStartPosition() - prefix2.getStartPosition());

        // Insert a PrefixPosition to represent the preamble
        PrefixPosition preambleMarker = new PrefixPosition(new Prefix(""), 0, 0);
        prefixPositions.addFirst(preambleMarker);

        // Add a dummy PrefixPosition to represent the end of the string
        PrefixPosition endPositionMarker = new PrefixPosition(new Prefix(""), argsString.length(), 0);
        prefixPositions.add(endPositionMarker);

        // Map prefixes to their argument values (if any)
        ArgumentMultimap argMultimap = new ArgumentMultimap();
        for (int i = 0; i < prefixPositions.size() - 1; i++) {
            // Extract and store prefixes and their arguments
            Prefix argPrefix = prefixPositions.get(i).getPrefix();
            String argValue = extractArgumentValue(argsString, prefixPositions.get(i), prefixPositions.get(i + 1));
            argMultimap.put(argPrefix, argValue);
        }

        return argMultimap;
    }

    /**
     * Returns the trimmed value of the argument in the arguments string specified by {@code currentPrefixPosition}.
     * The end position of the value is determined by {@code nextPrefixPosition}.
     */
    private static String extractArgumentValue(String argsString,
                                        PrefixPosition currentPrefixPosition,
                                        PrefixPosition nextPrefixPosition) {
        int valueStartPos = currentPrefixPosition.getStartPosition() + currentPrefixPosition.getMatchedLength();
        String value = argsString.substring(valueStartPos, nextPrefixPosition.getStartPosition());

        return value.trim();
    }

    /**
     * Represents a prefix's position in an arguments string, along with the length of the
     * form of the prefix (primary or alias) that was matched at that position.
     */
    private static class PrefixPosition {
        private int startPosition;
        private final int matchedLength;
        private final Prefix prefix;

        PrefixPosition(Prefix prefix, int startPosition, int matchedLength) {
            this.prefix = prefix;
            this.startPosition = startPosition;
            this.matchedLength = matchedLength;
        }

        int getStartPosition() {
            return startPosition;
        }

        int getMatchedLength() {
            return matchedLength;
        }

        Prefix getPrefix() {
            return prefix;
        }
    }

}
