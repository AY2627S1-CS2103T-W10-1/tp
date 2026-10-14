package seedu.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainAppTest {
    @TempDir
    public Path testFolder;

    @Test
    public void initPrefs_missingFile_createsDefaults() throws Exception {
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(testFolder.resolve("prefs.json"));
        UserPrefs prefs = new MainApp().initPrefs(storage);
        assertEquals(new UserPrefs(), prefs);
        assertEquals(prefs, storage.readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_existingFile_preservesSettings() throws Exception {
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(testFolder.resolve("prefs.json"));
        UserPrefs expected = new UserPrefs();
        expected.setGuiSettings(new GuiSettings(900, 700, 120, 80));
        storage.saveUserPrefs(expected);
        assertEquals(expected, new MainApp().initPrefs(storage));
        assertEquals(expected, storage.readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_corruptFile_replacesWithDefaults() throws Exception {
        Path path = testFolder.resolve("prefs.json");
        Files.writeString(path, "not JSON");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(path);
        assertEquals(new UserPrefs(), new MainApp().initPrefs(storage));
        assertEquals(new UserPrefs(), storage.readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_saveFailure_stillReturnsLoadedPreferences() throws Exception {
        Path path = testFolder.resolve("prefs.json");
        UserPrefs expected = new UserPrefs();
        expected.setGuiSettings(new GuiSettings(900, 700, 120, 80));
        new JsonUserPrefsStorage(path).saveUserPrefs(expected);
        JsonUserPrefsStorage blocked = new JsonUserPrefsStorage(path) {
            @Override
            public void saveUserPrefs(ReadOnlyUserPrefs prefs) throws IOException {
                throw new IOException("Simulated write failure");
            }
        };
        assertEquals(expected, new MainApp().initPrefs(blocked));
        assertEquals(expected, blocked.readUserPrefs().orElseThrow());
    }

    @Test
    public void stop_savesCurrentModelPreferences() throws Exception {
        MainApp app = createApp(new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
        app.model.setGuiSettings(new GuiSettings(900, 700, 120, 80));
        app.stop();
        assertEquals(app.model.getUserPrefs(), app.storage.readUserPrefs().orElseThrow());
        assertEquals(testFolder.resolve("prefs.json"), app.storage.getUserPrefsFilePath());
    }

    @Test
    public void stop_saveFailure_doesNotPreventShutdown() {
        boolean[] attempted = {false};
        JsonUserPrefsStorage blocked = new JsonUserPrefsStorage(testFolder.resolve("prefs.json")) {
            @Override
            public void saveUserPrefs(ReadOnlyUserPrefs prefs) throws IOException {
                attempted[0] = true;
                throw new IOException("Simulated write failure");
            }
        };
        assertDoesNotThrow(createApp(blocked)::stop);
        assertTrue(attempted[0]);
    }

    private MainApp createApp(JsonUserPrefsStorage prefsStorage) {
        MainApp app = new MainApp();
        app.model = new ModelManager();
        app.storage = new StorageManager(new JsonAddressBookStorage(testFolder.resolve("contacts.json")),
                prefsStorage);
        return app;
    }
}
