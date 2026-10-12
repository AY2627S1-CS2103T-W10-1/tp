package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ContactDetailsWindowTest {

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void showDetails_loadsReadOnlyWrappingTextAndOwner() throws Exception {
        FxTestUtil.run(() -> {
            Stage owner = new Stage();
            ContactDetailsWindow window = new ContactDetailsWindow(owner);
            String details = "Name: Alice\nPhone: 12345678\nEmail: alice@example.com\n"
                    + "Address: " + "Long address ".repeat(100) + "\nTags: client, vendor";

            window.showDetails(details);

            TextArea text = (TextArea) window.getRoot().getScene().lookup("#contactDetails");
            assertEquals(details, text.getText());
            assertTrue(text.isWrapText());
            assertFalse(text.isEditable());
            assertEquals(owner, window.getRoot().getOwner());
            assertTrue(window.getRoot().isShowing());
            assertEquals(0, text.getCaretPosition());
        });
    }

    @Test
    public void showDetails_updatesExistingWindowAndReopensAfterHiding() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindow window = new ContactDetailsWindow(new Stage());
            Stage stage = window.getRoot();
            window.showDetails("First contact");
            window.hide();
            assertFalse(stage.isShowing());

            window.showDetails("Second contact");

            assertEquals(stage, window.getRoot());
            assertTrue(stage.isShowing());
            assertEquals("Second contact", ((TextArea) stage.getScene().lookup("#contactDetails")).getText());
        });
    }

    @Test
    public void showDetails_nullText_throwsNullPointerException() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindow window = new ContactDetailsWindow(new Stage());
            assertThrows(NullPointerException.class, () -> window.showDetails(null));
            assertFalse(window.getRoot().isShowing());
        });
    }
}
