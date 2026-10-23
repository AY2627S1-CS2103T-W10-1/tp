package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

/**
 * A UI component that displays the result of a command execution.
 * It grows with its feedback, up to {@link #MAX_VISIBLE_ROWS} lines, so errors and their examples are not clipped.
 */
public class ResultDisplay extends UiPart<Region> {

    public static final String ERROR_STYLE_CLASS = "error";
    static final int MAX_VISIBLE_ROWS = 8;
    private static final String FXML = "ResultDisplay.fxml";

    @FXML
    private TextArea resultDisplay;

    /**
     * Creates an empty one-line result display.
     */
    public ResultDisplay() {
        super(FXML);
        resultDisplay.setPrefRowCount(1);
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
     * @param feedbackToUser Error feedback to show in the result box.
     * @throws NullPointerException If {@code feedbackToUser} is null.
     */
    public void setErrorFeedback(String feedbackToUser) {
        show(feedbackToUser);
        ObservableList<String> styleClass = resultDisplay.getStyleClass();
        if (!styleClass.contains(ERROR_STYLE_CLASS)) {
            styleClass.add(ERROR_STYLE_CLASS);
        }
    }

    private void show(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        resultDisplay.setText(feedbackToUser);
        int lineCount = (int) feedbackToUser.lines().count();
        resultDisplay.setPrefRowCount(Math.max(1, Math.min(MAX_VISIBLE_ROWS, lineCount)));
        resultDisplay.positionCaret(0);
        resultDisplay.setScrollTop(0);
        resultDisplay.setScrollLeft(0);
    }

}
