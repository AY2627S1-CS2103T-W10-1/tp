package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.AddressBookReplacement;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    /** Explains that a failed save does not roll back the command's in-memory change. */
    public static final String FILE_OPS_CHANGES_KEPT_NOTE =
            "Any change is still applied in the app and will be saved the next time saving succeeds.";

    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        Command command = addressBookParser.parseCommand(commandText);
        // Log the parsed type only: raw arguments and command.toString() can contain private data.
        logger.info("Executing command: " + command.getClass().getSimpleName());
        Optional<AddressBookReplacement> replacement = command.getAddressBookReplacement();
        if (replacement.isPresent()) {
            return executeReplacement(command, replacement.get());
        }
        return executeAndSave(command);
    }

    private CommandResult executeReplacement(Command command, AddressBookReplacement replacement)
            throws CommandException {
        saveAddressBook(replacement.addressBook(), replacement.failureRecoveryMessage());
        return command.execute(model);
    }

    private CommandResult executeAndSave(Command command) throws CommandException {
        CommandResult result = command.execute(model);
        if (command.requiresStorageSave()) {
            saveAddressBook(model.getAddressBook(), FILE_OPS_CHANGES_KEPT_NOTE);
        }
        return result;
    }

    private void saveAddressBook(ReadOnlyAddressBook addressBook, String failureRecoveryMessage)
            throws CommandException {
        try {
            storage.saveAddressBook(addressBook);
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage())
                    + "\n" + failureRecoveryMessage, e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage())
                    + "\n" + failureRecoveryMessage, ioe);
        }
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return model.getAddressBook().getPersonList();
    }

    @Override
    public ObservableList<Department> getDepartmentList() {
        return model.getAddressBook().getDepartmentList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
