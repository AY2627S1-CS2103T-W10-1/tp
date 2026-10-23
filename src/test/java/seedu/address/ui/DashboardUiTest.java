package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.ui.DepartmentPanel.DepartmentCount;

public class DashboardUiTest {

    @TempDir
    public Path folder;

    private ModelManager model;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void paneShortcuts_moveFocusAndHighlightFocusedPane() throws Exception {
        FxTestUtil.run(() -> {
            Scene scene = showMainWindow().getScene();
            TextField input = (TextField) scene.lookup("#commandTextField");
            input.requestFocus();
            assertHighlighted(scene, "#outputPane");

            press(scene, KeyCode.DIGIT1, true);
            assertEquals(scene.lookup("#departmentListView"), scene.getFocusOwner());
            assertHighlighted(scene, "#departmentPane");

            press(scene, KeyCode.DIGIT2, true);
            assertEquals(scene.lookup("#personListView"), scene.getFocusOwner());
            assertHighlighted(scene, "#contactPane");

            press(scene, KeyCode.DIGIT3, true);
            // Alt+2 selected the first contact, so its first detail value receives the focus.
            assertTrue(scene.getFocusOwner().getStyleClass().contains(DetailsPanel.VALUE_STYLE_CLASS));
            assertHighlighted(scene, "#detailsPane");

            press(scene, KeyCode.ESCAPE, false);
            assertEquals(input, scene.getFocusOwner());
            assertHighlighted(scene, "#outputPane");

            press(scene, KeyCode.DIGIT2, true);
            press(scene, KeyCode.DIGIT4, true);
            assertEquals(input, scene.getFocusOwner());

            Event.fireEvent(input, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ALT,
                    false, false, true, false));
            Event.fireEvent(input, new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.ALT,
                    false, false, false, false));
            assertEquals(input, scene.getFocusOwner());
        });
    }

    @Test
    public void contactSelection_followsVimKeysAndViewClearsIt() throws Exception {
        FxTestUtil.run(() -> {
            Scene scene = showMainWindow().getScene();
            TextField input = (TextField) scene.lookup("#commandTextField");
            Node details = scene.lookup("#contactDetails");
            @SuppressWarnings("unchecked")
            ListView<Person> contacts = (ListView<Person>) scene.lookup("#personListView");

            press(scene, KeyCode.DIGIT2, true);
            assertEquals(detailsOf(0), details.getAccessibleText());

            press(scene, KeyCode.J, false);
            assertEquals(1, contacts.getSelectionModel().getSelectedIndex());
            assertEquals(detailsOf(1), details.getAccessibleText());

            press(scene, KeyCode.K, false);
            assertEquals(0, contacts.getSelectionModel().getSelectedIndex());
            assertEquals(detailsOf(0), details.getAccessibleText());

            press(scene, KeyCode.ESCAPE, false);
            enter(input, "view 3");
            assertEquals(detailsOf(2), details.getAccessibleText());
            assertNull(contacts.getSelectionModel().getSelectedItem());

            press(scene, KeyCode.DIGIT3, true);
            TextArea name = (TextArea) scene.getFocusOwner();
            assertEquals(model.getFilteredPersonList().get(2).getName().fullName, name.getSelectedText());
            assertHighlighted(scene, "#detailsPane");
        });
    }

    @Test
    public void sidebarAndFooter_trackCommands() throws Exception {
        FxTestUtil.run(() -> {
            Scene scene = showMainWindow().getScene();
            TextField input = (TextField) scene.lookup("#commandTextField");
            Label count = (Label) scene.lookup("#contactCount");
            @SuppressWarnings("unchecked")
            ListView<DepartmentCount> departments =
                    (ListView<DepartmentCount>) scene.lookup("#departmentListView");
            int total = model.getAddressBook().getPersonList().size();
            assertEquals(total + " shown / " + total + " total", count.getText());
            assertEquals(DepartmentCount.allContacts(total), departments.getItems().getFirst());

            enter(input, "find Benson");
            assertEquals("1 shown / " + total + " total", count.getText());

            enter(input, "depart --add Engineering");
            enter(input, "edit 1 --department Engineering");
            assertTrue(departments.getItems().contains(DepartmentCount.department("Engineering", 1)));

            enter(input, "add -n ltm");
            assertTrue(((TextArea) scene.lookup("#resultDisplay")).getStyleClass()
                    .contains(ResultDisplay.ERROR_STYLE_CLASS));
            enter(input, "list");
            assertFalse(((TextArea) scene.lookup("#resultDisplay")).getStyleClass()
                    .contains(ResultDisplay.ERROR_STYLE_CLASS));
        });
    }

    private Stage showMainWindow() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Path dataPath = folder.resolve("contacts.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(folder.resolve("preferences.json")));
        Stage stage = new Stage();
        MainWindow main = new MainWindow(stage, new LogicManager(model, storage), dataPath);
        main.fillInnerParts();
        stage.show();
        return stage;
    }

    private String detailsOf(int zeroBasedIndex) {
        return ViewCommand.formatDetails(model.getFilteredPersonList().get(zeroBasedIndex));
    }

    private void assertHighlighted(Scene scene, String paneSelector) {
        for (String pane : new String[]{"#departmentPane", "#contactPane", "#detailsPane", "#outputPane"}) {
            Node node = scene.lookup(pane);
            assertEquals(pane.equals(paneSelector),
                    node.getStyleClass().contains(MainWindow.FOCUSED_PANE_STYLE_CLASS), pane);
        }
    }

    private void press(Scene scene, KeyCode key, boolean alt) {
        Event.fireEvent(scene.getFocusOwner(), new KeyEvent(KeyEvent.KEY_PRESSED, "", "", key,
                false, false, alt, false));
    }

    private void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
