package seedu.address.logic.help;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DepartCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.ExportCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.ViewCommand;

/**
 * Ordered collection of command manuals with case-insensitive name and alias lookup.
 */
public final class HelpCatalog {
    private static final List<HelpEntry> entries = List.of(
            entry(AddCommand.HELP_PAGE, "Add a contact."),
            entry(EditCommand.HELP_PAGE, "Edit contact details."),
            entry(DepartCommand.HELP_PAGE, "Manage departments."),
            entry(FindCommand.HELP_PAGE, "Search names or tags."),
            entry(ViewCommand.HELP_PAGE, "View full contact details."),
            entry(ListCommand.HELP_PAGE, "List all contacts.", ListCommand.COMMAND_ALIAS),
            entry(SortCommand.HELP_PAGE, "Sort displayed contacts."),
            entry(DeleteCommand.HELP_PAGE, "Delete a contact."),
            entry(UndoCommand.HELP_PAGE, "Restore the latest deletion."),
            entry(ExportCommand.HELP_PAGE, "Export contacts to CSV."),
            entry(ClearCommand.HELP_PAGE, "Clear contacts and departments."),
            entry(HelpCommand.HELP_PAGE, "Show command help.", HelpCommand.COMMAND_ALIAS),
            entry(ExitCommand.HELP_PAGE, "Close the application."));
    private static final List<HelpPage> pages = entries.stream().map(HelpEntry::page).toList();
    private static final String OVERVIEW_GUIDANCE = "Use help COMMAND or ? COMMAND for usage, options and examples."
            + "\nINDEX and CONTACT_ID are one-based positions in the displayed list."
            + "\nCommand names are case-insensitive.";
    private static final HelpPage overview = new HelpPage("Available commands",
            entries.stream().map(entry -> entry.displayName() + " — " + entry.summary())
                    .collect(Collectors.joining("\n")) + "\n\n" + OVERVIEW_GUIDANCE);

    private HelpCatalog() {
    }

    /**
     * Returns immutable overview entries in command display order.
     */
    public static List<HelpEntry> getEntries() {
        return entries;
    }

    /**
     * Returns the guidance displayed beneath the command overview.
     */
    public static String getOverviewGuidance() {
        return OVERVIEW_GUIDANCE;
    }

    /**
     * Returns every command page in overview order as an immutable list.
     */
    public static List<HelpPage> getPages() {
        return pages;
    }

    /**
     * Returns the overview of all supported commands and their aliases.
     */
    public static HelpPage getOverview() {
        return overview;
    }

    /**
     * Resolves a command name or alias, ignoring case and surrounding whitespace.
     *
     * @param commandName Command word or alias, including {@code ls} and {@code ?}.
     * @return The command's manual, or an empty optional for an unknown name.
     * @throws NullPointerException If {@code commandName} is null.
     */
    public static Optional<HelpPage> findPage(String commandName) {
        String name = requireNonNull(commandName).trim().toLowerCase(Locale.ROOT);
        return entries.stream().filter(entry -> entry.page().title().equals(name) || entry.aliases().contains(name))
                .map(HelpEntry::page).findFirst();
    }

    private static HelpEntry entry(HelpPage page, String summary, String... aliases) {
        return new HelpEntry(page, summary, List.of(aliases));
    }
}
