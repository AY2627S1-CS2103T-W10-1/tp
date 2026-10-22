package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {
    private final CommandHistory history = new CommandHistory();

    @Test
    public void navigate_emptyHistory_hasNoReplacement() {
        assertTrue(history.previous("unfinished draft").isEmpty());
        assertTrue(history.next().isEmpty());
    }

    @Test
    public void navigate_visitsEntriesInOrderAndStopsAtBothEnds() {
        history.record("list");
        history.record("sort --by tags");
        assertTrue(history.next().isEmpty());
        assertEquals("sort --by tags", history.previous("").orElseThrow());
        assertEquals("list", history.previous("sort --by tags").orElseThrow());
        assertTrue(history.previous("list").isEmpty());
        assertEquals("sort --by tags", history.next().orElseThrow());
        assertEquals("", history.next().orElseThrow());
        assertTrue(history.next().isEmpty());
    }

    @Test
    public void navigate_restoresDraftAndPreservesRecordedTextDespiteEdits() {
        history.record("  add --name \"Alice Tan\"  ");
        history.record("list");
        String draft = "  add --name Bob --email ";
        assertEquals("list", history.previous(draft).orElseThrow());
        assertEquals("  add --name \"Alice Tan\"  ", history.previous("edited list").orElseThrow());
        assertEquals("list", history.next().orElseThrow());
        assertEquals(draft, history.next().orElseThrow());
        assertEquals("list", history.previous("new draft").orElseThrow());
        assertEquals("new draft", history.next().orElseThrow());
    }

    @Test
    public void record_preservesConsecutiveDuplicates() {
        history.record("list");
        history.record("list");
        assertEquals("list", history.previous("").orElseThrow());
        assertEquals("list", history.previous("list").orElseThrow());
        assertTrue(history.previous("list").isEmpty());
    }

    @Test
    public void record_newSubmissionResetsBrowsingWithoutRewritingOlderEntries() {
        history.record("list");
        history.record("sort --by tags");
        history.previous("draft");
        history.previous("sort --by tags");
        history.record("find Alice");
        assertTrue(history.next().isEmpty());
        assertEquals("find Alice", history.previous("").orElseThrow());
        assertEquals("sort --by tags", history.previous("find Alice").orElseThrow());
        assertEquals("list", history.previous("sort --by tags").orElseThrow());
    }

    @Test
    public void record_blankInputIsNotStoredAndResetsNavigation() {
        history.record("");
        history.record(" \t ");
        assertTrue(history.previous("draft").isEmpty());
        history.record("list");
        history.previous("old draft");
        history.record("  ");
        assertTrue(history.next().isEmpty());
        assertEquals("list", history.previous("new draft").orElseThrow());
        assertEquals("new draft", history.next().orElseThrow());
    }

    @Test
    public void history_isNotSharedAcrossCommandBoxesOrSessions() {
        history.record("list");
        assertTrue(new CommandHistory().previous("").isEmpty());
    }

    @Test
    public void nullInput_throwsWithoutChangingHistory() {
        history.record("list");
        assertThrows(NullPointerException.class, () -> history.record(null));
        assertThrows(NullPointerException.class, () -> history.previous(null));
        assertEquals("list", history.previous("").orElseThrow());
    }
}
