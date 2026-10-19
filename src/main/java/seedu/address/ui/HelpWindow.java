package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.logic.help.HelpPage;

/**
 * Controller for the command help window.
 */
public class HelpWindow extends UiPart<Stage> {

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";

    @FXML
    private Label helpHeading;

    @FXML
    private ScrollPane helpScroll;

    @FXML
    private HelpContentView helpContent;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        super(FXML, root);
        setPage(HelpCatalog.getOverview());
        getRoot().setWidth(560);
        getRoot().setHeight(420);
        getRoot().getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.Q && !event.isAltDown() && !event.isControlDown()
                    && !event.isMetaDown() && !event.isShiftDown()) {
                hide();
                event.consume();
            }
        });
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Shows the current help content, restoring and focusing the existing window if necessary.
     * Must be called on the JavaFX Application Thread.
     */
    public void show() {
        logger.fine("Showing command help.");
        if (!isShowing()) {
            getRoot().show();
            getRoot().centerOnScreen();
        }
        getRoot().setIconified(false);
        focus();
    }

    /**
     * Displays the selected page in this window and resets scrolling.
     * Must be called on the JavaFX Application Thread.
     *
     * @param page Command manual or overview to show.
     * @throws NullPointerException If {@code page} is null.
     */
    public void showPage(HelpPage page) {
        setPage(requireNonNull(page));
        show();
    }

    private void setPage(HelpPage page) {
        helpHeading.setText(page.title());
        helpContent.showPage(page);
        helpScroll.setVvalue(0);
        helpScroll.setHvalue(0);
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Brings the help window to the front and focuses its scroll pane for keyboard navigation and dismissal.
     */
    public void focus() {
        getRoot().toFront();
        getRoot().requestFocus();
        helpScroll.requestFocus();
    }
}
