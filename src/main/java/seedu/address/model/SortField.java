package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.model.person.Person;

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
     * Returns the contact's sorting key for this field, or an empty optional when the field is absent.
     * Uses the department value for department sorting, or the alphabetically smallest tag using
     * case-insensitive comparison for tag sorting. Validates tags for both sorting fields.
     *
     * @param person Contact whose sorting key is returned.
     * @throws IllegalArgumentException If the contact contains a null tag.
     * @throws NullPointerException If {@code person} is null.
     */
    public Optional<String> getKey(Person person) {
        requireNonNull(person);
        Optional<String> firstTag = person.getTags().stream()
                .map(tag -> {
                    if (tag == null) {
                        throw new IllegalArgumentException("Invalid stored tag.");
                    }
                    return tag.tagName;
                })
                .min(String.CASE_INSENSITIVE_ORDER);
        Optional<String> departmentKey = person.getDepartment().map(department -> department.value);
        return this == DEPARTMENT ? departmentKey : firstTag;
    }
}
