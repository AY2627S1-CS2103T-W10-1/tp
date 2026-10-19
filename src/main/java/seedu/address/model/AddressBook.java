package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();
    private final ObservableList<Department> departments = FXCollections.observableArrayList();

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
        persons.forEach(this::registerDepartment);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        List<Person> replacementPersons = List.copyOf(newData.getPersonList());
        List<Department> replacementDepartments = List.copyOf(newData.getDepartmentList());
        departments.setAll(replacementDepartments);
        setPersons(replacementPersons);
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
        registerDepartment(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
        registerDepartment(editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    private void registerDepartment(Person person) {
        person.getDepartment().filter(department -> !departments.contains(department)).ifPresent(departments::add);
    }

    /** Adds a unique department to the catalogue. */
    public void addDepartment(Department department) {
        requireNonNull(department);
        if (departments.contains(department)) {
            throw new IllegalArgumentException("Department already exists: " + department);
        }
        departments.add(department);
    }

    /** Renames a catalogue entry and all assignments after validating both names. */
    public void renameDepartment(Department oldDepartment, Department newDepartment) {
        requireNonNull(oldDepartment);
        requireNonNull(newDepartment);
        int index = departments.indexOf(oldDepartment);
        if (index < 0 || (!oldDepartment.equals(newDepartment) && departments.contains(newDepartment))) {
            throw new IllegalArgumentException("Unknown source or duplicate target department.");
        }
        List<Person> replacements = persons.asUnmodifiableObservableList().stream()
                .map(person -> person.withRenamedDepartment(oldDepartment, newDepartment)).toList();
        departments.set(index, newDepartment);
        persons.setPersons(replacements);
    }

    @Override
    public ObservableList<Department> getDepartmentList() {
        return FXCollections.unmodifiableObservableList(departments);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons) && departments.equals(otherAddressBook.departments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, departments);
    }
}
