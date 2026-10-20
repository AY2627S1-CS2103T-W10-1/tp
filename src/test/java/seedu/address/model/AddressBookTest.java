package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.PersonBuilder;

public class AddressBookTest {

    private final AddressBook addressBook = new AddressBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getPersonList());
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        AddressBook newData = getTypicalAddressBook();
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // Two persons with the same identity fields
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Person> newPersons = List.of(ALICE, editedAlice);
        AddressBookStub newData = new AddressBookStub(newPersons);

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        assertTrue(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInAddressBook_returnsTrue() {
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(addressBook.hasPerson(editedAlice));
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName() + "{persons=" + addressBook.getPersonList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    @Test
    public void addDepartment_duplicate_rejectsWithoutChangingData() {
        Department engineering = new Department("Engineering");
        addressBook.addDepartment(engineering);
        AddressBook before = new AddressBook(addressBook);

        assertThrows(IllegalArgumentException.class, "Department already exists: Engineering", () ->
                addressBook.addDepartment(new Department("Engineering")));
        assertEquals(before, addressBook);
    }

    @Test
    public void deleteDepartment_unknownOrNull_preservesCatalogueAndContacts() {
        addressBook.addPerson(new PersonBuilder(ALICE).withDepartment("Engineering").build());
        AddressBook before = new AddressBook(addressBook);

        assertThrows(IllegalArgumentException.class, "Unknown department: Unknown", () ->
                addressBook.deleteDepartment(new Department("Unknown")));
        assertEquals(before, addressBook);
        assertThrows(NullPointerException.class, () -> addressBook.deleteDepartment(null));
        assertEquals(before, addressBook);
    }

    @Test
    public void renameDepartment_invalidSourceOrDuplicateTarget_preservesCatalogueAndContacts() {
        Department engineering = new Department("Engineering");
        Department sales = new Department("Sales");
        addressBook.addPerson(new PersonBuilder(ALICE).withDepartment("Engineering").build());
        addressBook.addDepartment(sales);
        AddressBook before = new AddressBook(addressBook);

        assertThrows(IllegalArgumentException.class, () ->
                addressBook.renameDepartment(new Department("Unknown"), sales));
        assertEquals(before, addressBook);
        assertThrows(IllegalArgumentException.class, () -> addressBook.renameDepartment(engineering, sales));
        assertEquals(before, addressBook);
    }

    @Test
    public void renameDepartment_mixedAssignments_changesOnlyMatchingContacts() {
        Person engineeringContact = new PersonBuilder(ALICE).withDepartment("Engineering").build();
        Person salesContact = new PersonBuilder().withName("Sales Contact").withDepartment("Sales").build();
        Person unassignedContact = new PersonBuilder().withName("Unassigned Contact").build();
        addressBook.setPersons(List.of(engineeringContact, salesContact, unassignedContact));

        addressBook.renameDepartment(new Department("Engineering"), new Department("Research"));

        assertEquals(List.of(new PersonBuilder(engineeringContact).withDepartment("Research").build(),
                salesContact, unassignedContact), addressBook.getPersonList());
        assertEquals(List.of(new Department("Research"), new Department("Sales")), addressBook.getDepartmentList());
    }

    @Test
    public void equalsAndHashCode_includeUnusedCatalogueEntries() {
        addressBook.addPerson(ALICE);
        AddressBook copy = new AddressBook(addressBook);
        assertEquals(addressBook, copy);
        assertEquals(addressBook.hashCode(), copy.hashCode());

        copy.addDepartment(new Department("Unused"));
        assertFalse(addressBook.equals(copy));
        addressBook.addDepartment(new Department("Unused"));
        assertEquals(addressBook, copy);
        assertEquals(addressBook.hashCode(), copy.hashCode());
    }

    /**
     * A stub ReadOnlyAddressBook whose persons list can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();

        AddressBookStub(Collection<Person> persons) {
            this.persons.setAll(persons);
        }

        @Override
        public ObservableList<Department> getDepartmentList() {
            return FXCollections.emptyObservableList();
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }
    }

}
