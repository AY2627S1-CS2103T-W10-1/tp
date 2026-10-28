package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class DashboardUiTest {

    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void view_fromMaximizedMainWindow_keepsDefaultPopupSizeAfterNativeLayout() throws Exception {
        FxTestUtil.run(() -> {
            MainWindow main = showMainWindow();
            main.getPrimaryStage().setMaximized(true);
            FxTestUtil.awaitWindowEvents();
            TextField input = (TextField) main.getPrimaryStage().getScene().lookup("#commandTextField");

            enter(input, "view 1");
            FxTestUtil.awaitWindowEvents();

            Stage popup = main.getContactDetailsWindows().getOpenWindows().getFirst().getRoot();
            assertEquals(560, popup.getScene().getWidth(), 2);
            assertTrue(popup.getScene().getHeight() >= 420 - 2,
                    "The contact content must keep its preferred height after native layout");
        });
    }

    @Test
    public void keyboard_movesBetweenPanesAndOpensSelectedContact() throws Exception {
        FxTestUtil.run(() -> {
            MainWindow main = showMainWindow();
            Scene scene = main.getPrimaryStage().getScene();
            TextField input = (TextField) scene.lookup("#commandTextField");
            TextArea feedback = (TextArea) scene.lookup("#resultDisplay");
            @SuppressWarnings("unchecked")
            ListView<Person> contacts = (ListView<Person>) scene.lookup("#personListView");
            assertSame(input, scene.getFocusOwner());

            press(input, KeyCode.DIGIT1, true);
            assertSame(scene.lookup("#departmentListView"), scene.getFocusOwner());

            press(scene.getFocusOwner(), KeyCode.DIGIT2, true);
            assertSame(contacts, scene.getFocusOwner());
            assertEquals(0, contacts.getSelectionModel().getSelectedIndex());
            assertTrue(dockedDetails(scene).startsWith("Name: Alice Pauline\n"));

            press(contacts, KeyCode.J, false);
            assertEquals(1, contacts.getSelectionModel().getSelectedIndex());
            assertTrue(dockedDetails(scene).startsWith("Name: Benson Meier\n"));
            assertTrue(main.getContactDetailsWindows().getOpenWindows().isEmpty());
            press(contacts, KeyCode.ENTER, false);
            assertEquals("Showing details of contact 2: Benson Meier", feedback.getText());
            assertEquals(1, main.getContactDetailsWindows().getOpenWindows().size());
            assertSame(contacts, scene.getFocusOwner());

            press(contacts, KeyCode.DIGIT4, true);
            assertSame(input, scene.getFocusOwner());
            press(input, KeyCode.DIGIT2, true);
            press(contacts, KeyCode.ESCAPE, false);
            assertSame(input, scene.getFocusOwner());
        });
    }

    @Test
    public void enterOnContact_storageCannotWrite_showsDetailsWithoutSaving() throws Exception {
        FxTestUtil.run(() -> {
            // A folder in place of the data file prevents saves, but read-only viewing must succeed.
            MainWindow main = showMainWindow(testFolder);
            Scene scene = main.getPrimaryStage().getScene();
            TextArea feedback = (TextArea) scene.lookup("#resultDisplay");
            Node contacts = scene.lookup("#personListView");

            press(scene.lookup("#commandTextField"), KeyCode.DIGIT2, true);
            press(contacts, KeyCode.ENTER, false);

            assertEquals("Showing details of contact 1: Alice Pauline", feedback.getText());
            assertEquals(1, main.getContactDetailsWindows().getOpenWindows().size());
            assertTrue(main.getPrimaryStage().isShowing());
        });
    }

    @Test
    public void altThree_stepsThroughDockedPaneThenPopups() throws Exception {
        FxTestUtil.run(() -> {
            MainWindow main = showMainWindow();
            Scene scene = main.getPrimaryStage().getScene();
            TextField input = (TextField) scene.lookup("#commandTextField");
            Label placeholder = (Label) ((GridPane) ((ScrollPane) scene.lookup("#detailsScroll")).getContent())
                    .getChildren().getFirst();
            assertEquals(DetailsPanel.PLACEHOLDER, placeholder.getText());

            press(input, KeyCode.DIGIT3, true);
            assertTrue(isInDetailsPane(scene));
            press(scene.getFocusOwner(), KeyCode.DIGIT3, true);
            assertTrue(isInDetailsPane(scene));

            enter(input, "view 1");
            enter(input, "view 2");
            Scene aliceScene = main.getContactDetailsWindows().getOpenWindows().get(0).getRoot().getScene();
            Scene bensonScene = main.getContactDetailsWindows().getOpenWindows().get(1).getRoot().getScene();

            press(input, KeyCode.DIGIT3, true);
            assertTrue(isInDetailsPane(scene));
            press(scene.getFocusOwner(), KeyCode.DIGIT3, true);
            assertTrue(isDetailsValue(aliceScene.getFocusOwner()));
            press(aliceScene.getFocusOwner(), KeyCode.DIGIT3, true);
            assertTrue(isDetailsValue(bensonScene.getFocusOwner()));
            press(bensonScene.getFocusOwner(), KeyCode.DIGIT3, true);
            assertTrue(isInDetailsPane(scene));

            press(bensonScene.getFocusOwner(), KeyCode.ESCAPE, false);
            assertSame(input, scene.getFocusOwner());
        });
    }

    @Test
    public void closePopupFromContactsPane_returnsToPromptAndKeepsOtherPopup() throws Exception {
        FxTestUtil.run(() -> {
            MainWindow main = showMainWindow();
            Scene scene = main.getPrimaryStage().getScene();
            TextField input = (TextField) scene.lookup("#commandTextField");
            enter(input, "view 1");
            enter(input, "view 2");
            ContactDetailsWindow alice = main.getContactDetailsWindows().getOpenWindows().getFirst();
            ContactDetailsWindow benson = main.getContactDetailsWindows().getOpenWindows().getLast();
            press(input, KeyCode.DIGIT2, true);
            assertSame(scene.lookup("#personListView"), scene.getFocusOwner());
            alice.focus();

            press(alice.getRoot().getScene().getFocusOwner(), KeyCode.Q, false);

            assertEquals(1, main.getContactDetailsWindows().getOpenWindows().size());
            assertSame(benson, main.getContactDetailsWindows().getOpenWindows().getFirst());
            assertSame(input, scene.getFocusOwner());
            assertTrue(benson.getRoot().isShowing());
            enter(input, "view 1");
            assertEquals(2, main.getContactDetailsWindows().getOpenWindows().size());
        });
    }

    @Test
    public void doubleClick_opensClickedContactsPopup() throws Exception {
        FxTestUtil.run(() -> {
            MainWindow main = showMainWindow();
            Scene scene = main.getPrimaryStage().getScene();
            TextArea feedback = (TextArea) scene.lookup("#resultDisplay");
            ListCell<?> carl = scene.lookup("#personListView").lookupAll(".list-cell").stream()
                    .map(ListCell.class::cast)
                    .filter(cell -> !cell.isEmpty() && cell.getIndex() == 2)
                    .findFirst().orElseThrow();

            click(carl, 1);
            assertTrue(main.getContactDetailsWindows().getOpenWindows().isEmpty());

            click(carl, 2);
            assertEquals("Showing details of contact 3: Carl Kurz", feedback.getText());
            assertEquals(ContactDetailsWindow.TITLE_PREFIX + "Carl Kurz",
                    main.getContactDetailsWindows().getOpenWindows().getFirst().getRoot().getTitle());

            click(carl, 2);
            assertEquals(1, main.getContactDetailsWindows().getOpenWindows().size());
        });
    }

    @Test
    public void selectingContact_showsItInDockedPaneWithoutPopup() throws Exception {
        FxTestUtil.run(() -> {
            MainWindow main = showMainWindow();
            Scene scene = main.getPrimaryStage().getScene();
            @SuppressWarnings("unchecked")
            ListView<Person> contacts = (ListView<Person>) scene.lookup("#personListView");

            // A single click selects the row, as here.
            contacts.getSelectionModel().select(2);
            assertTrue(dockedDetails(scene).startsWith("Name: Carl Kurz\n"));
            assertTrue(main.getContactDetailsWindows().getOpenWindows().isEmpty());

            contacts.getSelectionModel().clearSelection();
            assertTrue(dockedDetails(scene).startsWith("Name: Carl Kurz\n"));
        });
    }

    @Test
    public void footer_countsDisplayedAndStoredContacts() throws Exception {
        FxTestUtil.run(() -> {
            MainWindow main = showMainWindow();
            Scene scene = main.getPrimaryStage().getScene();
            TextField input = (TextField) scene.lookup("#commandTextField");
            Label count = (Label) scene.lookup("#contactCount");
            int total = getTypicalAddressBook().getPersonList().size();
            assertEquals(total + " shown / " + total + " total", count.getText());

            enter(input, "find Meier");
            assertTrue(count.getText().startsWith("2 shown / " + total));
            assertEquals("sudoContact", main.getPrimaryStage().getTitle());
        });
    }

    private MainWindow showMainWindow() {
        return showMainWindow(testFolder.resolve("contacts.json"));
    }

    private MainWindow showMainWindow(Path dataPath) {
        ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        Stage stage = new Stage();
        MainWindow main = new MainWindow(stage, new LogicManager(model, storage), dataPath);
        main.fillInnerParts();
        stage.show();
        return main;
    }

    private static boolean isDetailsValue(Node node) {
        return node != null && node.getStyleClass().contains(DetailsPanel.VALUE_STYLE_CLASS);
    }

    /** Returns the snapshot shown in the docked [3] pane. */
    private static String dockedDetails(Scene scene) {
        return ((ScrollPane) scene.lookup("#detailsScroll")).getContent().getAccessibleText();
    }

    private static boolean isInDetailsPane(Scene scene) {
        Node detailsPane = scene.lookup("#detailsPane");
        for (Node node = scene.getFocusOwner(); node != null; node = node.getParent()) {
            if (node == detailsPane) {
                return true;
            }
        }
        return false;
    }

    private static void click(Node target, int clickCount) {
        Event.fireEvent(target, new MouseEvent(MouseEvent.MOUSE_CLICKED, 5, 5, 5, 5, MouseButton.PRIMARY,
                clickCount, false, false, false, false, false, false, false, false, false, true, null));
    }

    private static void press(Node target, KeyCode code, boolean altDown) {
        Event.fireEvent(target, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, altDown, false));
    }

    private static void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
