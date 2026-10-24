package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.model.person.Person;

/**
 * Popup showing a snapshot of one contact's full details in a {@link DetailsPanel}. Several can be open at once,
 * one per contact; {@link ContactDetailsWindows} decides which popup shows which contact.
 * Press q in the popup to close it.
 */
public class ContactDetailsWindow extends UiPart<Stage> {

    public static final String TITLE_PREFIX = "Contact details: ";
    /** Width the popup opens with, as set in the FXML file. */
    public static final double WIDTH = 440;

    private static final String FXML = "ContactDetailsWindow.fxml";

    private final DetailsPanel detailsPanel = new DetailsPanel();
    private Person person;

    @FXML
    private StackPane detailsPanelPlaceholder;

    /**
     * Creates a contact details popup owned by the main application stage.
     */
    public ContactDetailsWindow(Stage owner) {
        super(FXML);
        getRoot().initOwner(requireNonNull(owner));
        detailsPanelPlaceholder.getChildren().add(detailsPanel.getRoot());
        getRoot().getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::handleClose);
    }

    /**
     * Shows {@code details} for {@code person}, replacing any earlier snapshot, and brings the popup to the front.
     */
    public void showDetails(Person person, String details) {
        requireNonNull(person);
        requireNonNull(details);
        this.person = person;
        getRoot().setTitle(TITLE_PREFIX + person.getName().fullName);
        detailsPanel.showDetails(details);
        getRoot().setIconified(false);
        getRoot().show();
        getRoot().toFront();
    }

    /**
     * Returns true if this popup is open and shows {@code other}, or an earlier snapshot of the same contact.
     */
    public boolean showsContact(Person other) {
        // A popup is only shown by showDetails, which sets the contact first.
        return getRoot().isShowing() && person.isSamePerson(other);
    }

    /**
     * Ties this popup to {@code edited} if it shows {@code original}, so that viewing the edited contact
     * refreshes this popup. The displayed snapshot stays unchanged until then.
     */
    public void followEdit(Person original, Person edited) {
        requireNonNull(original);
        requireNonNull(edited);
        if (original.equals(person)) {
            person = edited;
        }
    }

    /**
     * Brings the popup to the front and gives its first value keyboard focus, so values can be copied.
     */
    public void focus() {
        getRoot().toFront();
        getRoot().requestFocus();
        detailsPanel.focus();
    }

    /**
     * Hides the contact details popup.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Closes the popup on an unmodified q and hands the keyboard back to the main window.
     */
    private void handleClose(KeyEvent event) {
        if (event.getCode() != KeyCode.Q || event.isAltDown() || event.isControlDown() || event.isMetaDown()
                || event.isShiftDown()) {
            return;
        }
        event.consume();
        hide();
        getRoot().getOwner().requestFocus();
    }
}
