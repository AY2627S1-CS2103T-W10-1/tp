package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ContactDetailsWindowTest {

    private final Person alice = new PersonBuilder().withName("Alice Pauline").withPhone("94351253").build();

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void showDetails_showsFieldGridTitleAndOwner() throws Exception {
        FxTestUtil.run(() -> {
            Stage owner = new Stage();
            ContactDetailsWindow window = new ContactDetailsWindow(owner);
            String longAddress = "Long address ".repeat(100).strip();
            String details = "Name: Alice\nPhone: 12345678\nEmail: alice@example.com\n"
                    + "Address: " + longAddress + "\n\n\nTags: client, vendor";

            window.showDetails(alice, details);

            GridPane grid = grid(window);
            assertEquals(details, grid.getAccessibleText());
            List<TextArea> values = grid.getChildren().stream()
                    .filter(node -> node.getStyleClass().contains(DetailsPanel.VALUE_STYLE_CLASS))
                    .map(TextArea.class::cast).toList();
            assertEquals(List.of("Alice", "12345678", "alice@example.com", longAddress, "client, vendor"),
                    values.stream().map(TextArea::getText).toList());
            values.forEach(value -> {
                assertTrue(value.isWrapText());
                assertFalse(value.isEditable());
            });
            assertEquals(owner, window.getRoot().getOwner());
            assertEquals(ContactDetailsWindow.TITLE_PREFIX + "Alice Pauline", window.getRoot().getTitle());
            assertTrue(window.getRoot().isShowing());
        });
    }

    @Test
    public void focus_selectsFirstValueForCopying() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindow window = new ContactDetailsWindow(new Stage());
            window.showDetails(alice, "Name: Alice Pauline\nPhone: 94351253");

            window.focus();

            TextArea focused = (TextArea) window.getRoot().getScene().getFocusOwner();
            assertTrue(focused.getStyleClass().contains(DetailsPanel.VALUE_STYLE_CLASS));
            assertEquals("Alice Pauline", focused.getSelectedText());
        });
    }

    @Test
    public void pressJ_afterViewLeavesFocusOnPanel_movesToFirstValue() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindow window = new ContactDetailsWindow(new Stage());
            window.showDetails(alice, "Name: Alice Pauline\nPhone: 94351253");
            // Activating a popup opened by view focuses its panel rather than a value.
            ScrollPane panel = (ScrollPane) window.getRoot().getScene().lookup("#detailsScroll");
            panel.requestFocus();

            Event.fireEvent(panel, new KeyEvent(KeyEvent.KEY_PRESSED, "j", "j", KeyCode.J,
                    false, false, false, false));

            TextArea focused = (TextArea) window.getRoot().getScene().getFocusOwner();
            assertEquals("Alice Pauline", focused.getSelectedText());
        });
    }

    @Test
    public void showsContact_sameContactWhileShowing_returnsTrue() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindow window = new ContactDetailsWindow(new Stage());
            assertFalse(window.showsContact(alice));

            window.showDetails(alice, "Alice");
            Person editedAlice = new PersonBuilder(alice).withDepartment("Finance").build();
            assertTrue(window.showsContact(editedAlice));
            assertFalse(window.showsContact(new PersonBuilder().withName("Benson Meier").build()));

            window.hide();
            assertFalse(window.showsContact(alice));
        });
    }

    @Test
    public void pressQ_closesWindow() throws Exception {
        FxTestUtil.run(() -> {
            Stage owner = new Stage();
            owner.show();
            ContactDetailsWindow window = new ContactDetailsWindow(owner);
            window.showDetails(alice, "Name: Alice Pauline");
            window.focus();
            Node value = window.getRoot().getScene().getFocusOwner();

            // Shift, Ctrl, Alt or Meta with q is left alone.
            for (int modifier = 0; modifier < 4; modifier++) {
                Event.fireEvent(value, new KeyEvent(KeyEvent.KEY_PRESSED, "q", "q", KeyCode.Q,
                        modifier == 0, modifier == 1, modifier == 2, modifier == 3));
                assertTrue(window.getRoot().isShowing());
            }

            Event.fireEvent(value, new KeyEvent(KeyEvent.KEY_PRESSED, "q", "q", KeyCode.Q,
                    false, false, false, false));
            assertFalse(window.getRoot().isShowing());
        });
    }

    @Test
    public void showDetails_nullArgument_throwsNullPointerException() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindow window = new ContactDetailsWindow(new Stage());
            assertThrows(NullPointerException.class, () -> window.showDetails(null, "details"));
            assertThrows(NullPointerException.class, () -> window.showDetails(alice, null));
            assertFalse(window.getRoot().isShowing());
        });
    }

    /** Returns the popup's field grid, whose accessible text is the details snapshot. */
    private static GridPane grid(ContactDetailsWindow window) {
        return (GridPane) ((ScrollPane) window.getRoot().getScene().lookup("#detailsScroll")).getContent();
    }
}
