package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; omitted or valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    private static final String SPECIAL_CHARACTERS = "+_.-";
    public static final String MESSAGE_CONSTRAINTS = "Emails should be of the format local-part@domain "
            + "and adhere to the following constraints:\n"
            + "1. The local-part should only contain alphanumeric characters and these special characters, excluding "
            + "the parentheses, (" + SPECIAL_CHARACTERS + "). The local-part may not start or end with any special "
            + "characters.\n"
            + "2. The local-part is followed by an '@' and then a domain name. The domain name is made up of domain "
            + "labels separated by periods.\n"
            + "The domain name must:\n"
            + "    - end with a domain label at least 2 characters long\n"
            + "    - have each domain label start and end with alphanumeric characters\n"
            + "    - have each domain label consist of alphanumeric characters, separated only by hyphens, if any.";
    // alphanumeric and special characters
    private static final String ALPHANUMERIC_NO_UNDERSCORE = "[^\\W_]+"; // alphanumeric characters except underscore
    private static final String LOCAL_PART_REGEX = "^" + ALPHANUMERIC_NO_UNDERSCORE + "([" + SPECIAL_CHARACTERS + "]"
            + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    private static final String DOMAIN_PART_REGEX = ALPHANUMERIC_NO_UNDERSCORE
            + "(-" + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    private static final String DOMAIN_LAST_PART_REGEX = "(" + DOMAIN_PART_REGEX + "){2,}$"; // At least two chars
    private static final String DOMAIN_REGEX = "(" + DOMAIN_PART_REGEX + "\\.)*" + DOMAIN_LAST_PART_REGEX;
    public static final String VALIDATION_REGEX = LOCAL_PART_REGEX + "@" + DOMAIN_REGEX;

    public final String value;

    /**
     * Constructs an {@code Email}.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        checkArgument(isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = email;
    }

    private Email() {
        value = "";
    }

    /** Returns an omitted optional field, without accepting blank user input. */
    public static Email notProvided() {
        return new Email();
    }

    /** Returns whether this field contains supplied information. */
    public boolean isProvided() {
        return !value.isEmpty();
    }

    /**
     * Returns true if a given string is a valid email.
     * Uses an iterative scan rather than the nested syntax regex so long inputs
     * cannot exhaust the Java call stack. The accepted syntax remains unchanged.
     */
    public static boolean isValidEmail(String test) {
        requireNonNull(test);
        int atIndex = test.indexOf('@');
        if (atIndex <= 0 || !hasValidSeparators(test, 0, atIndex, SPECIAL_CHARACTERS)
                || !hasValidSeparators(test, atIndex + 1, test.length(), "-.")) {
            return false;
        }

        // The existing final-label regex requires a split between two adjacent
        // alphanumeric characters. Preserve that rule for stored-data compatibility.
        int lastLabelStart = test.lastIndexOf('.') + 1;
        lastLabelStart = Math.max(lastLabelStart, atIndex + 1);
        for (int i = lastLabelStart + 1; i < test.length(); i++) {
            if (isAlphanumeric(test.charAt(i - 1)) && isAlphanumeric(test.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks a nonempty part with alphanumeric ends and single allowed separators.
     */
    private static boolean hasValidSeparators(String value, int start, int end, String separators) {
        boolean isPreviousAlphanumeric = false;
        for (int i = start; i < end; i++) {
            char character = value.charAt(i);
            if (isAlphanumeric(character)) {
                isPreviousAlphanumeric = true;
            } else if (isPreviousAlphanumeric && separators.indexOf(character) >= 0) {
                isPreviousAlphanumeric = false;
            } else {
                return false;
            }
        }
        return isPreviousAlphanumeric;
    }

    private static boolean isAlphanumeric(char character) {
        return (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z')
                || (character >= '0' && character <= '9');
    }

    @Override
    public String toString() {
        return isProvided() ? value : "Not provided";
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
