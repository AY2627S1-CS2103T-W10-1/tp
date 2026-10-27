package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final ObservableList<Person> displayPersons;
    private final FilteredList<Person> filteredPersons;
    private final List<Person> deletedPersons = new ArrayList<>();
    private final List<DepartmentDeletion> deletionOrder = new ArrayList<>();

    /** A null entry in deletionOrder represents a contact deletion. */
    private record DepartmentDeletion(Department department, Map<Person, Boolean> assignments) {
        // Assignments are keyed by identity to track later edits, so equality compares the contacts by value.
        @Override
        public boolean equals(Object other) {
            return other instanceof DepartmentDeletion otherDeletion
                    && department.equals(otherDeletion.department)
                    && assignedPersons().equals(otherDeletion.assignedPersons());
        }

        @Override
        public int hashCode() {
            return Objects.hash(department, assignedPersons());
        }

        private Set<Person> assignedPersons() {
            return Set.copyOf(assignments.keySet());
        }
    }


    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing model with " + addressBook.getPersonList().size() + " contacts.");

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
        deletedPersons.add(target);
        deletionOrder.add(null);
    }

    @Override
    public boolean hasDeletedPerson() {
        return !deletedPersons.isEmpty();
    }

    @Override
    public Person getLastDeletedPerson() {
        return deletedPersons.getLast();
    }

    @Override
    public void restoreLastDeletedPerson() {
        addPerson(deletedPersons.getLast());
        deletedPersons.removeLast();
        deletionOrder.removeLast();
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
        trackPersonReplacement(target, editedPerson);
    }

    private void trackPersonReplacement(Person target, Person editedPerson) {
        for (DepartmentDeletion deletion : deletionOrder) {
            if (deletion != null && deletion.assignments().remove(target) != null) {
                deletion.assignments().put(editedPerson, true);
            }
        }
    }

    @Override
    public void addDepartment(Department department) {
        addressBook.addDepartment(department);
    }

    @Override
    public void renameDepartment(Department oldDepartment, Department newDepartment) {
        addressBook.renameDepartment(oldDepartment, newDepartment);
        deletedPersons.replaceAll(person -> person.withRenamedDepartment(oldDepartment, newDepartment));
    }

    @Override
    public void deleteDepartment(Department department) {
        Map<Person, Boolean> assignments = new IdentityHashMap<>();
        addressBook.getPersonList().stream()
                .filter(person -> person.getDepartment().filter(department::equals).isPresent())
                .forEach(person -> assignments.put(person, true));
        deletedPersons.stream().filter(person -> person.getDepartment().filter(department::equals).isPresent())
                .forEach(person -> assignments.put(person, true));
        // Keep the exact replacement objects so later contact edits can be tracked without matching by name.
        List<Person> before = List.copyOf(addressBook.getPersonList());
        addressBook.deleteDepartment(department);
        Map<Person, Boolean> cleared = new IdentityHashMap<>();
        for (int i = 0; i < before.size(); i++) {
            if (assignments.containsKey(before.get(i))) {
                cleared.put(addressBook.getPersonList().get(i), true);
            }
        }
        deletedPersons.replaceAll(person -> {
            Person replacement = person.withoutDepartment(department);
            if (assignments.containsKey(person)) {
                cleared.put(replacement, true);
            }
            return replacement;
        });
        deletionOrder.add(new DepartmentDeletion(department, cleared));
    }

    @Override
    public Department getLastDeletedDepartment() {
        if (deletionOrder.isEmpty() || deletionOrder.getLast() == null) {
            return null;
        }
        return deletionOrder.getLast().department();
    }

    @Override
    public void restoreLastDeletedDepartment() {
        DepartmentDeletion deletion = deletionOrder.getLast();
        addressBook.addDepartment(deletion.department());
        List<Person> before = List.copyOf(addressBook.getPersonList());
        List<Person> restored = before.stream().map(person -> deletion.assignments().containsKey(person)
                && person.getDepartment().isEmpty() ? withDepartment(person, deletion.department()) : person).toList();
        // Department restoration leaves identities unchanged, including conflicts already present in legacy data.
        addressBook.setPersons(restored);
        for (int i = 0; i < before.size(); i++) {
            trackPersonReplacement(before.get(i), restored.get(i));
        }
        deletedPersons.replaceAll(person -> deletion.assignments().containsKey(person)
                && person.getDepartment().isEmpty() ? withDepartment(person, deletion.department()) : person);
        deletionOrder.removeLast();
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void discardLastDeletion() {
        if (deletionOrder.removeLast() == null) {
            deletedPersons.removeLast();
        }
    }

    private Person withDepartment(Person person, Department department) {
        return new Person(person.getName(), person.getPhone(), person.getEmail(), person.getAddress(),
                person.getTags(), department);
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
                && filteredPersons.equals(otherModelManager.filteredPersons)
                && deletedPersons.equals(otherModelManager.deletedPersons)
                && deletionOrder.equals(otherModelManager.deletionOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressBook, userPrefs, filteredPersons, deletedPersons, deletionOrder);
    }

}
