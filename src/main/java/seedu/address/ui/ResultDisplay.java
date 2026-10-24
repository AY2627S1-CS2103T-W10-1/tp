package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 * It always shows a few lines, and grows with longer feedback up to a limit, so errors and their examples
 * are not cut off.
 */
public class ResultDisplay extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    /** Short feedback still gets this many lines, so the output pane does not jump in size after every command. */
    public static final int MIN_ROWS = 3;
    public static final int MAX_ROWS = 8;

    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    public ResultDisplay() {
        super(FXML);
    }

    /**
     * Displays feedback from its beginning so the original error precedes any command examples.
     *
     * @param feedbackToUser Feedback to show in the result box.
     * @throws NullPointerException If {@code feedbackToUser} is null.
     */
    public void setFeedbackToUser(String feedbackToUser) {
        show(feedbackToUser);
        resultDisplay.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    /**
     * Displays feedback for a failed command, highlighted as an error.
     *
     * @param feedbackToUser Error and examples to show in the result box.
     * @throws NullPointerException If {@code feedbackToUser} is null.
     */
    public void setErrorFeedback(String feedbackToUser) {
        show(feedbackToUser);
        if (!resultDisplay.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            resultDisplay.getStyleClass().add(ERROR_STYLE_CLASS);
        }
    }

    private void show(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        resultDisplay.setText(feedbackToUser);
        resultDisplay.setPrefRowCount((int) Math.clamp(feedbackToUser.lines().count(), MIN_ROWS, MAX_ROWS));
        resultDisplay.positionCaret(0);
        resultDisplay.setScrollTop(0);
        resultDisplay.setScrollLeft(0);
    }

}
