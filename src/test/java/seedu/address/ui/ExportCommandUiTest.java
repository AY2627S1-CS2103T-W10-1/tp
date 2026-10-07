package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Exercises CSV export through the real command box, feedback display and storage.
 */
public class ExportCommandUiTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void enterExport_filteredList_exportsAllAndReportsExistingFile() throws Exception {
        String fileName = "tp-export-test-" + UUID.randomUUID() + ".csv";
        Path csvPath = Path.of(fileName).toAbsolutePath();
        try {
            FxTestUtil.run(() -> {
                ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
                AddressBook original = new AddressBook(model.getAddressBook());
                Stage stage = showWindow(model);
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
                enter(input, "find Benson");
                enter(input, "export --csv " + fileName);
                assertEquals("Exported 7 contacts to " + fileName + ".", feedback.getText());
                assertEquals(1, model.getFilteredPersonList().size());
                assertEquals(original, model.getAddressBook());
                assertEquals("", input.getText());
                try {
                    String csv = Files.readString(csvPath);
                    assertEquals(8, csv.split("\r\n").length);
                    assertTrue(csv.contains("Alice Pauline"));
                    assertTrue(csv.contains("Benson Meier"));
                    JsonAddressBookStorage saved = new JsonAddressBookStorage(testFolder.resolve("contacts.json"));
                    assertEquals(original, new AddressBook(saved.readAddressBook().orElseThrow()));
                    enter(input, "export --csv " + fileName);
                    assertEquals("CSV file already exists: " + fileName
                            + ". Please choose a different filename.", feedback.getText());
                    assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                    assertEquals(csv, Files.readString(csvPath));
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            });
        } finally {
            Files.deleteIfExists(csvPath);
        }
    }

    @Test
    public void enterExport_emptyAddressBook_reportsErrorWithoutFile() throws Exception {
        String fileName = "tp-export-test-" + UUID.randomUUID() + ".csv";
        Path csvPath = Path.of(fileName).toAbsolutePath();
        try {
            FxTestUtil.run(() -> {
                Stage stage = showWindow(new ModelManager());
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                enter(input, "export --csv " + fileName);
                TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
                assertEquals("No contacts found to export.", feedback.getText());
                assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
                assertFalse(Files.exists(csvPath));
            });
        } finally {
            Files.deleteIfExists(csvPath);
        }
    }

    private Stage showWindow(ModelManager model) {
        Path dataPath = testFolder.resolve("contacts.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        Stage stage = new Stage();
        MainWindow main = new MainWindow(stage, new LogicManager(model, storage), dataPath);
        main.fillInnerParts();
        stage.show();
        return stage;
    }

    private void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
