package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.help.HelpPage;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class CommandResultTest {
    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());
    }

    @Test
    public void detailsResult_preservesFlagsAndEquality() {
        Person amy = new PersonBuilder().build();
        CommandResult details = new CommandResult("feedback", amy, "details");
        assertTrue(details.isShowDetails());
        assertFalse(details.isShowHelp());
        assertFalse(details.isExit());
        assertFalse(new CommandResult("details").isShowDetails());
        assertEquals(details, new CommandResult("feedback", new PersonBuilder().build(), "details"));
        assertEquals(details.hashCode(), new CommandResult("feedback", amy, "details").hashCode());
        assertEquals("feedback", details.getFeedbackToUser());
        assertEquals("details", details.getContactDetails().orElseThrow());
        assertEquals(amy, details.getViewedPerson().orElseThrow());
        assertTrue(new CommandResult("feedback").getContactDetails().isEmpty());
        assertTrue(new CommandResult("feedback").getViewedPerson().isEmpty());
        assertNotEquals(details, new CommandResult("feedback"));
        assertNotEquals(details, new CommandResult("feedback", amy, "different details"));
        assertNotEquals(details.hashCode(), new CommandResult("feedback", amy, "different details").hashCode());
        Person bob = new PersonBuilder().withName("Bob Choo").build();
        assertNotEquals(details, new CommandResult("feedback", bob, "details"));
    }

    @Test
    public void detailsResult_nullArgument_throwsNullPointerException() {
        Person amy = new PersonBuilder().build();
        assertThrows(NullPointerException.class, () -> new CommandResult(null, amy, "details"));
        assertThrows(NullPointerException.class, () -> new CommandResult("feedback", null, "details"));
        assertThrows(NullPointerException.class, () -> new CommandResult("feedback", amy, null));
    }

    @Test
    public void helpResult_preservesPayloadFlagsAndEquality() {
        HelpPage page = new HelpPage("depart", "manual");
        CommandResult result = CommandResult.forHelp("feedback", page);
        CommandResult equal = CommandResult.forHelp("feedback", new HelpPage("depart", "manual"));
        assertEquals(page, result.getHelpPage().orElseThrow());
        assertTrue(result.isShowHelp());
        assertFalse(result.isExit());
        assertFalse(result.isShowDetails());
        assertEquals(result, equal);
        assertEquals(result.hashCode(), equal.hashCode());
        assertNotEquals(result, new CommandResult("feedback", true, false));
        assertNotEquals(result, CommandResult.forHelp("feedback", new HelpPage("sort", "manual")));
        assertNotEquals(result, CommandResult.forHelp("feedback", new HelpPage("depart", "different")));
        assertTrue(result.toString().contains("helpPage=" + page));
        assertTrue(new CommandResult("feedback", true, false).getHelpPage().isEmpty());
        assertTrue(new CommandResult("feedback").getHelpPage().isEmpty());
    }

    @Test
    public void helpResult_nullFeedbackOrPage_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> CommandResult.forHelp(null, DepartCommand.HELP_PAGE));
        assertThrows(NullPointerException.class, () -> CommandResult.forHelp("feedback", null));
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + ", showDetails=" + commandResult.isShowDetails()
                + ", contactDetails=" + commandResult.getContactDetails().orElse(null)
                + ", helpPage=" + commandResult.getHelpPage().orElse(null) + "}";
        assertEquals(expected, commandResult.toString());
    }
}
