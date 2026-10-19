package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import seedu.address.logic.commands.DepartCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Department;

/** Parses exactly one department catalogue operation. */
public class DepartCommandParser implements Parser<DepartCommand> {
    public static final String MESSAGE_EMPTY_DEPARTMENT = "Department name cannot be empty.";

    @Override
    public DepartCommand parse(String args) throws ParseException {
        requireNonNull(args);
        List<ArgumentTokenizer.Word> words;
        try {
            words = ArgumentTokenizer.splitWords(args);
        } catch (ParseException e) {
            throw invalidFormat();
        }
        if (words.isEmpty() || words.getFirst().isQuoted()) {
            throw invalidFormat();
        }
        String operation = words.getFirst().text();
        int expectedSize = switch (operation) {
            case "--list" -> 1;
            case "--add" -> 2;
            case "--edit" -> 3;
            default -> 0;
        };
        if (expectedSize == 0 || words.size() != expectedSize
                || words.stream().skip(1).anyMatch(word -> !word.isQuoted() && word.text().startsWith("--"))) {
            throw invalidFormat();
        }
        return switch (operation) {
            case "--list" -> DepartCommand.list();
            case "--add" -> DepartCommand.add(parseDepartment(words.get(1).text()));
            default -> DepartCommand.rename(parseDepartment(words.get(1).text()), parseDepartment(words.get(2).text()));
        };
    }

    private Department parseDepartment(String value) throws ParseException {
        if (value.isBlank()) {
            throw new ParseException(MESSAGE_EMPTY_DEPARTMENT);
        }
        if (!Department.isValidDepartment(value)) {
            throw new ParseException(Department.MESSAGE_CONSTRAINTS);
        }
        return new Department(value);
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DepartCommand.MESSAGE_USAGE));
    }
}
