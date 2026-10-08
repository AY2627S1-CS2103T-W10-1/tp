package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final ContactLoadStatus contactLoadStatus;
    private final ObservableList<Person> displayPersons;
    private final FilteredList<Person> filteredPersons;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        this(addressBook, userPrefs, ContactLoadStatus.READY);
    }

    /**
     * Initializes the model with contact data and the outcome of loading it at startup.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs, ContactLoadStatus loadStatus) {
        requireAllNonNull(addressBook, userPrefs, loadStatus);
        contactLoadStatus = loadStatus;

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        displayPersons = FXCollections.observableArrayList(this.addressBook.getPersonList());
        filteredPersons = new FilteredList<>(displayPersons);
        this.addressBook.getPersonList().addListener((ListChangeListener<Person>) change ->
                displayPersons.setAll(this.addressBook.getPersonList()));
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the filtered display order.
     * Sorting the display does not reorder the stored address book.
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        displayPersons.setAll(addressBook.getPersonList());
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public void sortFilteredPersonList(SortField field) {
        requireNonNull(field);
        Iterator<Person> sortedPersons = ContactSorter.sortedCopy(filteredPersons, field::getKey).iterator();
        List<Person> displayOrder = new ArrayList<>(displayPersons);
        Predicate<? super Person> predicate = filteredPersons.getPredicate();
        for (int i = 0; i < displayOrder.size(); i++) {
            if (predicate == null || predicate.test(displayOrder.get(i))) {
                displayOrder.set(i, sortedPersons.next());
            }
        }
        displayPersons.setAll(displayOrder);
    }

    @Override
    public ContactLoadStatus getContactLoadStatus() {
        return contactLoadStatus;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && contactLoadStatus == otherModelManager.contactLoadStatus
                && filteredPersons.equals(otherModelManager.filteredPersons);
    }

}
