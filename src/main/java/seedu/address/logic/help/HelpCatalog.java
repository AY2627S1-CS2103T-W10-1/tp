package seedu.address.logic.help;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private static final List<HelpPage> pages = List.of(AddCommand.HELP_PAGE, EditCommand.HELP_PAGE,
            DepartCommand.HELP_PAGE, FindCommand.HELP_PAGE, ViewCommand.HELP_PAGE, ListCommand.HELP_PAGE,
            SortCommand.HELP_PAGE, DeleteCommand.HELP_PAGE, UndoCommand.HELP_PAGE, ExportCommand.HELP_PAGE,
            ClearCommand.HELP_PAGE, HelpCommand.HELP_PAGE, ExitCommand.HELP_PAGE);
    private static final Map<String, String> aliases = Map.of(
            ListCommand.COMMAND_ALIAS, ListCommand.COMMAND_WORD,
            HelpCommand.COMMAND_ALIAS, HelpCommand.COMMAND_WORD);
    private static final HelpPage overview = new HelpPage("Available commands",
            pages.stream().map(page -> displayName(page) + " — " + page.text().lines().findFirst().orElseThrow())
                    .collect(Collectors.joining("\n"))
                    + "\n\nUse help COMMAND or ? COMMAND for usage, options and examples."
                    + "\nINDEX and CONTACT_ID refer to one-based positions in the displayed list."
                    + "\nCommand names are case-insensitive. Press q to close this window.");

    private HelpCatalog() {
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
        String canonicalName = aliases.getOrDefault(name, name);
        return pages.stream().filter(page -> page.title().equals(canonicalName)).findFirst();
    }

    private static String displayName(HelpPage page) {
        return switch (page.title()) {
            case ListCommand.COMMAND_WORD -> page.title() + " | " + ListCommand.COMMAND_ALIAS;
            case HelpCommand.COMMAND_WORD -> page.title() + " | " + HelpCommand.COMMAND_ALIAS;
            default -> page.title();
        };
    }
}
