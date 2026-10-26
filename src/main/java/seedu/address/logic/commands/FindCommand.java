package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.help.HelpPage;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.TagsMatchPredicate;

/**
 * Finds persons by case-insensitive name keywords or case-insensitive tags.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    /** Detailed usage, supported options and examples for this command. */
    public static final HelpPage HELP_PAGE = new HelpPage(COMMAND_WORD, String.join("\n",
            "Finds contacts by name keywords or tags.",
            "Usage: find KEYWORD [MORE_KEYWORDS ...]",
            "       find --tag TAG [TAG ...] [--tag TAG ...]",
            "",
            "Options:",
            "  --tag  Match any specified whole tag; matching is case-insensitive.",
            "         This command has no short tag option.",
            "         Tag input follows add/edit rules; quote a tag that looks like an option.",
            "",
            "Name searches match any keyword, ignoring case.",
            "Tag and name searches cannot be combined. Results replace the displayed list.",
            "",
            "Examples:",
            "  find alice bob",
            "  find --tag client --tag important"));

    public static final String MESSAGE_NO_TAG_MATCHES = "No contacts found with the specified tag(s).";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds all persons whose names contain any of "
            + "the specified keywords (case-insensitive) and displays them as a list with index numbers.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]...\n"
            + "Example: " + COMMAND_WORD + " alice bob charlie\n"
            + "Or find contacts matching any tag (case-insensitive): "
            + "find --tag TAG [TAG ...] [--tag TAG ...]\n"
            + "Example: find --tag client --tag important";

    private final Predicate<Person> predicate;

    public FindCommand(Predicate<Person> predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        if (predicate instanceof TagsMatchPredicate && model.getFilteredPersonList().isEmpty()) {
            return new CommandResult(MESSAGE_NO_TAG_MATCHES);
        }
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
