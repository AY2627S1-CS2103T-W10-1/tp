package seedu.address.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Tag in the address book.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 */
public class Tag {

    public static final String MESSAGE_CONSTRAINTS = "Tags must be 1-30 characters using only letters, numbers, "
            + "hyphens (-), and underscores (_), with no spaces.";
    public static final String VALIDATION_REGEX = "[a-zA-Z0-9_-]{1,30}";

    public final String tagName;

    /**
     * Constructs a {@code Tag}.
     *
     * @param tagName A valid tag name.
     */
    public Tag(String tagName) {
        this(tagName, VALIDATION_REGEX);
    }

    private Tag(String tagName, String validationRegex) {
        requireNonNull(tagName);
        checkArgument(tagName.matches(validationRegex), MESSAGE_CONSTRAINTS);
        this.tagName = tagName;
    }

    /** Reads a stored tag, preserving older tags that exceed the current input length limit. */
    public static Tag fromStoredValue(String tagName) {
        return new Tag(tagName, "[a-zA-Z0-9_-]+");
    }

    /**
     * Returns true if a given string is a valid tag name.
     */
    public static boolean isValidTagName(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Tag otherTag)) {
            return false;
        }

        return tagName.equals(otherTag.tagName);
    }

    @Override
    public int hashCode() {
        return tagName.hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
