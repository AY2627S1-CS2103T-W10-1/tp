package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.List;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.help.HelpPage;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.storage.CsvContactExporter;

/**
 * Exports every stored contact, independently of the displayed list.
 */
public class ExportCommand extends Command {
    public static final String COMMAND_WORD = "export";

    /** Detailed usage, supported options and examples for this command. */
    public static final HelpPage HELP_PAGE = new HelpPage(COMMAND_WORD, String.join("\n",
            "Exports all stored contacts as UTF-8 CSV, including a header row.",
            "Usage: export --csv [FILENAME]",
            "",
            "Options:",
            "  -c, --csv  Required CSV export flag.",
            "",
            "Default filename: contacts.csv. The active filter does not limit the export.",
            "The file is created in the working directory; an existing file is never overwritten.",
            "Provide a valid .csv filename, not a directory path. Quote filenames containing spaces.",
            "Each stored contact becomes a separate row. Empty address books cannot be exported.",
            "",
            "Examples:",
            "  export --csv",
            "  export -c \"email list.csv\""));

    public static final String DEFAULT_FILE_NAME = "contacts.csv";
    public static final String MESSAGE_USAGE = "export -c|--csv [FILENAME]\n"
            + "Exports all stored contacts as UTF-8 CSV. Default filename: contacts.csv.\n"
            + "Example: export --csv email-list.csv";
    public static final String MESSAGE_INVALID_FILE_NAME = "Invalid file name: please provide a valid .csv file name.";
    public static final String MESSAGE_EMPTY_LIST = "No contacts found to export.";
    public static final String MESSAGE_SUCCESS = "Exported %1$d contacts to %2$s.";
    public static final String MESSAGE_ALREADY_EXISTS =
            "CSV file already exists: %1$s. Please choose a different filename.";
    public static final String MESSAGE_WRITE_ERROR = "Unable to create the CSV file '%1$s'. "
            + "Please check the folder's permissions and available space.";

    private final String fileName;
    private final CsvContactExporter exporter;

    public ExportCommand(String fileName) {
        this(fileName, new CsvContactExporter(Path.of(".")));
    }

    /**
     * Creates an export command using the given output service.
     */
    public ExportCommand(String fileName, CsvContactExporter exporter) {
        this.fileName = requireNonNull(fileName);
        checkArgument(CsvContactExporter.isValidFileName(fileName), MESSAGE_INVALID_FILE_NAME);
        this.exporter = requireNonNull(exporter);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> contacts = List.copyOf(model.getAddressBook().getPersonList());
        if (contacts.isEmpty()) {
            throw new CommandException(MESSAGE_EMPTY_LIST);
        }
        try {
            exporter.export(contacts, fileName);
        } catch (FileAlreadyExistsException e) {
            throw new CommandException(String.format(MESSAGE_ALREADY_EXISTS, fileName), e);
        } catch (IOException e) {
            throw new CommandException(String.format(MESSAGE_WRITE_ERROR, fileName), e);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, contacts.size(), fileName));
    }

    @Override
    public boolean requiresStorageSave() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof ExportCommand otherCommand
                && fileName.equals(otherCommand.fileName)
                && exporter.getDirectory().equals(otherCommand.exporter.getDirectory()));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("fileName", fileName).add("directory", exporter.getDirectory()).toString();
    }
}
