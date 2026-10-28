package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * Shows a snapshot of one contact's full details as a grid of field names and values, in the docked [3] pane
 * and in each details popup. Values can be selected and copied: j/k (or Tab, Up/Down) move between values,
 * and moving to a value selects it so Ctrl+C copies it.
 */
public class DetailsPanel extends UiPart<Region> {

    public static final String PLACEHOLDER = "Click a contact in [2], or select it with j/k, to see it here.";
    public static final String LABEL_STYLE_CLASS = "details-label";
    public static final String VALUE_STYLE_CLASS = "details-value";
    public static final String EMPTY_STYLE_CLASS = "details-empty";

    private static final String FXML = "DetailsPanel.fxml";
    private static final double GAP_HEIGHT = 8;
    /** Keeps the last wrapped line clear of the value's bottom edge. */
    private static final double VALUE_HEIGHT_ALLOWANCE = 2;

    private final List<TextArea> values = new ArrayList<>();

    /** One displayed field, with presence tracked separately from its text. */
    private record DetailField(String name, String value, boolean isProvided) { }

    @FXML
    private ScrollPane detailsScroll;

    @FXML
    private GridPane contactDetails;

    /**
     * Creates a details panel that shows a hint until the first contact is displayed.
     */
    public DetailsPanel() {
        super(FXML);
        Label placeholder = new Label(PLACEHOLDER);
        placeholder.setWrapText(true);
        placeholder.getStyleClass().add(LABEL_STYLE_CLASS);
        contactDetails.add(placeholder, 0, 0, 2, 1);
        detailsScroll.addEventFilter(KeyEvent.KEY_PRESSED, this::handlePanelNavigation);
    }

    /**
     * Replaces the displayed details with a snapshot of {@code person} and scrolls back to the top.
     * Missing fields are dimmed based on their presence, independently of the text they display.
     * A text description of the fields is also available to assistive technology.
     */
    public void showDetails(Person person) {
        requireNonNull(person);
        contactDetails.getChildren().clear();
        values.clear();

        List<DetailField> contactFields = List.of(
                new DetailField("Name", person.getName().fullName, true),
                new DetailField("Phone", person.getPhone().toString(), person.getPhone().isProvided()),
                new DetailField("Email", person.getEmail().toString(), person.getEmail().isProvided()),
                new DetailField("Address", person.getAddress().toString(), person.getAddress().isProvided()));
        String tags = person.getTags().stream().map(tag -> tag.tagName).sorted().collect(Collectors.joining(", "));
        List<DetailField> organizationFields = List.of(
                new DetailField("Department", person.getDepartment().map(department -> department.value)
                        .orElse("Not provided"), person.getDepartment().isPresent()),
                new DetailField("Tags", tags.isEmpty() ? "None" : tags, !person.getTags().isEmpty()));

        String contactText = addFields(contactFields, 0);
        // Preserve the two blank rows separating contact information from department and tags.
        for (int row = contactFields.size(); row < contactFields.size() + 2; row++) {
            Region gap = new Region();
            gap.setMinHeight(GAP_HEIGHT);
            contactDetails.add(gap, 0, row, 2, 1);
        }
        String organizationText = addFields(organizationFields, contactFields.size() + 2);
        contactDetails.setAccessibleText(contactText + "\n\n\n" + organizationText);
        detailsScroll.setVvalue(0);
    }

    /**
     * Adds a group of fields starting at {@code firstRow} and returns their text description.
     */
    private String addFields(List<DetailField> fields, int firstRow) {
        int row = firstRow;
        for (DetailField field : fields) {
            Label name = new Label(field.name());
            name.getStyleClass().add(LABEL_STYLE_CLASS);
            name.setMinWidth(Region.USE_PREF_SIZE);
            GridPane.setValignment(name, VPos.TOP);
            contactDetails.addRow(row++, name, createValue(field.value(), field.isProvided()));
        }
        return fields.stream().map(field -> field.name() + ": " + field.value()).collect(Collectors.joining("\n"));
    }

    /**
     * Gives the first value keyboard focus, or the panel itself while no contact is shown.
     */
    public void focus() {
        if (values.isEmpty()) {
            detailsScroll.requestFocus();
        } else {
            focusValue(values.getFirst());
        }
    }

    /**
     * Selects the value even when its window is still gaining native focus or it already owns scene focus.
     */
    private void focusValue(TextArea value) {
        values.forEach(TextArea::deselect);
        value.requestFocus();
        value.selectAll();
    }

    /**
     * Creates a read-only, wrapping value whose height follows its text, so the grid never scrolls inside a value.
     */
    private TextArea createValue(String value, boolean isProvided) {
        TextArea field = new TextArea(value);
        field.setEditable(false);
        field.setWrapText(true);
        field.setPrefRowCount(1);
        field.setMinHeight(Region.USE_PREF_SIZE);
        field.getStyleClass().add(VALUE_STYLE_CLASS);
        if (!isProvided) {
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
        // The value's own scroll pane is not skinned yet, so a lookup cannot pass through it; its content can.
        Node text = field.lookup(".scroll-pane") instanceof ScrollPane pane && pane.getContent() != null
                ? pane.getContent().lookup(".text")
                : null;
        if (text == null) {
            return;
        }
        field.prefHeightProperty().bind(Bindings.createDoubleBinding(() ->
                text.getBoundsInLocal().getHeight() + VALUE_HEIGHT_ALLOWANCE, text.boundsInLocalProperty()));
    }

    /**
     * Moves to the first value on an unmodified j or k while the panel itself has focus, as it does
     * when a popup is activated or empty space is clicked, so j/k work however the panel was reached.
     */
    private void handlePanelNavigation(KeyEvent event) {
        if (event.getTarget() != detailsScroll || values.isEmpty()
                || event.isAltDown() || event.isControlDown() || event.isMetaDown() || event.isShiftDown()
                || (event.getCode() != KeyCode.J && event.getCode() != KeyCode.K)) {
            return;
        }
        event.consume();
        focusValue(values.getFirst());
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
            focusValue(values.get(target));
        }
    }
}
