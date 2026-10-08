package seedu.address.model;

import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person and records the deletion so that it can be undone.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Returns true if a person deleted in this session has not been restored yet.
     */
    boolean hasDeletedPerson();

    /**
     * Returns the most recently deleted person that has not been restored yet, without restoring it.
     * There must be at least one such person.
     */
    Person getLastDeletedPerson();

    /**
     * Adds the most recently deleted person back to the address book and removes it from the deletion history.
     * There must be at least one such person, and it must not already exist in the address book.
     */
    void restoreLastDeletedPerson();

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Sorts the displayed contacts without changing the filter or stored contact order.
     * Preserves the current relative order of contacts with equal sorting keys.
     *
     * @throws IllegalArgumentException If a contact contains invalid sorting data.
     */
    void sortFilteredPersonList(SortField field);

    /**
     * Returns the contact loading status recorded at application startup.
     */
    ContactLoadStatus getContactLoadStatus();
}
