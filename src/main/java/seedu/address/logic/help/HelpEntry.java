package seedu.address.logic.help;

import static java.util.Objects.requireNonNull;

import java.util.List;

/**
 * An overview entry linking a command manual to its short summary and accepted aliases.
 *
 * @param page Complete command manual, whose title is the canonical command word.
 * @param summary Concise description for the command overview.
 * @param aliases Additional command words accepted by the parser.
 */
public record HelpEntry(HelpPage page, String summary, List<String> aliases) {
    /**
     * Creates an entry with an immutable copy of its aliases.
     *
     * @throws NullPointerException If a field or alias is null.
     */
    public HelpEntry {
        requireNonNull(page);
        requireNonNull(summary);
        aliases = List.copyOf(aliases);
    }

    /**
     * Returns the canonical name followed by its aliases for display.
     */
    public String displayName() {
        return page.title() + (aliases.isEmpty() ? "" : " | " + String.join(" | ", aliases));
    }
}
