package seedu.address.ui;

import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * A UI component that displays a {@code Person} as one row of the contact table.
 * The address is left out to keep rows short; {@code view} shows every field.
 */
public class PersonCard extends UiPart<Region> {

    /** Shown in place of a field that was not provided. */
    public static final String EMPTY_VALUE = "—";
    public static final String EMPTY_STYLE_CLASS = "cell-empty";
    public static final String TAG_STYLE_CLASS = "tag";

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label department;
    @FXML
    private FlowPane tags;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(String.valueOf(displayedIndex));
        name.setText(person.getName().fullName);
        showValue(phone, person.getPhone().isProvided(), person.getPhone().value);
        showValue(email, person.getEmail().isProvided(), person.getEmail().value);
        showValue(department, person.getDepartment().isPresent(),
                person.getDepartment().map(value -> value.value).orElse(""));

        if (person.getTags().isEmpty()) {
            Label none = new Label(EMPTY_VALUE);
            none.getStyleClass().add(EMPTY_STYLE_CLASS);
            tags.getChildren().add(none);
        }
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> {
                    Label label = new Label(tag.tagName);
                    label.getStyleClass().add(TAG_STYLE_CLASS);
                    // A tag wider than the column is shortened with an ellipsis instead of being cut off.
                    label.maxWidthProperty().bind(tags.widthProperty());
                    tags.getChildren().add(label);
                });
    }

    /**
     * Shows {@code value}, or a dimmed placeholder when the field was not provided.
     */
    private static void showValue(Label label, boolean isProvided, String value) {
        label.setText(isProvided ? value : EMPTY_VALUE);
        if (!isProvided) {
            label.getStyleClass().add(EMPTY_STYLE_CLASS);
        }
    }
}
