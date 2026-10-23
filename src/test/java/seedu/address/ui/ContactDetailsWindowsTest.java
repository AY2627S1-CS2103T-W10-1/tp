package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ContactDetailsWindowsTest {

    private final Person alice = new PersonBuilder().withName("Alice Pauline").withPhone("94351253").build();
    private final Person benson = new PersonBuilder().withName("Benson Meier").withPhone("98765432").build();

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void show_differentContacts_opensOneWindowEach() throws Exception {
        FxTestUtil.run(() -> {
            List<Scene> setUpScenes = new ArrayList<>();
            ContactDetailsWindows windows = new ContactDetailsWindows(shownOwner(), setUpScenes::add);

            ContactDetailsWindow first = windows.show(alice, "Alice");
            ContactDetailsWindow second = windows.show(benson, "Benson");

            assertNotEquals(first, second);
            assertEquals(List.of(first, second), windows.getOpenWindows());
            assertEquals(List.of(first.getRoot().getScene(), second.getRoot().getScene()), setUpScenes);
            assertTrue(first.getRoot().isShowing());
            assertTrue(second.getRoot().isShowing());
            assertEquals(ContactDetailsWindows.CASCADE_OFFSET, second.getRoot().getX() - first.getRoot().getX());
            assertEquals(ContactDetailsWindows.CASCADE_OFFSET, second.getRoot().getY() - first.getRoot().getY());
        });
    }

    @Test
    public void show_sameContactAgain_refreshesItsWindow() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindows windows = new ContactDetailsWindows(shownOwner(), unused -> { });
            ContactDetailsWindow first = windows.show(alice, "Department: Not provided");
            windows.show(benson, "Benson");

            Person editedAlice = new PersonBuilder(alice).withDepartment("Finance").build();
            ContactDetailsWindow refreshed = windows.show(editedAlice, "Department: Finance");

            assertSame(first, refreshed);
            assertEquals(2, windows.getOpenWindows().size());
            assertEquals("Department: Finance", detailsText(first));
        });
    }

    @Test
    public void show_afterContactWindowClosed_opensNewWindow() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindows windows = new ContactDetailsWindows(shownOwner(), unused -> { });
            ContactDetailsWindow first = windows.show(alice, "Alice");
            first.hide();
            assertTrue(windows.getOpenWindows().isEmpty());

            ContactDetailsWindow reopened = windows.show(alice, "Alice again");
            assertNotEquals(first, reopened);
            assertEquals(List.of(reopened), windows.getOpenWindows());
        });
    }

    @Test
    public void focusAfter_stepsFromOldestToNewestOnce() throws Exception {
        FxTestUtil.run(() -> {
            Stage owner = shownOwner();
            ContactDetailsWindows windows = new ContactDetailsWindows(owner, unused -> { });
            assertFalse(windows.focusAfter(owner));

            ContactDetailsWindow first = windows.show(alice, "Name: Alice");
            ContactDetailsWindow second = windows.show(benson, "Name: Benson");

            assertTrue(windows.focusAfter(owner));
            assertTrue(hasFocusedDetails(first));
            assertTrue(windows.focusAfter(first.getRoot()));
            assertTrue(hasFocusedDetails(second));
            assertFalse(windows.focusAfter(second.getRoot()));
        });
    }

    @Test
    public void hideAll_closesEveryWindow() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindows windows = new ContactDetailsWindows(shownOwner(), unused -> { });
            ContactDetailsWindow first = windows.show(alice, "Alice");
            ContactDetailsWindow second = windows.show(benson, "Benson");

            windows.hideAll();

            assertFalse(first.getRoot().isShowing());
            assertFalse(second.getRoot().isShowing());
            assertTrue(windows.getOpenWindows().isEmpty());
        });
    }

    @Test
    public void show_nullArgument_throwsNullPointerException() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindows windows = new ContactDetailsWindows(shownOwner(), unused -> { });
            assertThrows(NullPointerException.class, () -> windows.show(null, "details"));
            assertThrows(NullPointerException.class, () -> windows.show(alice, null));
            assertTrue(windows.getOpenWindows().isEmpty());
        });
    }

    private static Stage shownOwner() {
        Stage owner = new Stage();
        owner.setWidth(1100);
        owner.setHeight(700);
        owner.show();
        return owner;
    }

    private static boolean hasFocusedDetails(ContactDetailsWindow window) {
        Node focusOwner = window.getRoot().getScene().getFocusOwner();
        return focusOwner != null && focusOwner.getStyleClass().contains(DetailsPanel.VALUE_STYLE_CLASS);
    }

    private static String detailsText(ContactDetailsWindow window) {
        Scene scene = window.getRoot().getScene();
        return ((ScrollPane) scene.lookup("#detailsScroll")).getContent().getAccessibleText();
    }
}
