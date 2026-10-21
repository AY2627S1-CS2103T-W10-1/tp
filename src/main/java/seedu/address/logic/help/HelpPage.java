package seedu.address.logic.help;

import static java.util.Objects.requireNonNull;

import java.util.List;

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

    /**
     * Returns the commands in the manual's {@code Examples:} section as an immutable list.
     * Leading indentation is removed, and the section ends at the next blank line.
     * Returns an empty list when the page has no examples section.
     */
    public List<String> examples() {
        return text.lines()
                .dropWhile(line -> !line.strip().equals("Examples:"))
                .skip(1)
                .takeWhile(line -> !line.isBlank())
                .map(String::strip)
                .toList();
    }

}
