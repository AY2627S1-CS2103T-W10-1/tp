package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import seedu.address.model.person.Person;

/**
 * Creates stable contact orderings without modifying the input or collapsing duplicates.
 */
public final class ContactSorter {

    private ContactSorter() {}

    /**
     * Returns an ascending, case-insensitive copy with absent keys last and equal keys in encounter order.
     * Computes every key before sorting so invalid data cannot partially reorder the input.
     */
    public static List<Person> sortedCopy(List<Person> persons, Function<Person, Optional<String>> keyExtractor) {
        requireNonNull(persons);
        requireNonNull(keyExtractor);
        List<SortEntry> entries = new ArrayList<>();
        for (Person person : persons) {
            requireNonNull(person);
            Optional<String> key = requireNonNull(keyExtractor.apply(person));
            entries.add(new SortEntry(person, key.orElse(null)));
        }

        entries.sort(Comparator.comparing(entry -> entry.key,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return entries.stream().map(entry -> entry.person).toList();
    }

    /**
     * Holds a contact occurrence and its precomputed key.
     */
    private static class SortEntry {
        private final Person person;
        private final String key;

        SortEntry(Person person, String key) {
            this.person = person;
            this.key = key;
        }
    }
}
