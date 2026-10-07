package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a contact; only the name is required.\n"
            + "Parameters: --name NAME [--phone PHONE] [--email EMAIL] [--department DEPARTMENT] "
            + "[--address ADDRESS] [--tag TAG ...]\n"
            + "Example: add --name \"Alice Tan\" --email alice@example.com\n"
            + "Existing syntax: add n/NAME [p/PHONE] [e/EMAIL] [d/DEPARTMENT] [a/ADDRESS] [t/TAG]...";

    public static final String MESSAGE_NAME_REQUIRED = "Name cannot be empty.";
    public static final String MESSAGE_INVALID_PHONE = "Invalid phone number: Must contain 7-15 digits.";
    public static final String MESSAGE_INVALID_TAG = "Invalid tag: Must contain 1-30 characters.";
    public static final String MESSAGE_SUCCESS = "Contact added successfully: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasPerson(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        model.addPerson(toAdd);
        String tags = toAdd.getTags().stream().map(tag -> tag.tagName).sorted().collect(Collectors.joining(", "));
        String details = toAdd.getName() + " | Phone: " + toAdd.getPhone() + " | Email: " + toAdd.getEmail()
                + " | Department: " + toAdd.getDepartment().map(value -> value.value).orElse("Not provided")
                + " | Address: " + toAdd.getAddress() + " | Tags: " + (tags.isEmpty() ? "None" : tags);
        return new CommandResult(String.format(MESSAGE_SUCCESS, details));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
