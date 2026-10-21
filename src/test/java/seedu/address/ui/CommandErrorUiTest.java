package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class CommandErrorUiTest {
    private static final String ADD_EXAMPLES = "\n\nExamples:\n  add --name \"Alice Tan\" --phone 91234567\n"
            + "  add -n \"Alice Tan\" -e alice@example.com -d Engineering -t client important";
    private static final String DELETE_EXAMPLES = "\n\nExamples:\n  delete 2";

    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void enter_invalidInputs_showExamplesAndPreserveInputAndContacts() throws Exception {
        FxTestUtil.run(() -> {
            ModelManager model = new ModelManager();
            Stage stage = showWindow(model, jsonStorage());
            TextArea feedback = feedback(stage);
            TextField input = input(stage);
            assertError(stage, "add --phone 91234567", AddCommand.MESSAGE_NAME_REQUIRED + ADD_EXAMPLES);
            assertError(stage, "add --name Alice --phone 12", ParserUtil.MESSAGE_INVALID_PHONE + ADD_EXAMPLES);
            assertError(stage, "add --name Alice", Person.MESSAGE_CONTACT_INFORMATION_REQUIRED + ADD_EXAMPLES);
            assertError(stage, "nonsense", MESSAGE_UNKNOWN_COMMAND + "\n\nExamples:\n  help");
            assertError(stage, "LS extra", ListCommand.MESSAGE_USAGE + "\n\nExamples:\n  list\n  ls");
            assertError(stage, "delete -1", String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
            assertTrue(model.getAddressBook().getPersonList().isEmpty());
            assertFalse(Files.exists(testFolder.resolve("contacts.json")));
            assertTrue(feedback.isWrapText());
            assertFalse(feedback.isEditable());
            assertTrue(Window.getWindows().stream().noneMatch(window ->
                    window instanceof Stage candidate && "Help".equals(candidate.getTitle())));

            enter(input, "ls");
            assertEquals(ListCommand.MESSAGE_SUCCESS, feedback.getText());
            assertEquals("", input.getText());
            assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void enter_invalidIndex_preservesFilterContactsAndDeletionHistory() throws Exception {
        FxTestUtil.run(() -> {
            ModelManager model = new ModelManager();
            Person alice = new PersonBuilder().withName("Alice").build();
            Person bob = new PersonBuilder().withName("Bob").build();
            model.addPerson(alice);
            model.addPerson(bob);
            model.deletePerson(bob);
            model.updateFilteredPersonList(alice::equals);
            Stage stage = showWindow(model, jsonStorage());
            assertError(stage, "DELETE 999", MESSAGE_INVALID_PERSON_DISPLAYED_INDEX + DELETE_EXAMPLES);
            assertEquals(List.of(alice), model.getAddressBook().getPersonList());
            assertEquals(List.of(alice), model.getFilteredPersonList());
            assertEquals(bob, model.getLastDeletedPerson());
            assertFalse(Files.exists(testFolder.resolve("contacts.json")));
        });
    }

    @Test
    public void enter_duplicateContact_showsExamplesWithoutChangingSavedData() throws Exception {
        FxTestUtil.run(() -> {
            ModelManager model = new ModelManager();
            Stage stage = showWindow(model, jsonStorage());
            enter(input(stage), "add --name Alice --phone 91234567");
            Path dataPath = testFolder.resolve("contacts.json");
            String saved = assertDoesNotThrow(() -> Files.readString(dataPath));
            assertError(stage, "add --name Alice --phone 91234567", AddCommand.MESSAGE_DUPLICATE_PERSON + ADD_EXAMPLES);
            assertEquals(1, model.getAddressBook().getPersonList().size());
            assertEquals(saved, assertDoesNotThrow(() -> Files.readString(dataPath)));
        });
    }

    @Test
    public void enter_storageIoFailure_preservesErrorAndDoesNotRetryCommand() throws Exception {
        assertStorageFailure(new IOException("Simulated write failure"),
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, "Simulated write failure"));
    }

    @Test
    public void enter_storagePermissionFailure_preservesErrorAndDoesNotRetryCommand() throws Exception {
        assertStorageFailure(new AccessDeniedException("contacts.json"),
                String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, "contacts.json"));
    }

    private void assertStorageFailure(IOException failure, String expected) throws Exception {
        FxTestUtil.run(() -> {
            ModelManager model = new ModelManager();
            Person alice = new PersonBuilder().withName("Alice").build();
            model.addPerson(alice);
            AtomicInteger saves = new AtomicInteger();
            JsonAddressBookStorage blocked = new JsonAddressBookStorage(testFolder.resolve("contacts.json")) {
                @Override
                public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                    saves.incrementAndGet();
                    throw failure;
                }
            };
            Stage stage = showWindow(model, blocked);
            assertError(stage, "delete 1", expected + DELETE_EXAMPLES);
            assertEquals(1, saves.get());
            // The deletion happened before saving failed; displaying examples must not retry or undo it.
            assertTrue(model.getAddressBook().getPersonList().isEmpty());
            assertEquals(alice, model.getLastDeletedPerson());
        });
    }

    private JsonAddressBookStorage jsonStorage() {
        return new JsonAddressBookStorage(testFolder.resolve("contacts.json"));
    }

    private Stage showWindow(ModelManager model, JsonAddressBookStorage addressBookStorage) {
        StorageManager storage = new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
        Stage stage = new Stage();
        MainWindow main = new MainWindow(stage, new LogicManager(model, storage),
                addressBookStorage.getAddressBookFilePath());
        main.fillInnerParts();
        stage.show();
        return stage;
    }

    private static void assertError(Stage stage, String command, String expected) {
        enter(input(stage), command);
        assertEquals(expected, feedback(stage).getText());
        assertEquals(command, input(stage).getText());
        assertTrue(input(stage).getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
    }

    private static TextField input(Stage stage) {
        return (TextField) stage.getScene().lookup("#commandTextField");
    }

    private static TextArea feedback(Stage stage) {
        return (TextArea) stage.getScene().lookup("#resultDisplay");
    }

    private static void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
