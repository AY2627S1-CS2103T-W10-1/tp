package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seedu.address.logic.commands.HelpCommand;

public class HelpWindowTest {

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void show_displaysScrollableCommandListAndQClosesWindow() throws Exception {
        FxTestUtil.run(() -> {
            HelpWindow window = new HelpWindow();
            window.show();

            TextArea text = (TextArea) window.getRoot().getScene().lookup("#helpText");
            assertEquals(HelpCommand.HELP_TEXT, text.getText());
            assertFalse(text.isEditable());
            assertTrue(text.isWrapText());
            assertTrue(window.isShowing());

            text.fireEvent(keyPress(true));
            assertTrue(window.isShowing());

            text.fireEvent(keyPress(false));
            assertFalse(window.isShowing());

            window.show();
            assertTrue(window.isShowing());
        });
    }

    private static KeyEvent keyPress(boolean controlDown) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "q", "q", KeyCode.Q,
                false, controlDown, false, false);
    }
}
