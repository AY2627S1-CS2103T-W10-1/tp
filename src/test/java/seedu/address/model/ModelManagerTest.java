package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Department;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasDeletedPerson_noDeletion_returnsFalse() {
        assertFalse(modelManager.hasDeletedPerson());
    }

    @Test
    public void deletePerson_personInAddressBook_recordsDeletion() {
        modelManager.addPerson(ALICE);
        modelManager.deletePerson(ALICE);
        assertTrue(modelManager.hasDeletedPerson());
        assertEquals(ALICE, modelManager.getLastDeletedPerson());
    }

    @Test
    public void getLastDeletedPerson_noDeletion_throwsNoSuchElementException() {
        assertThrows(NoSuchElementException.class, () -> modelManager.getLastDeletedPerson());
    }

    @Test
    public void restoreLastDeletedPerson_afterDelete_addsPersonBack() {
        modelManager.addPerson(ALICE);
        modelManager.deletePerson(ALICE);
        modelManager.restoreLastDeletedPerson();
        assertTrue(modelManager.hasPerson(ALICE));
        assertFalse(modelManager.hasDeletedPerson());
    }

    @Test
    public void restoreLastDeletedPerson_duplicatePerson_keepsDeletion() {
        modelManager.addPerson(ALICE);
        modelManager.deletePerson(ALICE);
        modelManager.addPerson(ALICE);
        assertThrows(DuplicatePersonException.class, () -> modelManager.restoreLastDeletedPerson());
        assertEquals(ALICE, modelManager.getLastDeletedPerson());
    }

    @Test
    public void discardLastDeletion_contactDeletion_dropsPersonWithoutRestoring() {
        modelManager.addPerson(ALICE);
        modelManager.deletePerson(ALICE);
        modelManager.discardLastDeletion();
        assertFalse(modelManager.hasPerson(ALICE));
        assertFalse(modelManager.hasDeletedPerson());
    }

    @Test
    public void discardLastDeletion_departmentDeletion_keepsEarlierContactDeletion() {
        Department engineering = new Department("Engineering");
        modelManager.addPerson(ALICE);
        modelManager.deletePerson(ALICE);
        modelManager.addDepartment(engineering);
        modelManager.deleteDepartment(engineering);
        modelManager.discardLastDeletion();
        assertFalse(modelManager.hasDepartment(engineering));
        assertNull(modelManager.getLastDeletedDepartment());
        assertEquals(ALICE, modelManager.getLastDeletedPerson());
    }

    @Test
    public void discardLastDeletion_noDeletion_throwsNoSuchElementException() {
        assertThrows(NoSuchElementException.class, () -> modelManager.discardLastDeletion());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));

        // different deletion history -> returns false
        ModelManager modelManagerWithDeletion = new ModelManager(addressBook, userPrefs);
        modelManagerWithDeletion.deletePerson(BENSON);
        AddressBook addressBookWithoutBenson = new AddressBookBuilder().withPerson(ALICE).build();
        assertFalse(modelManagerWithDeletion.equals(new ModelManager(addressBookWithoutBenson, userPrefs)));
    }

    @Test
    public void equals_departmentDeletionHistory_returnsFalse() {
        Department engineering = new Department("Engineering");
        modelManager.addDepartment(engineering);
        modelManager.deleteDepartment(engineering);
        assertFalse(modelManager.equals(new ModelManager()));
    }

    @Test
    public void equals_differentDeletedDepartment_returnsFalse() {
        Department engineering = new Department("Engineering");
        ModelManager engineeringDeleted = new ModelManager();
        engineeringDeleted.addDepartment(engineering);
        engineeringDeleted.deleteDepartment(engineering);

        Department sales = new Department("Sales");
        ModelManager salesDeleted = new ModelManager();
        salesDeleted.addDepartment(sales);
        salesDeleted.deleteDepartment(sales);

        // Both address books end up empty, so only the deleted department differs.
        assertEquals(engineeringDeleted.getAddressBook(), salesDeleted.getAddressBook());
        assertFalse(engineeringDeleted.equals(salesDeleted));
    }

    @Test
    public void equals_differentDeletionOrder_returnsFalse() {
        Department engineering = new Department("Engineering");
        Person engineer = new PersonBuilder(ALICE).withDepartment("Engineering").build();
        AddressBook addressBook = new AddressBookBuilder().withPerson(engineer).build();

        ModelManager contactFirst = new ModelManager(addressBook, new UserPrefs());
        contactFirst.deletePerson(engineer);
        contactFirst.deleteDepartment(engineering);

        ModelManager departmentFirst = new ModelManager(addressBook, new UserPrefs());
        departmentFirst.deleteDepartment(engineering);
        departmentFirst.deletePerson(departmentFirst.getAddressBook().getPersonList().get(0));

        // Same data and deleted contacts, but undo restores the department first in only one of them.
        assertEquals(contactFirst.getAddressBook(), departmentFirst.getAddressBook());
        assertEquals(contactFirst.getLastDeletedPerson(), departmentFirst.getLastDeletedPerson());
        assertFalse(contactFirst.equals(departmentFirst));
        assertFalse(departmentFirst.equals(contactFirst));
    }

    @Test
    public void equals_differentClearedAssignments_returnsFalse() {
        Department engineering = new Department("Engineering");
        ModelManager assigned = new ModelManager(new AddressBookBuilder()
                .withPerson(new PersonBuilder(ALICE).withDepartment("Engineering").build()).build(), new UserPrefs());
        assigned.deleteDepartment(engineering);

        ModelManager unassigned = new ModelManager(new AddressBookBuilder().withPerson(ALICE).build(), new UserPrefs());
        unassigned.addDepartment(engineering);
        unassigned.deleteDepartment(engineering);

        // Same data, but undo reassigns Engineering to Alice in only one of them.
        assertEquals(assigned.getAddressBook(), unassigned.getAddressBook());
        assertFalse(assigned.equals(unassigned));
    }

    @Test
    public void equals_sameHistoryBuiltSeparately_returnsTrue() {
        Department engineering = new Department("Engineering");
        AddressBook addressBook = new AddressBookBuilder()
                .withPerson(new PersonBuilder(ALICE).withDepartment("Engineering").build()).withPerson(BENSON).build();

        ModelManager first = new ModelManager(addressBook, new UserPrefs());
        first.deletePerson(BENSON);
        first.deleteDepartment(engineering);

        ModelManager second = new ModelManager(addressBook, new UserPrefs());
        second.deletePerson(BENSON);
        second.deleteDepartment(engineering);

        assertTrue(first.equals(second));
        assertEquals(first.hashCode(), second.hashCode());
    }
}
