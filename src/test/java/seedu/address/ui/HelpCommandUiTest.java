package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.DepartCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class HelpCommandUiTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void enterHelp_reusesWindowAndMenuAndF1ResetOverview() throws Exception {
        FxTestUtil.run(() -> {
            ModelManager model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
            List<Person> contacts = List.copyOf(model.getFilteredPersonList());
            Path dataPath = testFolder.resolve("contacts.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                    new JsonUserPrefsStorage(testFolder.resolve("preferences.json")));
            Stage stage = new Stage();
            MainWindow main = new MainWindow(stage, new LogicManager(model, storage), dataPath);
            main.fillInnerParts();
            stage.show();
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            TextArea feedback = (TextArea) stage.getScene().lookup("#resultDisplay");

            enter(input, "help depart");
            Stage help = helpStage();
            TextArea text = (TextArea) help.getScene().lookup("#helpText");
            Label heading = (Label) help.getScene().lookup("#helpHeading");
            assertEquals(HelpCommand.SHOWING_HELP_MESSAGE, feedback.getText());
            assertEquals(DepartCommand.HELP_PAGE.text(), text.getText());
            assertEquals("depart", heading.getText());
            assertFalse(text.isEditable());
            assertTrue(text.isWrapText());

            stage.toFront();
            stage.requestFocus();
            input.requestFocus();
            awaitFocus(stage::isFocused);
            assertTrue(stage.isFocused());

            enter(input, "? sort");
            awaitFocus(() -> help.isFocused() && text.isFocused());
            assertTrue(help.isFocused());
            assertTrue(text.isFocused());
            assertEquals(help, helpStage());
            assertEquals(SortCommand.HELP_PAGE.text(), text.getText());
            enter(input, "help missing");
            assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            assertTrue(feedback.getText().contains("Unknown help command: missing"));
            assertEquals(SortCommand.HELP_PAGE.text(), text.getText());

            MenuBar menu = (MenuBar) stage.getScene().lookup("#menuBar");
            menu.getMenus().get(1).getItems().getFirst().fire();
            assertEquals(HelpCatalog.getOverview().text(), text.getText());
            enter(input, "help depart");
            input.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F1,
                    false, false, false, false));
            assertEquals(HelpCatalog.getOverview().text(), text.getText());
            assertEquals("Available commands", heading.getText());
            assertEquals(help, helpStage());
            assertEquals(contacts, model.getFilteredPersonList());
            assertFalse(Files.exists(dataPath));

            enter(input, "exit");
            assertFalse(help.isShowing());
            assertFalse(stage.isShowing());
        });
    }

    private static Stage helpStage() {
        return (Stage) Window.getWindows().stream()
                .filter(window -> window instanceof Stage candidate && "Help".equals(candidate.getTitle()))
                .findFirst().orElseThrow();
    }

    private static void awaitFocus(BooleanSupplier focused) {
        // Allow the window manager to acknowledge focus changes without blocking the JavaFX thread.
        Object loopKey = new Object();
        Timeline poll = new Timeline();
        poll.getKeyFrames().add(new KeyFrame(Duration.millis(50), event -> {
            if (focused.getAsBoolean()) {
                poll.stop();
                Platform.exitNestedEventLoop(loopKey, null);
            }
        }));
        poll.setCycleCount(60);
        poll.setOnFinished(event -> Platform.exitNestedEventLoop(loopKey, null));
        poll.play();
        Platform.enterNestedEventLoop(loopKey);
    }

    private static void enter(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }
}
