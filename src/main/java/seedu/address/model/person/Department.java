package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents a contact's department.
 */
public class Department {

    public static final String MESSAGE_CONSTRAINTS =
            "Invalid Department: Must be 2-50 characters using only letters, numbers, spaces, '-', or '&'.";
    private static final String VALIDATION_REGEX = "[A-Za-z0-9 &-]{2,50}";

    public final String value;

    /**
     * Creates a department with a valid name.
     */
    public Department(String department) {
        requireNonNull(department);
        if (!isValidDepartment(department)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        value = department;
    }

    public static boolean isValidDepartment(String test) {
        return test != null && test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof Department otherDepartment && value.equals(otherDepartment.value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
