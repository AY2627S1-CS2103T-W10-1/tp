package seedu.address.logic.help;

import static java.util.Objects.requireNonNull;

/**
 * Immutable heading and formatted content for a command manual or the help overview.
 *
 * @param title Heading displayed above the help text.
 * @param text Read-only manual text, including usage and examples.
 */
public record HelpPage(String title, String text) {
    /**
     * Creates a page with non-null heading and content.
     *
     * @throws NullPointerException If either argument is null.
     */
    public HelpPage {
        requireNonNull(title);
        requireNonNull(text);
    }
}
