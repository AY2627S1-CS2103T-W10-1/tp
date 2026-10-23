package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.help.HelpPage;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Displays the full details of a contact identified by its index in the displayed list.
 */
public class ViewCommand extends Command {

    public static final String COMMAND_WORD = "view";

    /** Detailed usage, supported options and examples for this command. */
    public static final HelpPage HELP_PAGE = new HelpPage(COMMAND_WORD, String.join("\n",
            "Shows a contact's full details in a separate window.",
            "Usage: view INDEX",
            "",
            "INDEX is a positive, one-based position in the currently displayed list.",
            "Preserves the active filter and display order.",
            "",
            "Examples:",
            "  view 2"));

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows the full details of the contact identified by CONTACT_ID in the displayed list.\n"
            + "Parameters: CONTACT_ID (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";
    public static final String MESSAGE_SUCCESS = "Showing details of contact %1$d: %2$s";
    public static final String MESSAGE_EMPTY_LIST = "There is no contact to view in the currently displayed list.";
    public static final String MESSAGE_INVALID_CONTACT_ID =
            "Error: Please enter a Contact ID in the range 1-%1$d.";

    private final Index targetIndex;

    public ViewCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedPersons = model.getFilteredPersonList();
        if (displayedPersons.isEmpty()) {
            throw new CommandException(MESSAGE_EMPTY_LIST);
        }
        if (targetIndex.getZeroBased() >= displayedPersons.size()) {
            throw new CommandException(String.format(MESSAGE_INVALID_CONTACT_ID, displayedPersons.size()));
        }

        Person person = displayedPersons.get(targetIndex.getZeroBased());
        String feedback = String.format(MESSAGE_SUCCESS, targetIndex.getOneBased(), person.getName());
        return new CommandResult(feedback, person, formatDetails(person));
    }

    /**
     * Formats every currently supported contact field without truncation, in two groups separated by blank lines:
     * how to reach the contact (name, phone, email, address), then how they are organised (department, tags).
     */
    private String formatDetails(Person person) {
        String tags = person.getTags().stream()
                .map(tag -> tag.tagName)
                .sorted()
                .collect(Collectors.joining(", "));
        return "Name: " + person.getName() + "\n"
                + "Phone: " + person.getPhone() + "\n"
                + "Email: " + person.getEmail() + "\n"
                + "Address: " + person.getAddress() + "\n"
                + "\n\n"
                + "Department: " + person.getDepartment().map(department -> department.value)
                        .orElse("Not provided") + "\n"
                + "Tags: " + (tags.isEmpty() ? "None" : tags);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ViewCommand otherViewCommand)) {
            return false;
        }
        return targetIndex.equals(otherViewCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
