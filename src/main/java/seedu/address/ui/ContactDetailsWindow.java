package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * Displays a snapshot of one contact's full details in a reusable window.
 */
public class ContactDetailsWindow extends UiPart<Stage> {

    private static final String FXML = "ContactDetailsWindow.fxml";

    @FXML
    private TextArea contactDetails;

    /**
     * Creates a contact details window owned by the main application stage.
     */
    public ContactDetailsWindow(Stage owner) {
        super(FXML);
        getRoot().initOwner(requireNonNull(owner));
    }

    /**
     * Updates the displayed details and shows or focuses the window.
     */
    public void showDetails(String details) {
        contactDetails.setText(requireNonNull(details));
        contactDetails.positionCaret(0);
        getRoot().setIconified(false);
        getRoot().show();
        getRoot().toFront();
        getRoot().requestFocus();
    }

    /**
     * Hides the contact details window.
     */
    public void hide() {
        getRoot().hide();
    }
}
