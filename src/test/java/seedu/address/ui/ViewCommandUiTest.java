package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
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
    public void enterView_filteredList_opensDetailsAndExitClosesWindow() throws Exception {
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

            enter(input, "find Benson");
            enter(input, "view 1");
            TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
            assertEquals("Showing details of contact 1: Benson Meier", feedback.getText());

            Stage details = (Stage) Window.getWindows().stream()
                    .filter(window -> window instanceof Stage candidate
                            && "Contact details".equals(candidate.getTitle()))
                    .findFirst().orElseThrow();
            TextArea text = (TextArea) details.getScene().lookup("#contactDetails");
            assertTrue(text.getText().contains("Name: Benson Meier\n"));
            assertTrue(text.getText().contains("Department: Not provided\n"));
            assertTrue(details.isShowing());
            assertEquals(1, model.getFilteredPersonList().size());
            assertEquals("", input.getText());

            enter(input, "view 2");
            assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            assertTrue(text.getText().contains("Name: Benson Meier\n"));

            enter(input, "depart 1 --set R&D Engineering");
            assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            assertTrue(text.getText().contains("Department: Not provided\n"));
            enter(input, "view 1");
            assertTrue(text.getText().contains("Department: R&D Engineering\n"));
            assertEquals("Showing details of contact 1: Benson Meier", feedback.getText());
            assertEquals(1, model.getFilteredPersonList().size());
            assertEquals(details, Window.getWindows().stream()
                    .filter(window -> window instanceof Stage candidate
                            && "Contact details".equals(candidate.getTitle()))
                    .findFirst().orElseThrow());

            enter(input, "exit");
            assertFalse(details.isShowing());
            assertFalse(stage.isShowing());
        });
    }

    private void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
