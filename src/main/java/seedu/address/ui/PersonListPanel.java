package seedu.address.ui;

import java.util.function.IntConsumer;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons, shown as a table with one row per contact.
 * A row grows taller when its tags wrap onto further lines.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    /** Room for the vertical scroll bar, so rows never make the list scroll sideways. */
    private static final double SCROLL_BAR_ALLOWANCE = 14;
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    private IntConsumer viewRequestHandler = unused -> { };

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        VimKeys.install(personListView);
        personListView.addEventFilter(KeyEvent.KEY_PRESSED, this::handleEnter);
    }

    /**
     * Sets what happens when Enter is pressed on a selected row, or a row is double-clicked;
     * the handler receives the row's contact ID.
     */
    public void setOnViewRequested(IntConsumer handler) {
        viewRequestHandler = handler;
    }

    /**
     * Returns the contact selected with a click or the keyboard, or null when none is selected.
     */
    public ReadOnlyObjectProperty<Person> selectedPersonProperty() {
        return personListView.getSelectionModel().selectedItemProperty();
    }

    /**
     * Gives the list keyboard focus, selecting the first contact if none is selected yet,
     * so that the next Up/Down (or k/j) press moves from a visible row.
     */
    public void focus() {
        personListView.requestFocus();
        if (personListView.getSelectionModel().isEmpty() && !personListView.getItems().isEmpty()) {
            personListView.getSelectionModel().selectFirst();
        }
    }

    private void handleEnter(KeyEvent event) {
        int selected = personListView.getSelectionModel().getSelectedIndex();
        if (event.getCode() != KeyCode.ENTER || selected < 0) {
            return;
        }
        event.consume();
        viewRequestHandler.accept(selected + 1);
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        PersonListViewCell() {
            setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !isEmpty()) {
                    viewRequestHandler.accept(getIndex() + 1);
                }
            });
        }

        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                Region row = new PersonCard(person, getIndex() + 1).getRoot();
                row.prefWidthProperty().bind(personListView.widthProperty().subtract(SCROLL_BAR_ALLOWANCE));
                setGraphic(row);
            }
        }
    }

}
