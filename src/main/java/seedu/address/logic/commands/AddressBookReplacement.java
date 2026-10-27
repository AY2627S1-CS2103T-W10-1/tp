package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.ReadOnlyAddressBook;

/**
 * Describes replacement data to persist before a command changes the live model.
 *
 * @param addressBook Replacement data that the command applies after a successful save.
 * @param failureRecoveryMessage Recovery instructions when saving prevents command execution.
 */
public record AddressBookReplacement(ReadOnlyAddressBook addressBook, String failureRecoveryMessage) {
    /**
     * Creates a replacement with non-null data and recovery instructions.
     */
    public AddressBookReplacement {
        requireNonNull(addressBook);
        requireNonNull(failureRecoveryMessage);
    }
}
