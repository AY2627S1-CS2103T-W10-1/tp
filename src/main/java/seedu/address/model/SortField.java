package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Identifies the contact field used for ascending display sorting.
 */
public enum SortField {
    DEPARTMENT("department"),
    TAGS("tags");

    private final String displayName;

    SortField(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the contact's sorting key, or an empty optional when the field is absent.
     * Departments are placeholders until department storage is implemented.
     *
     * @throws IllegalArgumentException If a stored tag is invalid.
     */
    public Optional<String> getKey(Person person) {
        requireNonNull(person);
        if (this == DEPARTMENT) {
            return Optional.empty();
        }

        return person.getTags().stream()
                .map(tag -> {
                    if (tag == null || tag.tagName == null || !Tag.isValidTagName(tag.tagName)) {
                        throw new IllegalArgumentException("Invalid stored tag.");
                    }
                    return tag.tagName;
                })
                .min(String.CASE_INSENSITIVE_ORDER);
    }
}
