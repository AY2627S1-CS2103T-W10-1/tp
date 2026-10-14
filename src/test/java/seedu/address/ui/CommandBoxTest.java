package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

public class CommandBoxTest {
    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void enter_emptyCommand_doesNotExecute() throws Exception {
        FxTestUtil.run(() -> {
            CommandBox box = new CommandBox(command -> {
                throw new AssertionError("Empty input must not be executed");
            });
            TextField input = (TextField) box.getRoot().lookup("#commandTextField");
            input.fireEvent(new ActionEvent());
            assertEquals("", input.getText());
            assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void enter_repeatedFailure_marksErrorOnceAndEditingClearsIt() throws Exception {
        FxTestUtil.run(() -> {
            AtomicInteger executions = new AtomicInteger();
            CommandBox box = new CommandBox(command -> {
                executions.incrementAndGet();
                throw new ParseException("Unknown command");
            });
            TextField input = (TextField) box.getRoot().lookup("#commandTextField");
            input.setText("invalid");
            input.fireEvent(new ActionEvent());
            input.fireEvent(new ActionEvent());
            assertEquals(2, executions.get());
            assertEquals("invalid", input.getText());
            assertEquals(1L, input.getStyleClass().stream().filter(CommandBox.ERROR_STYLE_CLASS::equals).count());
            input.setText("help");
            assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }

    @Test
    public void enter_executionFailureRetainsInputAndSuccessClearsIt() throws Exception {
        FxTestUtil.run(() -> {
            CommandBox box = new CommandBox(command -> {
                if (command.equals("delete 99")) {
                    throw new CommandException("Invalid index");
                }
                return new CommandResult("success");
            });
            TextField input = (TextField) box.getRoot().lookup("#commandTextField");
            input.setText("delete 99");
            input.fireEvent(new ActionEvent());
            assertEquals("delete 99", input.getText());
            assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            input.setText("help");
            input.fireEvent(new ActionEvent());
            assertEquals("", input.getText());
            assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
        });
    }
}
