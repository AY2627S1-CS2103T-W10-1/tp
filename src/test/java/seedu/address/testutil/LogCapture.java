package seedu.address.testutil;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import seedu.address.commons.core.LogsCenter;

/**
 * Captures formatted application logs at every level and restores logging on close.
 */
public class LogCapture implements AutoCloseable {
    private final Logger logger = LogsCenter.getLogger(LogCapture.class).getParent();
    private final Level previousLevel = logger.getLevel();
    private final Handler[] previousHandlers = logger.getHandlers();
    private final StringBuilder output = new StringBuilder();
    private final Handler handler = new Handler() {
        private final SimpleFormatter formatter = new SimpleFormatter();

        @Override
        public void publish(LogRecord record) {
            output.append(formatter.format(record));
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };

    /**
     * Replaces application handlers temporarily so even debug output is captured without writing to disk.
     */
    public LogCapture() {
        for (Handler previous : previousHandlers) {
            logger.removeHandler(previous);
        }
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
    }

    public String getOutput() {
        return output.toString();
    }

    @Override
    public void close() {
        logger.removeHandler(handler);
        logger.setLevel(previousLevel);
        for (Handler previous : previousHandlers) {
            logger.addHandler(previous);
        }
    }
}
