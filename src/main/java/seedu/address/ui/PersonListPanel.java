package seedu.address.ui;

import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.Person;

/**
 * Panel containing the list of persons.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private static final double SCROLL_BAR_ALLOWANCE = 16;
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.addEventFilter(KeyEvent.KEY_PRESSED, this::handleVimNavigation);
    }

    /**
     * Returns the contact currently selected with the keyboard, or null when none is selected.
     */
    public ReadOnlyObjectProperty<Person> selectedPersonProperty() {
        return personListView.getSelectionModel().selectedItemProperty();
    }

    /**
     * Clears the keyboard selection, e.g. after another contact is shown with {@code view}.
     */
    public void clearSelection() {
        personListView.getSelectionModel().clearSelection();
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

    /**
     * Keeps a row's preferred width within the list so the list never scrolls sideways.
     * The cell still stretches the row to its full width, as the header row fills the panel.
     */
    private void fitToList(Region row) {
        row.prefWidthProperty().bind(personListView.widthProperty().subtract(SCROLL_BAR_ALLOWANCE));
    }

    /**
     * Maps unmodified j/k to Down/Up so the list keeps its native selection and scrolling behaviour.
     */
    private void handleVimNavigation(KeyEvent event) {
        if (event.isAltDown() || event.isControlDown() || event.isMetaDown() || event.isShiftDown()
                || (event.getCode() != KeyCode.J && event.getCode() != KeyCode.K)) {
            return;
        }
        KeyCode arrow = event.getCode() == KeyCode.J ? KeyCode.DOWN : KeyCode.UP;
        event.consume();
        Event.fireEvent(personListView, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", arrow,
                false, false, false, false));
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                Region row = new PersonCard(person, getIndex() + 1).getRoot();
                fitToList(row);
                setGraphic(row);
            }
        }
    }

}
