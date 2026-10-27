package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.LogCapture;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks that actual command-box execution keeps private input and feedback out of application logs.
 */
public class CommandLoggingUiTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void enterExport_successAndFailures_doNotLogFilenameAtAnyLevel() throws Exception {
        String privateStem = "confidential-" + UUID.randomUUID();
        String fileName = privateStem + " client list.csv";
        Path csvPath = Path.of(fileName).toAbsolutePath();
        try (LogCapture logs = new LogCapture()) {
            FxTestUtil.run(() -> {
                AddressBook addressBook = new AddressBook();
                addressBook.addPerson(new PersonBuilder().build());
                Stage stage = showWindow(new ModelManager(addressBook, new UserPrefs()));
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
                String command = "EXPORT -c \"" + fileName + "\"";
                enter(input, command);
                assertEquals("Exported 1 contacts to " + fileName + ".", feedback.getText());
                assertTrue(Files.exists(csvPath));
                enter(input, command);
                assertTrue(feedback.getText().startsWith("CSV file already exists: " + fileName));
                assertEquals(command, input.getText());
                enter(input, "export --csv " + privateStem + ".CSV");
                assertTrue(feedback.getText().contains("extension must be lowercase .csv"));
                enter(input, privateStem + ".csv");
                assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            });
            String output = logs.getOutput();
            assertFalse(output.contains(privateStem), output);
            assertTrue(output.contains("Executing command: ExportCommand"), output);
            assertTrue(output.contains("CSV export completed: 1 contacts written."), output);
            assertTrue(output.contains("destination file already exists"), output);
            assertTrue(output.contains("Command execution failed: CommandException"), output);
            assertTrue(output.contains("Command execution failed: ParseException"), output);
        } finally {
            Files.deleteIfExists(csvPath);
        }
    }

    @Test
    public void enterViewAndAdd_doNotLogContactFieldsOrFeedbackAtAnyLevel() throws Exception {
        Person person = new PersonBuilder().withName("Private Contact")
                .withEmail("private.contact@example.com").withPhone("98765432")
                .withAddress("Private Street").withTags("confidential").build();
        try (LogCapture logs = new LogCapture()) {
            FxTestUtil.run(() -> {
                AddressBook addressBook = new AddressBook();
                addressBook.addPerson(person);
                Stage stage = showWindow(new ModelManager(addressBook, new UserPrefs()));
                TextField input = (TextField) stage.getScene().lookup("#commandTextField");
                TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");
                enter(input, "view 1");
                assertEquals("Showing details of contact 1: Private Contact", feedback.getText());
                enter(input, "add --name Other Private Contact --email other.private@example.com");
                assertTrue(feedback.getText().contains("Other Private Contact"));
                enter(input, "add --name Private Contact --phone private-phone");
                assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            });
            String output = logs.getOutput();
            for (String field : new String[] {"Private Contact", "private.contact@example.com", "98765432",
                "Private Street", "confidential", "other.private@example.com", "private-phone"}) {
                assertFalse(output.contains(field), output);
            }
            assertTrue(output.contains("Preparing contact details for displayed index 1."), output);
            assertTrue(output.contains("Executing command: AddCommand"), output);
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
