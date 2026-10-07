package seedu.address.ui;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.stage.Window;

/**
 * Runs interface tests on the JavaFX thread using the platform's display.
 */
final class FxTestUtil {

    private static boolean initialized;

    private FxTestUtil() {
    }

    static synchronized void initialize() throws Exception {
        assumeTrue(!System.getProperty("os.name").toLowerCase().contains("linux")
                || System.getenv("DISPLAY") != null, "Run GUI tests under xvfb-run on headless Linux.");
        if (!initialized) {
            CountDownLatch started = new CountDownLatch(1);
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                started.countDown();
            });
            if (!started.await(15, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX startup timed out.");
            }
            initialized = true;
        }
    }

    static void run(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            try {
                action.run();
            } finally {
                Window.getWindows().stream().toList().forEach(Window::hide);
            }
        }, null);
        Platform.runLater(task);
        task.get(15, TimeUnit.SECONDS);
    }
}
