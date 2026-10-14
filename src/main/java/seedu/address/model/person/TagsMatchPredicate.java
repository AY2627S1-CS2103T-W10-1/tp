package seedu.address.model.person;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/** Tests whether a person has any of the specified tags, respecting case. */
public class TagsMatchPredicate implements Predicate<Person> {
    private final List<String> tags;

    public TagsMatchPredicate(List<String> tags) {
        this.tags = List.copyOf(tags);
    }

    @Override
    public boolean test(Person person) {
        return person.getTags().stream()
                .anyMatch(tag -> tags.stream().anyMatch(tag.tagName::equals));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof TagsMatchPredicate otherPredicate
                && tags.equals(otherPredicate.tags));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("tags", tags).toString();
    }
}
