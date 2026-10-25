package seedu.address.ui;

import javafx.event.Event;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Adds vim-style j/k movement to lists, for users who keep their hands on the home row.
 */
final class VimKeys {

    private VimKeys() {
    }

    /**
     * Maps unmodified j/k to Down/Up, so the list keeps its native selection and scrolling behaviour.
     */
    static void install(ListView<?> listView) {
        listView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isAltDown() || event.isControlDown() || event.isMetaDown() || event.isShiftDown()
                    || (event.getCode() != KeyCode.J && event.getCode() != KeyCode.K)) {
                return;
            }
            KeyCode arrow = event.getCode() == KeyCode.J ? KeyCode.DOWN : KeyCode.UP;
            event.consume();
            Event.fireEvent(listView, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", arrow,
                    false, false, false, false));
        });
    }
}
