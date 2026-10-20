package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.stream.Collectors;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.help.HelpPage;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    /** Detailed usage, supported options and examples for this command. */
    public static final HelpPage HELP_PAGE = new HelpPage(COMMAND_WORD, String.join("\n",
            "Adds a contact; only the name is required.",
            "Usage: add --name NAME [--phone PHONE] [--email EMAIL]",
            "       [--address ADDRESS] [--department DEPARTMENT] [--tag TAG ...]",
            "",
            "Options:",
            "  -n, --name        Required contact name.",
            "  -p, --phone       Optional phone number (7-15 digits).",
            "  -e, --email       Optional valid email address.",
            "  -a, --address     Optional postal address.",
            "  -d, --department  Optional department (2-50 characters).",
            "  -t, --tag         Optional tags; repeat the option or supply several tags.",
            "",
            "Quote values containing spaces. Single-valued options cannot be repeated.",
            "Contacts may share a name; duplicate name, phone and email combinations are rejected.",
            "",
            "Examples:",
            "  add --name \"Alice Tan\"",
            "  add -n \"Alice Tan\" -e alice@example.com -d Engineering -t client important"));

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a contact; only the name is required.\n"
            + "Parameters: --name NAME [--phone PHONE] [--email EMAIL] [--department DEPARTMENT] "
            + "[--address ADDRESS] [--tag TAG ...]\n"
            + "Example: add --name \"Alice Tan\" --email alice@example.com\n"
            + "Short flags: add -n NAME [-p PHONE] [-e EMAIL] [-d DEPARTMENT] [-a ADDRESS] [-t TAG]...";

    public static final String MESSAGE_NAME_REQUIRED = "Name cannot be empty.";
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
