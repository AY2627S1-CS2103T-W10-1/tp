package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.SortField;

/**
 * Parses the complete Unix-style option and field tokens for a sort command.
 */
public class SortCommandParser implements Parser<SortCommand> {
    public static final String MESSAGE_MISSING_OPTION = "Missing sorting option. Usage: " + SortCommand.MESSAGE_USAGE;
    public static final String MESSAGE_INVALID_FIELD = "Invalid sorting field. Use department, dept, tag, or tags.";
    public static final String MESSAGE_UNSUPPORTED_ARGUMENT =
            "Unsupported argument. Usage: " + SortCommand.MESSAGE_USAGE;
    public static final String MESSAGE_DESCENDING =
            "Descending order is not supported. Contacts are sorted in ascending order.";

    /**
     * Parses one sorting option and its field, rejecting descending order and unsupported arguments.
     *
     * @throws ParseException If the option, field, or argument structure is invalid.
     */
    @Override
    public SortCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_OPTION);
        }
        String[] tokens = trimmedArgs.split("\\s+");
        if (Arrays.asList(tokens).contains("--descending")) {
            throw new ParseException(MESSAGE_DESCENDING);
        }
        if ((!tokens[0].equals("-b") && !tokens[0].equals("--by")) || tokens.length > 2
                || (tokens.length == 2 && (tokens[1].startsWith("-") || tokens[1].contains("\"")))) {
            throw new ParseException(MESSAGE_UNSUPPORTED_ARGUMENT);
        }
        if (tokens.length == 1) {
            throw new ParseException(MESSAGE_MISSING_OPTION);
        }

        SortField field = switch (tokens[1]) {
            case "department", "dept" -> SortField.DEPARTMENT;
            case "tags", "tag" -> SortField.TAGS;
            default -> throw new ParseException(MESSAGE_INVALID_FIELD);
        };
        return new SortCommand(field);
    }
}
