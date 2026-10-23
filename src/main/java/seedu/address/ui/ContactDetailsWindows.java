package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.model.person.Person;

/**
 * Keeps one contact details popup per viewed contact, so several contacts can be compared side by side.
 * Viewing a contact whose popup is already open refreshes that popup instead of opening another one.
 * New popups cascade from the main window's top-right corner, so they do not cover one another exactly.
 */
public class ContactDetailsWindows {

    public static final double CASCADE_OFFSET = 28;
    /** After this many popups the cascade starts again from the first position. */
    public static final int CASCADE_STEPS = 8;

    private static final double RIGHT_MARGIN = 40;
    private static final double TOP_MARGIN = 70;

    private final Stage owner;
    private final Consumer<Scene> sceneSetup;
    /** Open popups, oldest first. */
    private final List<ContactDetailsWindow> openWindows = new ArrayList<>();

    /**
     * Creates an empty group of popups owned by {@code owner}.
     *
     * @param owner Main window, which every popup stays in front of.
     * @param sceneSetup Applied to each new popup's scene, e.g. to share the main window's shortcuts.
     */
    public ContactDetailsWindows(Stage owner, Consumer<Scene> sceneSetup) {
        this.owner = requireNonNull(owner);
        this.sceneSetup = requireNonNull(sceneSetup);
    }

    /**
     * Shows {@code details} in the popup for {@code person}, opening a new popup if that contact has none.
     *
     * @return The popup that shows the details.
     */
    public ContactDetailsWindow show(Person person, String details) {
        requireNonNull(person);
        requireNonNull(details);
        ContactDetailsWindow window = openWindows.stream()
                .filter(open -> open.showsContact(person))
                .findFirst()
                .orElseGet(this::open);
        window.showDetails(person, details);
        return window;
    }

    /**
     * Focuses the popup after {@code current} if it is one of the popups, and the oldest popup otherwise,
     * so repeated presses step through every popup once.
     *
     * @param current Window the request came from.
     * @return False if there is no popup after {@code current}.
     */
    public boolean focusAfter(Window current) {
        int index = openWindows.stream().map(ContactDetailsWindow::getRoot).toList().indexOf(current);
        if (index + 1 >= openWindows.size()) {
            return false;
        }
        openWindows.get(index + 1).focus();
        return true;
    }

    /**
     * Closes every popup.
     */
    public void hideAll() {
        List.copyOf(openWindows).forEach(ContactDetailsWindow::hide);
    }

    /**
     * Returns the open popups, oldest first.
     */
    public List<ContactDetailsWindow> getOpenWindows() {
        return List.copyOf(openWindows);
    }

    private ContactDetailsWindow open() {
        ContactDetailsWindow window = new ContactDetailsWindow(owner);
        Stage stage = window.getRoot();
        sceneSetup.accept(stage.getScene());

        int step = openWindows.size() % CASCADE_STEPS;
        double cascadeWidth = ContactDetailsWindow.WIDTH + RIGHT_MARGIN + (CASCADE_STEPS - 1) * CASCADE_OFFSET;
        stage.setX(owner.getX() + Math.max(0, owner.getWidth() - cascadeWidth) + step * CASCADE_OFFSET);
        stage.setY(owner.getY() + TOP_MARGIN + step * CASCADE_OFFSET);

        stage.setOnHidden(unused -> openWindows.remove(window));
        openWindows.add(window);
        return window;
    }
}
