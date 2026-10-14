package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.UniquePersonList;
import seedu.address.model.tag.Tag;

public class ValueObjectContractTest {
    @Test
    public void addressBookEquality_supportsHashCollections() {
        AddressBook book = getTypicalAddressBook();
        assertEqualityContract(book, new AddressBook(book), new AddressBook());
    }

    @Test
    public void userPrefsEquality_comparesGuiSettings() {
        UserPrefs prefs = new UserPrefs();
        prefs.setGuiSettings(new GuiSettings(900, 700, 120, 80));
        assertEqualityContract(prefs, new UserPrefs(prefs), new UserPrefs());
    }

    @Test
    public void uniquePersonListEquality_preservesOrder() {
        UniquePersonList contacts = new UniquePersonList();
        contacts.setPersons(List.of(ALICE, BOB));
        UniquePersonList equal = new UniquePersonList();
        equal.setPersons(List.of(ALICE, BOB));
        UniquePersonList reversed = new UniquePersonList();
        reversed.setPersons(List.of(BOB, ALICE));
        assertEqualityContract(contacts, equal, reversed);
    }

    @Test
    public void uniquePersonListIterator_visitsContactsInOrder() {
        UniquePersonList contacts = new UniquePersonList();
        contacts.setPersons(List.of(ALICE, BOB));
        var iterator = contacts.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(ALICE, iterator.next());
        assertEquals(BOB, iterator.next());
        assertFalse(iterator.hasNext());
        assertThrows(NoSuchElementException.class, iterator::next);
    }

    @Test
    public void tagEquality_isCaseSensitive() {
        assertEqualityContract(new Tag("Client"), new Tag("Client"), new Tag("client"));
    }

    private static void assertEqualityContract(Object value, Object equal, Object different) {
        assertTrue(value.equals(value));
        assertEquals(value, equal);
        assertEquals(equal, value);
        assertEquals(value.hashCode(), equal.hashCode());
        assertTrue(new HashSet<>(List.of(value)).contains(equal));
        assertFalse(value.equals(different));
        assertFalse(value.equals(null));
        assertFalse(value.equals("unrelated"));
    }
}
