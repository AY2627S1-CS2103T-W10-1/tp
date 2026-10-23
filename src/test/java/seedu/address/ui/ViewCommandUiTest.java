package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class ViewCommandUiTest {

    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void enterView_differentContacts_opensOnePopupEachAndExitClosesThem() throws Exception {
        FxTestUtil.run(() -> {
            ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
            Path dataPath = testFolder.resolve("contacts.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                    new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
            Stage stage = new Stage();
            MainWindow main = new MainWindow(stage, new LogicManager(model, storage), dataPath);
            main.fillInnerParts();
            stage.show();
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
            ContactDetailsWindows popups = main.getContactDetailsWindows();

            enter(input, "find Meier");
            enter(input, "view 1");
            assertEquals("Showing details of contact 1: Benson Meier", feedback.getText());
            enter(input, "view 2");
            assertEquals("Showing details of contact 2: Daniel Meier", feedback.getText());

            List<ContactDetailsWindow> open = popups.getOpenWindows();
            assertEquals(2, open.size());
            ContactDetailsWindow benson = open.get(0);
            assertEquals(ContactDetailsWindow.TITLE_PREFIX + "Benson Meier", benson.getRoot().getTitle());
            assertEquals(ContactDetailsWindow.TITLE_PREFIX + "Daniel Meier", open.get(1).getRoot().getTitle());
            assertTrue(detailsText(benson).contains("Name: Benson Meier\n"));
            assertTrue(detailsText(benson).contains("Department: Not provided\n"));
            assertEquals(stage, benson.getRoot().getOwner());
            assertEquals("", input.getText());

            enter(input, "view 3");
            assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            assertEquals(2, popups.getOpenWindows().size());

            enter(input, "depart --add \"R&D Engineering\"");
            enter(input, "find Benson");
            enter(input, "edit 1 --department \"R&D Engineering\"");
            enter(input, "find Benson");
            assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            assertTrue(detailsText(benson).contains("Department: Not provided\n"));

            enter(input, "view 1");
            assertTrue(detailsText(benson).contains("Department: R&D Engineering\n"));
            assertEquals(2, popups.getOpenWindows().size());
            assertSame(benson, popups.getOpenWindows().getFirst());

            enter(input, "exit");
            assertFalse(benson.getRoot().isShowing());
            assertTrue(popups.getOpenWindows().isEmpty());
            assertFalse(stage.isShowing());
        });
    }

    private static String detailsText(ContactDetailsWindow window) {
        Scene scene = window.getRoot().getScene();
        return ((ScrollPane) scene.lookup("#detailsScroll")).getContent().getAccessibleText();
    }

    private void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
