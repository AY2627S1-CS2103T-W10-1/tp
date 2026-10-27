package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.logic.help.HelpPage;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Shows a warning or clears the address book when explicitly confirmed.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";

    /** Detailed usage, supported options and examples for this command. */
    public static final HelpPage HELP_PAGE = new HelpPage(COMMAND_WORD, String.join("\n",
            "Deletes all contacts and registered departments from the address book.",
            "Usage: clear [--confirm]",
            "",
            "Run clear to see the total contact count without deleting anything.",
            "Run clear --confirm to delete all contacts, including those hidden by a filter,",
            "and all registered departments, including unused entries.",
            "Only --confirm is accepted. Clearing the address book cannot be undone.",
            "Earlier individual deletions remain available to undo in this session.",
            "If saving fails, clearing is not applied; contacts and departments remain in this session.",
            "Resolve the save error, then retry clear --confirm or run list to save the retained data.",
            "",
            "Examples:",
            "  clear",
            "  clear --confirm"));

    public static final String MESSAGE_SUCCESS = "Address book has been cleared!";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows a warning before deleting all contacts and registered departments.\n"
            + "Use " + COMMAND_WORD + " --confirm to delete all contacts and registered departments permanently.";

    public static final String MESSAGE_CONFIRMATION =
            "This will delete all %d contacts and all registered departments and cannot be undone. "
            + "Type \"clear --confirm\" to proceed.";

    public static final String MESSAGE_SAVE_FAILURE_RECOVERY =
            "Clear was not applied. Contacts and departments remain available in this session. "
            + "Resolve the save error, then retry clear --confirm or run list to save the retained data.";

    private final boolean isConfirmed;
    private final AddressBookReplacement replacement =
            new AddressBookReplacement(new AddressBook(), MESSAGE_SAVE_FAILURE_RECOVERY);

    /**
     * Creates a command that shows a warning without deleting contacts.
     */
    public ClearCommand() {
        this(false);
    }

    /**
     * Creates a clear command with the specified confirmation state.
     */
    public ClearCommand(boolean isConfirmed) {
        this.isConfirmed = isConfirmed;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        if (!isConfirmed) {
            return new CommandResult(String.format(MESSAGE_CONFIRMATION,
                    model.getAddressBook().getPersonList().size()));
        }
        model.setAddressBook(replacement.addressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }

    /**
     * Returns the empty address book to save before a confirmed clear changes the model.
     */
    @Override
    public Optional<AddressBookReplacement> getAddressBookReplacement() {
        return isConfirmed ? Optional.of(replacement) : Optional.empty();
    }

    @Override
    public boolean requiresStorageSave() {
        return isConfirmed;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof ClearCommand otherCommand && isConfirmed == otherCommand.isConfirmed);
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(isConfirmed);
    }
}
