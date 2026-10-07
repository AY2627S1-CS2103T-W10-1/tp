package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.storage.CsvContactExporter;

/**
 * Parses export's required --csv flag and optional filename.
 */
public class ExportCommandParser implements Parser<ExportCommand> {
    @Override
    public ExportCommand parse(String args) throws ParseException {
        String trimmed = requireNonNull(args).trim();
        if (!trimmed.equals("--csv") && !trimmed.matches("(?s)--csv\\s+.*")) {
            throw invalidFormat();
        }
        String fileName = trimmed.substring("--csv".length()).trim();
        if (fileName.isEmpty()) {
            return new ExportCommand(ExportCommand.DEFAULT_FILE_NAME);
        }
        char first = fileName.charAt(0);
        if (first == '"' || first == '\'') {
            if (fileName.length() < 2 || fileName.charAt(fileName.length() - 1) != first) {
                throw invalidFormat();
            }
            fileName = fileName.substring(1, fileName.length() - 1);
        } else if (fileName.chars().anyMatch(Character::isWhitespace) || fileName.startsWith("--")) {
            throw invalidFormat();
        }
        if (!CsvContactExporter.isValidFileName(fileName)) {
            throw new ParseException(ExportCommand.MESSAGE_INVALID_FILE_NAME);
        }
        return new ExportCommand(fileName);
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExportCommand.MESSAGE_USAGE));
    }
}
