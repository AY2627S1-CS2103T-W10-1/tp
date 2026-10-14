package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.help.HelpCatalog;

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
            assertEquals(HelpCatalog.getOverview().text(), text.getText());
            assertFalse(text.isEditable());
            assertTrue(text.isWrapText());
            assertTrue(window.isShowing());

            text.fireEvent(keyPress(KeyCode.A, false, false, false, false));
            text.fireEvent(keyPress(KeyCode.Q, false, false, true, false));
            text.fireEvent(keyPress(KeyCode.Q, false, true, false, false));
            text.fireEvent(keyPress(KeyCode.Q, false, false, false, true));
            text.fireEvent(keyPress(KeyCode.Q, true, false, false, false));
            assertTrue(window.isShowing());

            text.fireEvent(keyPress(KeyCode.Q, false, false, false, false));
            assertFalse(window.isShowing());

            window.show();
            assertTrue(window.isShowing());
        });
    }

    @Test
    public void showPage_updatesHeadingResetsScrollingAndRestoresWindow() throws Exception {
        FxTestUtil.run(() -> {
            HelpWindow window = new HelpWindow();
            window.showPage(AddCommand.HELP_PAGE);
            TextArea text = (TextArea) window.getRoot().getScene().lookup("#helpText");
            Label heading =
                    (Label) window.getRoot().getScene().lookup("#helpHeading");
            assertEquals("add", heading.getText());
            assertEquals(AddCommand.HELP_PAGE.text(), text.getText());
            text.positionCaret(text.getLength());
            text.setScrollTop(100);
            window.getRoot().setIconified(true);
            awaitIconification(window.getRoot(), true);

            window.showPage(SortCommand.HELP_PAGE);
            assertEquals("sort", heading.getText());
            assertEquals(SortCommand.HELP_PAGE.text(), text.getText());
            assertEquals(0, text.getCaretPosition());
            assertEquals(0, text.getScrollTop());
            awaitIconification(window.getRoot(), false);
            assertFalse(window.getRoot().isIconified());
            assertTrue(window.isShowing());

            window.showPage(HelpCatalog.getOverview());
            assertEquals("Available commands", heading.getText());
            assertEquals(HelpCatalog.getOverview().text(), text.getText());
        });
    }

    private static void awaitIconification(Stage stage, boolean iconified) {
        // Iconification is acknowledged asynchronously by the window manager.
        Object loopKey = new Object();
        Timeline poll = new Timeline();
        poll.getKeyFrames().add(new KeyFrame(Duration.millis(50), event -> {
            if (stage.isIconified() == iconified) {
                poll.stop();
                Platform.exitNestedEventLoop(loopKey, null);
            }
        }));
        poll.setCycleCount(60);
        poll.setOnFinished(event -> Platform.exitNestedEventLoop(loopKey, null));
        poll.play();
        Platform.enterNestedEventLoop(loopKey);
    }

    private static KeyEvent keyPress(KeyCode code, boolean shiftDown, boolean controlDown,
            boolean altDown, boolean metaDown) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, code.getChar(), code.getChar(), code,
                shiftDown, controlDown, altDown, metaDown);
    }
}
