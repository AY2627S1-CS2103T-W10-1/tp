package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.logic.help.HelpPage;

/**
 * Adds command examples to displayed errors without changing the underlying exception or executing commands.
 */
public final class CommandErrorFormatter {
    private static final int MAX_EXAMPLES = 2;

    private CommandErrorFormatter() {
    }

    /**
     * Preserves the error and appends up to two documented examples for the input's command word.
     * Command names and aliases are resolved case-insensitively through the help catalog.
     * Unknown commands use {@code help}; errors already containing an examples section are unchanged.
     *
     * @param commandText Original command input.
     * @param message Original parsing, execution or storage error.
     * @return Feedback containing the original error followed by examples, when needed.
     * @throws NullPointerException If either argument is null.
     */
    public static String format(String commandText, String message) {
        requireNonNull(commandText);
        requireNonNull(message);
        boolean containsExamples = message.lines().map(String::stripLeading)
                .anyMatch(line -> line.startsWith("Example:") || line.startsWith("Examples:"));
        if (containsExamples) {
            return message;
        }

        String commandWord = commandText.trim().split("\\s+", 2)[0];
        List<String> examples = HelpCatalog.findPage(commandWord).map(HelpPage::examples)
                .filter(commands -> !commands.isEmpty()).orElse(List.of(HelpCommand.COMMAND_WORD));
        return message + "\n\nExamples:\n" + examples.stream().limit(MAX_EXAMPLES)
                .map(example -> "  " + example).collect(Collectors.joining("\n"));
    }
}
