package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

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
        CommandResult details = new CommandResult("feedback", "details");
        assertTrue(details.isShowDetails());
        assertFalse(details.isShowHelp());
        assertFalse(details.isExit());
        assertFalse(new CommandResult("details").isShowDetails());
        assertEquals(details, new CommandResult("feedback", "details"));
        assertEquals(details.hashCode(), new CommandResult("feedback", "details").hashCode());
        assertEquals("feedback", details.getFeedbackToUser());
        assertEquals("details", details.getContactDetails().orElseThrow());
        assertTrue(new CommandResult("feedback").getContactDetails().isEmpty());
        assertNotEquals(details, new CommandResult("feedback"));
        assertNotEquals(details, new CommandResult("feedback", "different details"));
        assertNotEquals(details.hashCode(), new CommandResult("feedback", "different details").hashCode());
    }

    @Test
    public void detailsResult_nullFeedbackOrDetails_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new CommandResult(null, "details"));
        assertThrows(NullPointerException.class, () -> new CommandResult("feedback", null));
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + ", showDetails=" + commandResult.isShowDetails()
                + ", contactDetails=" + commandResult.getContactDetails().orElse(null) + "}";
        assertEquals(expected, commandResult.toString());
    }
}
