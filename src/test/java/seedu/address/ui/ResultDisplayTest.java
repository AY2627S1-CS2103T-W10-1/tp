package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.TextArea;

public class ResultDisplayTest {
    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void setFeedback_wrapsReadOnlyTextAndResetsSelectionAndScrolling() throws Exception {
        FxTestUtil.run(() -> {
            ResultDisplay display = new ResultDisplay();
            TextArea text = (TextArea) display.getRoot().lookup("#resultDisplay");
            display.setFeedbackToUser("Long feedback\n".repeat(30));
            text.positionCaret(text.getLength());
            text.setScrollTop(100);
            text.setScrollLeft(100);
            display.setFeedbackToUser("Error\n\nExamples:\n  help");
            assertEquals("Error\n\nExamples:\n  help", text.getText());
            assertEquals(0, text.getCaretPosition());
            assertEquals(0, text.getScrollTop());
            assertEquals(0, text.getScrollLeft());
            assertTrue(text.isWrapText());
            assertFalse(text.isEditable());
            assertThrows(NullPointerException.class, () -> display.setFeedbackToUser(null));
            assertEquals("Error\n\nExamples:\n  help", text.getText());
        });
    }

    @Test
    public void setFeedback_growsWithLinesUpToLimit() throws Exception {
        FxTestUtil.run(() -> {
            ResultDisplay display = new ResultDisplay();
            TextArea text = (TextArea) display.getRoot().lookup("#resultDisplay");
            assertEquals(1, text.getPrefRowCount());
            display.setFeedbackToUser("Error\n\nExamples:\n  help");
            assertEquals(4, text.getPrefRowCount());
            display.setFeedbackToUser("Long feedback\n".repeat(30));
            assertEquals(ResultDisplay.MAX_VISIBLE_ROWS, text.getPrefRowCount());
            display.setFeedbackToUser("");
            assertEquals(1, text.getPrefRowCount());
        });
    }

    @Test
    public void setErrorFeedback_marksErrorUntilNextSuccess() throws Exception {
        FxTestUtil.run(() -> {
            ResultDisplay display = new ResultDisplay();
            TextArea text = (TextArea) display.getRoot().lookup("#resultDisplay");
            display.setErrorFeedback("Unknown command");
            display.setErrorFeedback("Unknown command");
            assertEquals("Unknown command", text.getText());
            assertEquals(1L, text.getStyleClass().stream().filter(ResultDisplay.ERROR_STYLE_CLASS::equals).count());
            display.setFeedbackToUser("Listed all persons");
            assertFalse(text.getStyleClass().contains(ResultDisplay.ERROR_STYLE_CLASS));
            assertThrows(NullPointerException.class, () -> display.setErrorFeedback(null));
        });
    }
}
