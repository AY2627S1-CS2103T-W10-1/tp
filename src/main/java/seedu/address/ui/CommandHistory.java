package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Session-only command history with bounded navigation and a saved draft after the newest entry.
 * Editing a recalled entry never changes the recorded commands.
 */
final class CommandHistory {
    private final List<String> commands = new ArrayList<>();
    private int position;
    private String draft = "";

    /**
     * Records exact, nonblank input, including duplicates, and resets navigation after every submission.
     *
     * @param command Submitted input, regardless of whether execution succeeds.
     * @throws NullPointerException If {@code command} is null.
     */
    void record(String command) {
        requireNonNull(command);
        if (!command.isBlank()) {
            commands.add(command);
        }
        position = commands.size();
        draft = "";
    }

    /**
     * Recalls the previous command, saving current input when leaving the draft position.
     *
     * @param currentInput Current command-field text.
     * @return The older entry, or empty at the oldest entry or when history is empty.
     * @throws NullPointerException If {@code currentInput} is null.
     */
    Optional<String> previous(String currentInput) {
        requireNonNull(currentInput);
        if (position == 0) {
            return Optional.empty();
        }
        if (position == commands.size()) {
            draft = currentInput;
        }
        position--;
        return Optional.of(commands.get(position));
    }

    /**
     * Recalls the next command, restoring the saved draft after the newest entry.
     *
     * @return The newer entry or saved draft, or empty when already at the draft position.
     */
    Optional<String> next() {
        if (position == commands.size()) {
            return Optional.empty();
        }
        position++;
        return Optional.of(position == commands.size() ? draft : commands.get(position));
    }
}
