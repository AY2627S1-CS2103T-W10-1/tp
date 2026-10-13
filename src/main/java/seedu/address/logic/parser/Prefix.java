package seedu.address.logic.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A prefix that marks the beginning of an argument in an arguments string.
 * E.g. '-t' in 'add James -t friend'.
 * A prefix may have aliases (e.g. '--tag' for '-t') that are treated as the same prefix.
 */
public class Prefix {
    private final String prefix;
    private final List<String> allForms;

    /**
     * Creates a prefix with the given primary form and optional aliases.
     *
     * @param prefix  Primary form of the prefix, used for equality and display
     * @param aliases Alternative forms that are recognised as the same prefix
     */
    public Prefix(String prefix, String... aliases) {
        this.prefix = prefix;
        List<String> forms = new ArrayList<>();
        forms.add(prefix);
        forms.addAll(Arrays.asList(aliases));
        this.allForms = Collections.unmodifiableList(forms);
    }

    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the primary form followed by all aliases of this prefix.
     */
    public List<String> getAllForms() {
        return allForms;
    }

    @Override
    public String toString() {
        return getPrefix();
    }

    @Override
    public int hashCode() {
        return prefix == null ? 0 : prefix.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Prefix otherPrefix)) {
            return false;
        }

        return prefix.equals(otherPrefix.prefix);
    }
}
