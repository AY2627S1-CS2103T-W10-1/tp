package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class PartialAddUiTest {
    @TempDir
    public Path folder;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void enterAdd_nameOnlyAndSameName_thenCompleteAndPersist() throws Exception {
        FxTestUtil.run(() -> {
            ModelManager model = new ModelManager();
            Path dataPath = folder.resolve("contacts.json");
            JsonAddressBookStorage json = new JsonAddressBookStorage(dataPath);
            StorageManager storage = new StorageManager(json, new JsonUserPrefsStorage(folder.resolve("prefs.json")));
            Stage stage = new Stage();
            MainWindow main = new MainWindow(stage, new LogicManager(model, storage), dataPath);
            main.fillInnerParts();
            stage.show();
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            TextArea result = (TextArea) stage.getScene().lookup("#resultDisplay");

            enter(input, "add --name \"Alice Tan\"");
            assertEquals("", input.getText());
            assertEquals(1, model.getFilteredPersonList().size());
            assertTrue(result.getText().contains("Contact added successfully: Alice Tan | Phone: Not provided"));
            PersonCard card = new PersonCard(model.getFilteredPersonList().getFirst(), 1);
            for (String field : new String[]{"phone", "email", "address"}) {
                assertEquals("Not provided", ((Label) card.getRoot().lookup("#" + field)).getText());
            }
            assertEquals("Department: Not provided", ((Label) card.getRoot().lookup("#department")).getText());
            FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
            assertEquals("None", ((Label) tags.getChildren().getFirst()).getText());

            enter(input, "add --name \"Alice Tan\" --email alice@example.com");
            assertEquals(2, model.getFilteredPersonList().size());
            enter(input, "add --name \"Alice Tan\"");
            assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            assertEquals(2, model.getFilteredPersonList().size());

            enter(input, "edit 1 p/91234567 e/firstalice@example.com a/Main Road");
            assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            enter(input, "depart 1 --set Engineering");
            Person completed = model.getFilteredPersonList().getFirst();
            assertEquals("91234567", completed.getPhone().value);
            assertEquals("Engineering", completed.getDepartment().orElseThrow().value);
            assertEquals(model.getAddressBook(), assertDoesNotThrow(() -> json.readAddressBook().orElseThrow()));
            enter(input, "exit");
            assertFalse(stage.isShowing());
        });
    }

    @Test
    public void personCard_suppliedFieldsAndTags_showActualValues() throws Exception {
        FxTestUtil.run(() -> {
            Person person = new PersonBuilder().withDepartment("R&D").withTags("new-client", "team_intern").build();
            PersonCard card = new PersonCard(person, 2);
            assertEquals("85355255", ((Label) card.getRoot().lookup("#phone")).getText());
            assertEquals("amy@gmail.com", ((Label) card.getRoot().lookup("#email")).getText());
            assertEquals("Department: R&D", ((Label) card.getRoot().lookup("#department")).getText());
            FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
            assertEquals(2, tags.getChildren().size());
            assertEquals("new-client", ((Label) tags.getChildren().getFirst()).getText());
        });
    }

    private void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
