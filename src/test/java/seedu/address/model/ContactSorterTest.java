package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class ContactSorterTest {
    private final Person alex = new PersonBuilder().withName("Alex").withTags("Zulu", "mentor").build();
    private final Person beatrice = new PersonBuilder().withName("Beatrice").withTags("client").build();
    private final Person chen = new PersonBuilder().withName("Chen").withTags("MENTOR").build();
    private final Person dana = new PersonBuilder().withName("Dana").build();

    @Test
    public void sortedCopy_tags_usesSmallestTagAndPreservesCaseInsensitiveTies() {
        List<Person> original = new ArrayList<>(List.of(dana, alex, beatrice, chen));
        assertEquals(List.of(beatrice, alex, chen, dana), ContactSorter.sortedCopy(original, SortField.TAGS::getKey));
        assertEquals(List.of(dana, alex, beatrice, chen), original);
        assertEquals("mentor", SortField.TAGS.getKey(alex).orElseThrow());
    }

    @Test
    public void sortedCopy_departments_sortsAbsentLastAndPreservesTies() {
        Person engineering = new PersonBuilder(alex).withDepartment("Engineering").build();
        Person marketing = new PersonBuilder(beatrice).withDepartment("Marketing").build();
        Person sameDepartment = new PersonBuilder(chen).withDepartment("engineering").build();
        List<Person> original = List.of(dana, sameDepartment, marketing, engineering);
        assertEquals(List.of(sameDepartment, engineering, marketing, dana),
                ContactSorter.sortedCopy(original, SortField.DEPARTMENT::getKey));
        assertEquals("Engineering", SortField.DEPARTMENT.getKey(engineering).orElseThrow());
        assertEquals(List.of(dana, sameDepartment, marketing, engineering), original);
    }

    @Test
    public void sortedCopy_missingDepartments_preservesOrderAndDuplicateOccurrences() {
        List<Person> persons = List.of(chen, alex, chen, dana);
        assertEquals(persons, ContactSorter.sortedCopy(persons, SortField.DEPARTMENT::getKey));
        assertEquals(Optional.empty(), SortField.DEPARTMENT.getKey(alex));
    }

    @Test
    public void sortedCopy_emptyList_returnsEmptyList() {
        assertEquals(List.of(), ContactSorter.sortedCopy(List.of(), SortField.TAGS::getKey));
    }

    @Test
    public void sortedCopy_singleInvalidContact_rejectsInvalidDataWithoutMutatingInput() {
        Set<Tag> invalidTags = new HashSet<>();
        invalidTags.add(null);
        Person invalid = new Person(alex.getName(), alex.getPhone(), alex.getEmail(), alex.getAddress(), invalidTags);
        List<Person> persons = new ArrayList<>(List.of(invalid));
        assertThrows(IllegalArgumentException.class, () -> ContactSorter.sortedCopy(persons, SortField.TAGS::getKey));
        assertEquals(List.of(invalid), persons);
    }
}
