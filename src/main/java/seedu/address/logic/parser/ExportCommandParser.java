package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Optional;

import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.storage.CsvContactExporter;

/**
 * Parses export's required -c/--csv flag and optional filename.
 */
public class ExportCommandParser implements Parser<ExportCommand> {
    @Override
    public ExportCommand parse(String args) throws ParseException {
        String trimmed = requireNonNull(args).trim();
        String option;
        if (trimmed.equals("--csv") || trimmed.matches("(?s)--csv\\s+.*")) {
            option = "--csv";
        } else if (trimmed.equals("-c") || trimmed.matches("(?s)-c\\s+.*")) {
            option = "-c";
        } else {
            throw invalidFormat();
        }
        String fileName = trimmed.substring(option.length()).trim();
        if (fileName.isEmpty()) {
            return new ExportCommand(ExportCommand.DEFAULT_FILE_NAME);
        }
        char first = fileName.charAt(0);
        if (first == '"' || first == '\'') {
            if (fileName.indexOf(first, 1) != fileName.length() - 1) {
                throw invalidFormat();
            }
            fileName = fileName.substring(1, fileName.length() - 1);
        } else if (fileName.chars().anyMatch(Character::isWhitespace) || fileName.startsWith("--")) {
            throw invalidFormat();
        }
        Optional<String> fileNameError = CsvContactExporter.getFileNameError(fileName);
        if (fileNameError.isPresent()) {
            throw new ParseException(fileNameError.orElseThrow());
        }
        return new ExportCommand(fileName);
    }

    private ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ExportCommand.MESSAGE_USAGE));
    }
}
