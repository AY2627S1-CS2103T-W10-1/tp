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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ContactDetailsWindowsTest {

    private final Person alice = new PersonBuilder().withName("Alice Pauline").withPhone("94351253").build();
    private final Person benson = new PersonBuilder().withName("Benson Meier").withPhone("98765432").build();
    private final Person renamedAlice = new PersonBuilder(alice).withName("Alice Renamed").build();
    private final Person renamedBenson = new PersonBuilder(benson).withName("Benson Renamed").build();
    private final ObservableList<Person> persons = FXCollections.observableArrayList(alice, benson);

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void show_differentContacts_opensOneWindowEach() throws Exception {
        FxTestUtil.run(() -> {
            List<Scene> setUpScenes = new ArrayList<>();
            ContactDetailsWindows windows = new ContactDetailsWindows(shownOwner(), persons, setUpScenes::add);

            ContactDetailsWindow first = windows.show(alice);
            ContactDetailsWindow second = windows.show(benson);

            assertNotEquals(first, second);
            assertEquals(List.of(first, second), windows.getOpenWindows());
            assertEquals(List.of(first.getRoot().getScene(), second.getRoot().getScene()), setUpScenes);
            assertTrue(first.getRoot().isShowing());
            assertTrue(second.getRoot().isShowing());
            // Native window coordinates can include subpixel rounding on scaled displays.
            assertEquals(ContactDetailsWindows.CASCADE_OFFSET, second.getRoot().getX() - first.getRoot().getX(), 0.01);
            assertEquals(ContactDetailsWindows.CASCADE_OFFSET, second.getRoot().getY() - first.getRoot().getY(), 0.01);
        });
    }

    @Test
    public void show_sameContactAgain_refreshesItsWindow() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindows windows = new ContactDetailsWindows(shownOwner(), persons, unused -> { });
            ContactDetailsWindow first = windows.show(alice);
            windows.show(benson);

            Person editedAlice = new PersonBuilder(alice).withDepartment("Finance").build();
            ContactDetailsWindow refreshed = windows.show(editedAlice);

            assertSame(first, refreshed);
            assertEquals(2, windows.getOpenWindows().size());
            assertTrue(detailsText(first).contains("Department: Finance\n"));
        });
    }

    @Test
    public void show_afterRenamingEdit_refreshesItsWindow() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindows windows = new ContactDetailsWindows(shownOwner(), persons, unused -> { });
            ContactDetailsWindow first = windows.show(alice);
            assertFalse(renamedAlice.isSamePerson(alice));

            persons.set(0, renamedAlice);
            ContactDetailsWindow refreshed = windows.show(renamedAlice);

            assertSame(first, refreshed);
            assertEquals(List.of(first), windows.getOpenWindows());
            assertTrue(detailsText(first).startsWith("Name: Alice Renamed\n"));
            assertEquals(ContactDetailsWindow.TITLE_PREFIX + "Alice Renamed", first.getRoot().getTitle());
        });
    }

    @Test
    public void show_afterInOrderReplacementOfEveryContact_refreshesEachWindow() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindows windows = new ContactDetailsWindows(shownOwner(), persons, unused -> { });
            ContactDetailsWindow aliceWindow = windows.show(alice);
            ContactDetailsWindow bensonWindow = windows.show(benson);

            // Restoring a deleted department rewrites every contact at once, in the same order.
            persons.setAll(renamedAlice, renamedBenson);

            assertSame(aliceWindow, windows.show(renamedAlice));
            assertSame(bensonWindow, windows.show(renamedBenson));
            assertEquals(List.of(aliceWindow, bensonWindow), windows.getOpenWindows());
        });
    }

    @Test
    public void show_afterReplacementOfDifferentSize_opensNewWindow() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindows windows = new ContactDetailsWindows(shownOwner(), persons, unused -> { });
            ContactDetailsWindow aliceWindow = windows.show(alice);

            // Positions no longer pair old and new contacts, so no popup is handed over.
            persons.setAll(renamedAlice);

            assertNotEquals(aliceWindow, windows.show(renamedAlice));
            assertEquals(2, windows.getOpenWindows().size());
        });
    }

    @Test
    public void show_afterContactWindowClosed_opensNewWindow() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindows windows = new ContactDetailsWindows(shownOwner(), persons, unused -> { });
            ContactDetailsWindow first = windows.show(alice);
            first.hide();
            assertTrue(windows.getOpenWindows().isEmpty());

            ContactDetailsWindow reopened = windows.show(alice);
            assertNotEquals(first, reopened);
            assertEquals(List.of(reopened), windows.getOpenWindows());
        });
    }

    @Test
    public void focusAfter_stepsFromOldestToNewestOnce() throws Exception {
        FxTestUtil.run(() -> {
            Stage owner = shownOwner();
            ContactDetailsWindows windows = new ContactDetailsWindows(owner, persons, unused -> { });
            assertFalse(windows.focusAfter(owner));

            ContactDetailsWindow first = windows.show(alice);
            ContactDetailsWindow second = windows.show(benson);

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
            ContactDetailsWindows windows = new ContactDetailsWindows(shownOwner(), persons, unused -> { });
            ContactDetailsWindow first = windows.show(alice);
            ContactDetailsWindow second = windows.show(benson);

            windows.hideAll();

            assertFalse(first.getRoot().isShowing());
            assertFalse(second.getRoot().isShowing());
            assertTrue(windows.getOpenWindows().isEmpty());
        });
    }

    @Test
    public void show_nullArgument_throwsNullPointerException() throws Exception {
        FxTestUtil.run(() -> {
            ContactDetailsWindows windows = new ContactDetailsWindows(shownOwner(), persons, unused -> { });
            assertThrows(NullPointerException.class, () -> windows.show(null));
            assertTrue(windows.getOpenWindows().isEmpty());
            Stage owner = shownOwner();
            assertThrows(NullPointerException.class, () -> new ContactDetailsWindows(owner, null, unused -> { }));
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
