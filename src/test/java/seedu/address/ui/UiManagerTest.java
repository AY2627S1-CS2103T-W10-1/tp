package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.MainApp;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class UiManagerTest {
    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void start_initializesMainWindowAndDisplaysOwnedAlert() throws Exception {
        FxTestUtil.run(() -> {
            UiManager manager = new UiManager(createLogic(new ModelManager()), testFolder.resolve("contacts.json"));
            Stage stage = new Stage();
            new AppWithUi(manager).start(stage);
            assertTrue(stage.isShowing());
            assertFalse(stage.getIcons().isEmpty());
            assertNotNull(stage.getScene().lookup("#commandTextField"));
            AtomicReference<DialogPane> shownDialog = new AtomicReference<>();
            AtomicReference<Window> owner = new AtomicReference<>();
            Platform.runLater(() -> Window.getWindows().stream().toList().forEach(window -> {
                if (window.getScene().getRoot() instanceof DialogPane pane) {
                    shownDialog.set(pane);
                    owner.set(((Stage) window).getOwner());
                    window.hide();
                }
            }));
            manager.showAlertDialogAndWait(AlertType.INFORMATION, "Notice", "Header", "Details");
            assertNotNull(shownDialog.get());
            assertEquals("Header", shownDialog.get().getHeaderText());
            assertEquals("Details", shownDialog.get().getContentText());
            assertEquals(UiManager.ALERT_DIALOG_PANE_FIELD_ID, shownDialog.get().getId());
            assertEquals(stage, owner.get());
        });
    }

    @Test
    public void mainWindow_restoresSavedGeometryAndProvidesContactPanel() throws Exception {
        FxTestUtil.run(() -> {
            ModelManager model = new ModelManager();
            model.setGuiSettings(new GuiSettings(900, 700, 120, 80));
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, createLogic(model), testFolder.resolve("contacts.json"));
            assertEquals(stage, window.getPrimaryStage());
            assertEquals(900, stage.getWidth());
            assertEquals(700, stage.getHeight());
            assertEquals(120, stage.getX());
            assertEquals(80, stage.getY());
            window.fillInnerParts();
            assertNotNull(window.getPersonListPanel());
        });
    }

    @Test
    public void mainWindow_unrelatedKeys_doNotOpenHelp() throws Exception {
        FxTestUtil.run(() -> {
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, createLogic(new ModelManager()),
                    testFolder.resolve("contacts.json"));
            window.fillInnerParts();
            window.show();
            TextField input = (TextField) stage.getScene().lookup("#commandTextField");
            input.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F2,
                    false, false, false, false));
            stage.getScene().getRoot().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.F2,
                    false, false, false, false));
            assertTrue(Window.getWindows().stream().noneMatch(other ->
                    other instanceof Stage candidate && "Help".equals(candidate.getTitle())));
        });
    }

    private LogicManager createLogic(ModelManager model) {
        return new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(testFolder.resolve("contacts.json")),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json"))));
    }

    private static class AppWithUi extends MainApp {
        AppWithUi(Ui ui) {
            this.ui = ui;
        }
    }
}
