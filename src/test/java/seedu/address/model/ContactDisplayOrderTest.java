package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class ContactDisplayOrderTest {
    private final Person alex = new PersonBuilder().withName("Alex").withTags("zulu").build();
    private final Person beatrice = new PersonBuilder().withName("Beatrice").withTags("alpha").build();
    private final Person chen = new PersonBuilder().withName("Chen").withTags("ALPHA").build();
    private final ModelManager model = new ModelManager();

    private void addContacts() {
        model.addPerson(alex);
        model.addPerson(beatrice);
        model.addPerson(chen);
    }

    @Test
    public void sortFilteredPersonList_preservesStoredOrderAndObservableList() {
        addContacts();
        ObservableList<Person> displayed = model.getFilteredPersonList();
        model.sortFilteredPersonList(SortField.TAGS);
        assertSame(displayed, model.getFilteredPersonList());
        assertEquals(List.of(beatrice, chen, alex), displayed);
        assertEquals(List.of(alex, beatrice, chen), model.getAddressBook().getPersonList());
        assertThrows(UnsupportedOperationException.class, () -> displayed.remove(0));
    }

    @Test
    public void sortFilteredPersonList_preservesFilterAndCurrentOrderOfTies() {
        addContacts();
        model.updateFilteredPersonList(person -> !person.equals(chen));
        model.sortFilteredPersonList(SortField.TAGS);
        assertEquals(List.of(beatrice, alex), model.getFilteredPersonList());
        model.sortFilteredPersonList(SortField.DEPARTMENT);
        assertEquals(List.of(beatrice, alex), model.getFilteredPersonList());
        model.sortFilteredPersonList(SortField.TAGS);
        assertEquals(List.of(beatrice, alex), model.getFilteredPersonList());
    }

    @Test
    public void updateFilteredPersonList_resetsSortToStoredOrder() {
        addContacts();
        model.sortFilteredPersonList(SortField.TAGS);
        model.updateFilteredPersonList(Model.PREDICATE_SHOW_ALL_PERSONS);
        assertEquals(List.of(alex, beatrice, chen), model.getFilteredPersonList());
    }

    @Test
    public void contactMutations_refreshDisplayWithoutChangingIndexTargets() {
        addContacts();
        model.sortFilteredPersonList(SortField.TAGS);
        model.deletePerson(model.getFilteredPersonList().get(0));
        assertEquals(List.of(alex, chen), model.getFilteredPersonList());
        Person edited = new PersonBuilder(alex).withTags("beta").build();
        model.setPerson(alex, edited);
        assertEquals(List.of(edited, chen), model.getFilteredPersonList());
        model.addPerson(beatrice);
        assertEquals(List.of(edited, chen, beatrice), model.getFilteredPersonList());
        model.setAddressBook(new AddressBook());
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void sortFilteredPersonList_invalidData_leavesPreviousDisplayOrder() {
        Set<Tag> tags = new HashSet<>();
        tags.add(null);
        Person invalid = new Person(chen.getName(), chen.getPhone(), chen.getEmail(), chen.getAddress(), tags);
        model.addPerson(alex);
        model.addPerson(invalid);
        List<Person> previousOrder = List.copyOf(model.getFilteredPersonList());
        assertThrows(IllegalArgumentException.class, () -> model.sortFilteredPersonList(SortField.TAGS));
        assertEquals(previousOrder, model.getFilteredPersonList());
    }
}
