package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ContactLoadStatus;
import seedu.address.model.Model;
import seedu.address.model.SortField;
import seedu.address.model.person.Person;

/**
 * Sorts the currently displayed contacts in ascending order without changing stored data.
 */
public class SortCommand extends Command {
    public static final String COMMAND_WORD = "sort";
    public static final String MESSAGE_USAGE = "sort -b/--by <department|dept|tag|tags>";
    public static final String MESSAGE_EMPTY = "No contacts found to sort.";
    public static final String MESSAGE_LOAD_FAILURE =
            "Unable to load contacts for sorting. Please restart the application or check the data file.";
    public static final String MESSAGE_INVALID_DATA =
            "Unable to sort contacts because some stored contact data is invalid.";

    private final SortField field;

    /**
     * Creates a command that sorts by the specified field.
     */
    public SortCommand(SortField field) {
        this.field = requireNonNull(field);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (model.getContactLoadStatus() == ContactLoadStatus.UNREADABLE) {
            throw new CommandException(MESSAGE_LOAD_FAILURE);
        }
        if (model.getContactLoadStatus() == ContactLoadStatus.INVALID) {
            throw new CommandException(MESSAGE_INVALID_DATA);
        }
        if (model.getFilteredPersonList().isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY);
        }

        try {
            model.sortFilteredPersonList(field);
        } catch (IllegalArgumentException e) {
            throw new CommandException(MESSAGE_INVALID_DATA, e);
        }
        return new CommandResult(formatContacts(model.getFilteredPersonList()));
    }

    private String formatContacts(List<Person> persons) {
        StringBuilder output = new StringBuilder("Contacts sorted by " + field.getDisplayName() + ":");
        for (int i = 0; i < persons.size(); i++) {
            Person person = persons.get(i);
            output.append("\n").append(i + 1).append(". ").append(person.getName())
                    .append(" | Department: ")
                    .append(person.getDepartment().map(department -> department.value).orElse("Not assigned"))
                    .append(" | Tags: ").append(formatTags(person));
        }
        return output.toString();
    }

    private String formatTags(Person person) {
        String tags = person.getTags().stream().map(tag -> tag.tagName)
                .sorted(String.CASE_INSENSITIVE_ORDER.thenComparing(String::compareTo))
                .collect(Collectors.joining(", "));
        return tags.isEmpty() ? "None" : tags;
    }

    @Override
    public boolean requiresStorageSave() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof SortCommand otherCommand && field == otherCommand.field);
    }
}
