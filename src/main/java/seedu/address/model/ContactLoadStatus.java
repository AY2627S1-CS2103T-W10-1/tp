package seedu.address.model;

/**
 * Records whether startup contact loading succeeded or failed because of storage or invalid data.
 * A failed status remains until the application restarts and loads the data again.
 */
public enum ContactLoadStatus {
    READY,
    UNREADABLE,
    INVALID
}
