package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.help.HelpCatalog;

public class CommandErrorFormatterTest {
    @Test
    public void format_knownCommand_keepsErrorFirstAndLimitsExamples() {
        for (var page : HelpCatalog.getPages()) {
            String formatted = CommandErrorFormatter.format(page.title() + " invalid", "Original error.");
            List<String> expected = page.examples().stream().limit(2).toList();
            assertEquals("Original error.\n\nExamples:\n  " + String.join("\n  ", expected), formatted);
        }
    }

    @Test
    public void format_capitalizationWhitespaceAndAliases_resolveCanonicalCommand() {
        for (var page : HelpCatalog.getPages()) {
            assertEquals(CommandErrorFormatter.format(page.title(), "Error"),
                    CommandErrorFormatter.format("  " + page.title().toUpperCase(Locale.ROOT) + "\t invalid  ",
                            "Error"));
        }
        assertEquals(CommandErrorFormatter.format("list", "Error"), CommandErrorFormatter.format("ls 3", "Error"));
        assertEquals(CommandErrorFormatter.format("help", "Error"), CommandErrorFormatter.format("? missing", "Error"));
    }

    @Test
    public void format_unknownOrBlankCommand_suggestsHelpWithoutGuessing() {
        for (String input : List.of("", "  \t ", "ad --name Alice", "unknown", "--name Alice")) {
            assertEquals("Unknown command.\n\nExamples:\n  help",
                    CommandErrorFormatter.format(input, "Unknown command."));
        }
    }

    @Test
    public void format_existingExamples_preservesOriginalMessage() {
        for (String message : List.of(AddCommand.MESSAGE_USAGE, FindCommand.MESSAGE_USAGE,
                "Error\nExamples:\n  add --name Alice", "Error\n  Example: list", "Error\nExample:ls")) {
            assertEquals(message, CommandErrorFormatter.format("add invalid", message));
        }
        String once = CommandErrorFormatter.format("add", "Error");
        assertEquals(once, CommandErrorFormatter.format("add", once));
    }

    @Test
    public void format_exampleWordWithinExplanation_doesNotSuppressExamples() {
        String error = "Invalid name. For Example: provide letters.";
        assertTrue(CommandErrorFormatter.format("add", error).startsWith(error + "\n\nExamples:\n"));
    }

    @Test
    public void format_nullInputOrMessage_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> CommandErrorFormatter.format(null, "Error"));
        assertThrows(NullPointerException.class, () -> CommandErrorFormatter.format("add", null));
        assertThrows(NullPointerException.class, () -> CommandErrorFormatter.format(null, "Example: help"));
    }
}
