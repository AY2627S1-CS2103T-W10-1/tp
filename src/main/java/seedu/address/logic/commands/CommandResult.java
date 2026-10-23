package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.help.HelpPage;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /** Full contact details for the details pane, absent for other commands. */
    private final String contactDetails;

    /** Selected help content, absent for ordinary and legacy help results. */
    private final HelpPage helpPage;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, null, null);
    }

    /**
     * Constructs a result with separate main-window feedback and contact details.
     */
    public CommandResult(String feedbackToUser, String contactDetails) {
        this(feedbackToUser, false, false, requireNonNull(contactDetails), null);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, String contactDetails,
            HelpPage helpPage) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.contactDetails = contactDetails;
        this.helpPage = helpPage;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    /**
     * Creates a result that opens a specific help page without exiting or showing contact details.
     *
     * @param feedbackToUser Feedback for the main window.
     * @param page Help content for the separate window.
     * @return A help result containing the selected page.
     * @throws NullPointerException If either argument is null.
     */
    public static CommandResult forHelp(String feedbackToUser, HelpPage page) {
        return new CommandResult(feedbackToUser, true, false, null, requireNonNull(page));
    }

    /**
     * Returns the selected help page, absent for ordinary and legacy help results.
     */
    public Optional<HelpPage> getHelpPage() {
        return Optional.ofNullable(helpPage);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    public boolean isShowDetails() {
        return contactDetails != null;
    }

    public Optional<String> getContactDetails() {
        return Optional.ofNullable(contactDetails);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && Objects.equals(contactDetails, otherCommandResult.contactDetails)
                && Objects.equals(helpPage, otherCommandResult.helpPage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, contactDetails, helpPage);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("showDetails", isShowDetails())
                .add("contactDetails", contactDetails)
                .add("helpPage", helpPage)
                .toString();
    }

}
