package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    @Test
    public void history_successfulCommands_restoreDraftWithoutExecutingWhileBrowsing() throws Exception {
        FxTestUtil.run(() -> {
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("success");
            });
            TextField input = (TextField) box.getRoot().lookup("#commandTextField");
            submit(input, "list");
            submit(input, "sort --by tags");
            input.setText("find Alice ");
            arrow(input, KeyCode.UP);
            assertEquals("sort --by tags", input.getText());
            assertEquals(input.getLength(), input.getCaretPosition());
            arrow(input, KeyCode.UP);
            assertEquals("list", input.getText());
            arrow(input, KeyCode.UP);
            assertEquals("list", input.getText());
            arrow(input, KeyCode.DOWN);
            assertEquals("sort --by tags", input.getText());
            arrow(input, KeyCode.DOWN);
            assertEquals("find Alice ", input.getText());
            assertEquals(input.getLength(), input.getCaretPosition());
            arrow(input, KeyCode.DOWN);
            assertEquals("find Alice ", input.getText());
            assertEquals(List.of("list", "sort --by tags"), executed);
        });
    }

    @Test
    public void history_failedCommands_areRecordedIncludingDuplicatesAndClearErrorOnRecall() throws Exception {
        FxTestUtil.run(() -> {
            AtomicInteger executions = new AtomicInteger();
            CommandBox box = new CommandBox(command -> {
                executions.incrementAndGet();
                if (command.equals("bad")) {
                    throw new ParseException("Unknown command");
                }
                if (command.equals("delete 99")) {
                    throw new CommandException("Invalid index");
                }
                return new CommandResult("success");
            });
            TextField input = (TextField) box.getRoot().lookup("#commandTextField");
            submit(input, "list");
            submit(input, "bad");
            assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            arrow(input, KeyCode.UP);
            assertEquals("bad", input.getText());
            assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            submit(input, "delete 99");
            input.fireEvent(new ActionEvent());
            assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            arrow(input, KeyCode.UP);
            assertEquals("delete 99", input.getText());
            assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            arrow(input, KeyCode.UP);
            assertEquals("delete 99", input.getText());
            arrow(input, KeyCode.UP);
            assertEquals("bad", input.getText());
            arrow(input, KeyCode.UP);
            assertEquals("list", input.getText());
            assertEquals(4, executions.get());
        });
    }

    @Test
    public void history_editingRecalledInput_doesNotRewriteHistoryAndResubmissionAddsEntry() throws Exception {
        FxTestUtil.run(() -> {
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("success");
            });
            TextField input = (TextField) box.getRoot().lookup("#commandTextField");
            submit(input, "list");
            submit(input, "sort --by tags");
            arrow(input, KeyCode.UP);
            input.setText("find Alice");
            arrow(input, KeyCode.UP);
            assertEquals("list", input.getText());
            arrow(input, KeyCode.DOWN);
            assertEquals("sort --by tags", input.getText());
            submit(input, "find Bob");
            assertEquals("", input.getText());
            arrow(input, KeyCode.UP);
            assertEquals("find Bob", input.getText());
            arrow(input, KeyCode.UP);
            assertEquals("sort --by tags", input.getText());
            arrow(input, KeyCode.UP);
            assertEquals("list", input.getText());
            assertEquals(List.of("list", "sort --by tags", "find Bob"), executed);
        });
    }

    @Test
    public void history_modifiedArrowsAndOtherKeys_areNotIntercepted() throws Exception {
        FxTestUtil.run(() -> {
            CommandBox box = new CommandBox(command -> new CommandResult("success"));
            TextField input = (TextField) box.getRoot().lookup("#commandTextField");
            submit(input, "list");
            input.setText("draft");
            AtomicInteger passedThrough = new AtomicInteger();
            input.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (!event.isConsumed()) {
                    passedThrough.incrementAndGet();
                }
            });
            for (KeyCode code : List.of(KeyCode.UP, KeyCode.DOWN)) {
                for (int modifier = 0; modifier < 4; modifier++) {
                    input.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                            modifier == 0, modifier == 1, modifier == 2, modifier == 3));
                    assertEquals("draft", input.getText());
                }
            }
            arrow(input, KeyCode.F2);
            assertEquals(9, passedThrough.get());
            arrow(input, KeyCode.UP);
            assertEquals("list", input.getText());
            arrow(input, KeyCode.DOWN);
            assertEquals("draft", input.getText());
            assertEquals(9, passedThrough.get());
        });
    }

    @Test
    public void history_emptyHistory_preservesInputAndCaret() throws Exception {
        FxTestUtil.run(() -> {
            CommandBox box = new CommandBox(command -> {
                throw new AssertionError("Browsing must not execute input");
            });
            TextField input = (TextField) box.getRoot().lookup("#commandTextField");
            input.setText("draft");
            input.positionCaret(2);
            arrow(input, KeyCode.UP);
            arrow(input, KeyCode.DOWN);
            assertEquals("draft", input.getText());
            assertEquals(2, input.getCaretPosition());
        });
    }

    @Test
    public void history_whitespaceFailure_isNotStoredAndRemainsAvailableAsDraft() throws Exception {
        FxTestUtil.run(() -> {
            AtomicInteger executions = new AtomicInteger();
            CommandBox box = new CommandBox(command -> {
                executions.incrementAndGet();
                if (command.isBlank()) {
                    throw new ParseException("Invalid command");
                }
                return new CommandResult("success");
            });
            TextField input = (TextField) box.getRoot().lookup("#commandTextField");
            submit(input, "list");
            submit(input, "  ");
            assertTrue(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            arrow(input, KeyCode.UP);
            assertEquals("list", input.getText());
            arrow(input, KeyCode.UP);
            assertEquals("list", input.getText());
            arrow(input, KeyCode.DOWN);
            assertEquals("  ", input.getText());
            assertEquals(2, executions.get());
        });
    }

    private static void submit(TextField input, String command) {
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }

    private static void arrow(TextField input, KeyCode code) {
        input.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

}
