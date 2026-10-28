package seedu.address.logic.commands;

import java.util.Optional;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

    /**
     * Returns replacement data that must be saved before this command executes, if any.
     * Preparing a replacement must not modify the model. After a successful save, execution must apply
     * that same replacement. A replacement takes precedence over the normal save after execution.
     */
    public Optional<AddressBookReplacement> getAddressBookReplacement() {
        return Optional.empty();
    }

    /**
     * Returns whether the command requires an address book save.
     * Commands with a replacement save it before execution; other commands save the live book afterward.
     */
    public boolean requiresStorageSave() {
        return true;
    }

}
