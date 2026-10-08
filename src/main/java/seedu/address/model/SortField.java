package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.model.person.Department;
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
     *
     * @throws IllegalArgumentException If a stored department or tag is invalid.
     */
    public Optional<String> getKey(Person person) {
        requireNonNull(person);
        Optional<String> firstTag = person.getTags().stream()
                .map(tag -> {
                    if (tag == null || tag.tagName == null || !Tag.isValidTagName(tag.tagName)) {
                        throw new IllegalArgumentException("Invalid stored tag.");
                    }
                    return tag.tagName;
                })
                .min(String.CASE_INSENSITIVE_ORDER);
        Optional<String> departmentKey = person.getDepartment().map(department -> {
            if (!Department.isValidDepartment(department.value)) {
                throw new IllegalArgumentException("Invalid stored department.");
            }
            return department.value;
        });
        return this == DEPARTMENT ? departmentKey : firstTag;
    }
}
