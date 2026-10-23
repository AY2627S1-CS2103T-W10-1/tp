package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

/**
 * Docked pane showing a snapshot of one contact's full details as a grid of field names and values.
 * Values can be selected and copied: in the pane, j/k (or Tab, Up/Down) move between values, and moving to a
 * value selects it so Ctrl+C copies it. The snapshot changes only when another contact is viewed or selected.
 */
public class DetailsPanel extends UiPart<Region> {

    public static final String PLACEHOLDER = "Run view CONTACT_ID, or select a contact in [2].";
    public static final String LABEL_STYLE_CLASS = "details-label";
    public static final String VALUE_STYLE_CLASS = "details-value";
    public static final String EMPTY_STYLE_CLASS = "details-empty";

    private static final String FXML = "DetailsPanel.fxml";
    private static final String FIELD_SEPARATOR = ": ";
    /** Values that mean a field was not provided; they are dimmed like the table's placeholder. */
    private static final Set<String> EMPTY_VALUES = Set.of("Not provided", "None");
    private static final double GAP_HEIGHT = 8;
    /** Keeps the last wrapped line clear of the value's bottom edge. */
    private static final double VALUE_HEIGHT_ALLOWANCE = 2;

    private final List<TextArea> values = new ArrayList<>();

    @FXML
    private ScrollPane detailsScroll;

    @FXML
    private GridPane contactDetails;

    /**
     * Creates a details pane that shows a hint until the first contact is displayed.
     */
    public DetailsPanel() {
        super(FXML);
        Label placeholder = new Label(PLACEHOLDER);
        placeholder.setWrapText(true);
        placeholder.getStyleClass().add(LABEL_STYLE_CLASS);
        contactDetails.add(placeholder, 0, 0, 2, 1);
    }

    /**
     * Replaces the displayed details and scrolls back to the top.
     * Each {@code "Field: value"} line becomes a row and each blank line a gap between groups of fields.
     * The original text stays available as the grid's accessible text.
     */
    public void showDetails(String details) {
        requireNonNull(details);
        contactDetails.getChildren().clear();
        values.clear();

        int row = 0;
        for (String line : details.lines().toList()) {
            if (line.isBlank()) {
                Region gap = new Region();
                gap.setMinHeight(GAP_HEIGHT);
                contactDetails.add(gap, 0, row++, 2, 1);
                continue;
            }
            int separator = line.indexOf(FIELD_SEPARATOR);
            if (separator < 0) {
                contactDetails.add(createValue(line), 0, row++, 2, 1);
                continue;
            }
            Label name = new Label(line.substring(0, separator));
            name.getStyleClass().add(LABEL_STYLE_CLASS);
            name.setMinWidth(Region.USE_PREF_SIZE);
            GridPane.setValignment(name, VPos.TOP);
            contactDetails.addRow(row++, name, createValue(line.substring(separator + FIELD_SEPARATOR.length())));
        }

        contactDetails.setAccessibleText(details);
        detailsScroll.setVvalue(0);
    }

    /**
     * Gives the first value keyboard focus, or the pane itself while no contact is shown.
     */
    public void focus() {
        if (values.isEmpty()) {
            detailsScroll.requestFocus();
        } else {
            values.getFirst().requestFocus();
        }
    }

    /**
     * Creates a read-only, wrapping value whose height follows its text, so the grid never scrolls inside a value.
     */
    private TextArea createValue(String value) {
        TextArea field = new TextArea(value);
        field.setEditable(false);
        field.setWrapText(true);
        field.setPrefRowCount(1);
        field.setMinHeight(Region.USE_PREF_SIZE);
        field.getStyleClass().add(VALUE_STYLE_CLASS);
        if (EMPTY_VALUES.contains(value)) {
            field.getStyleClass().add(EMPTY_STYLE_CLASS);
        }
        field.skinProperty().addListener((unused, oldSkin, newSkin) -> fitHeightToText(field));
        // Only the focused value stays selected, so the highlight shows which value Ctrl+C copies.
        field.focusedProperty().addListener((unused, wasFocused, isFocused) -> {
            if (isFocused) {
                field.selectAll();
            } else {
                field.deselect();
            }
        });
        field.addEventFilter(KeyEvent.KEY_PRESSED, event -> handleValueNavigation(field, event));
        values.add(field);
        return field;
    }

    private static void fitHeightToText(TextArea field) {
        Node text = field.lookup(".text");
        if (text == null) {
            return;
        }
        field.prefHeightProperty().bind(Bindings.createDoubleBinding(() ->
                text.getBoundsInLocal().getHeight() + VALUE_HEIGHT_ALLOWANCE, text.boundsInLocalProperty()));
    }

    /**
     * Moves to the next value on j, Down or Tab, and to the previous one on k, Up or Shift+Tab.
     * Shift with the other keys is left to the value, so part of it can still be selected.
     */
    private void handleValueNavigation(TextArea field, KeyEvent event) {
        if (event.isAltDown() || event.isControlDown() || event.isMetaDown()) {
            return;
        }
        int offset = switch (event.getCode()) {
            case TAB -> event.isShiftDown() ? -1 : 1;
            case J, DOWN -> event.isShiftDown() ? 0 : 1;
            case K, UP -> event.isShiftDown() ? 0 : -1;
            default -> 0;
        };
        if (offset == 0) {
            return;
        }
        event.consume();
        int target = values.indexOf(field) + offset;
        if (target >= 0 && target < values.size()) {
            values.get(target).requestFocus();
        }
    }
}
