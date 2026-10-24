package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class VimKeysTest {

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void install_jAndKMoveSelection_unlessModified() throws Exception {
        FxTestUtil.run(() -> {
            ListView<String> list = new ListView<>(FXCollections.observableArrayList("first", "second", "third"));
            VimKeys.install(list);
            Stage stage = new Stage();
            stage.setScene(new Scene(list, 200, 200));
            stage.show();
            list.requestFocus();
            list.getSelectionModel().select(1);

            press(list, KeyCode.K, false, false, false, false);
            assertEquals(0, list.getSelectionModel().getSelectedIndex());
            press(list, KeyCode.J, false, false, false, false);
            assertEquals(1, list.getSelectionModel().getSelectedIndex());

            // Shift, Ctrl, Alt or Meta with j is not a vim key.
            for (int modifier = 0; modifier < 4; modifier++) {
                press(list, KeyCode.J, modifier == 0, modifier == 1, modifier == 2, modifier == 3);
                assertEquals(1, list.getSelectionModel().getSelectedIndex());
            }
        });
    }

    private static void press(ListView<?> list, KeyCode code, boolean shift, boolean control, boolean alt,
                              boolean meta) {
        Event.fireEvent(list, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, shift, control, alt, meta));
    }
}
