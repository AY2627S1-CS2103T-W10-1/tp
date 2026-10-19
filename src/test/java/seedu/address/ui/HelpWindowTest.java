package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

            ScrollPane scroll = (ScrollPane) window.getRoot().getScene().lookup("#helpScroll");
            HelpContentView content = (HelpContentView) scroll.getContent();
            assertEquals(HelpCatalog.getOverview().text(), content.getAccessibleText());
            assertTrue(scroll.isFitToWidth());
            assertEquals(13, content.lookupAll(".help-command-row").size());
            assertTrue(window.isShowing());

            scroll.fireEvent(keyPress(KeyCode.A, false, false, false, false));
            scroll.fireEvent(keyPress(KeyCode.Q, false, false, true, false));
            scroll.fireEvent(keyPress(KeyCode.Q, false, true, false, false));
            scroll.fireEvent(keyPress(KeyCode.Q, false, false, false, true));
            scroll.fireEvent(keyPress(KeyCode.Q, true, false, false, false));
            assertTrue(window.isShowing());

            scroll.fireEvent(keyPress(KeyCode.Q, false, false, false, false));
            assertFalse(window.isShowing());

            window.show();
            assertTrue(window.isShowing());
        });
    }

    @Test
    public void showPage_updatesHeadingAndResetsScrolling() throws Exception {
        FxTestUtil.run(() -> {
            HelpWindow window = new HelpWindow();
            window.showPage(AddCommand.HELP_PAGE);
            ScrollPane scroll = (ScrollPane) window.getRoot().getScene().lookup("#helpScroll");
            HelpContentView content = (HelpContentView) scroll.getContent();
            Label heading =
                    (Label) window.getRoot().getScene().lookup("#helpHeading");
            assertEquals("add", heading.getText());
            assertEquals(AddCommand.HELP_PAGE.text(), content.getAccessibleText());
            window.getRoot().getScene().getRoot().applyCss();
            window.getRoot().getScene().getRoot().layout();
            scroll.fireEvent(keyPress(KeyCode.PAGE_DOWN, false, false, false, false));
            assertTrue(scroll.getVvalue() > 0);
            scroll.setVvalue(1);
            scroll.setHvalue(1);

            window.showPage(SortCommand.HELP_PAGE);
            assertEquals("sort", heading.getText());
            assertEquals(SortCommand.HELP_PAGE.text(), content.getAccessibleText());
            assertEquals(0, scroll.getVvalue());
            assertEquals(0, scroll.getHvalue());
            assertTrue(window.isShowing());

            window.showPage(HelpCatalog.getOverview());
            assertEquals("Available commands", heading.getText());
            assertEquals(HelpCatalog.getOverview().text(), content.getAccessibleText());
        });
    }

    @Test
    public void q_closesWindowWhenCopyButtonHasFocus() throws Exception {
        FxTestUtil.run(() -> {
            HelpWindow window = new HelpWindow();
            window.showPage(AddCommand.HELP_PAGE);
            Button copy = (Button) window.getRoot().getScene().lookup(".help-copy");
            copy.requestFocus();
            copy.fireEvent(keyPress(KeyCode.Q, false, false, false, false));
            assertFalse(window.isShowing());
        });
    }

    private static KeyEvent keyPress(KeyCode code, boolean shiftDown, boolean controlDown,
            boolean altDown, boolean metaDown) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, code.getChar(), code.getChar(), code,
                shiftDown, controlDown, altDown, metaDown);
    }
}
