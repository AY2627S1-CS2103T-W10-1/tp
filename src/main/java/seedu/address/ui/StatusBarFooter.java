package seedu.address.ui;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/**
 * A UI for the status bar that is displayed at the footer of the application.
 * It lists the keyboard shortcuts, how many contacts are displayed, and where the data is saved.
 */
public class StatusBarFooter extends UiPart<Region> {

    private static final String FXML = "StatusBarFooter.fxml";

    @FXML
    private Label contactCount;

    @FXML
    private Label saveLocationStatus;

    /**
     * Creates a {@code StatusBarFooter} for the given save location and contact lists.
     *
     * @param saveLocation Data file shown in the footer.
     * @param displayedPersons Currently displayed contacts.
     * @param allPersons Every stored contact.
     */
    public StatusBarFooter(Path saveLocation, ObservableList<Person> displayedPersons,
            ObservableList<Person> allPersons) {
        super(FXML);
        saveLocationStatus.setText(Paths.get(".").resolve(saveLocation).toString());
        contactCount.textProperty().bind(Bindings.createStringBinding(() ->
                displayedPersons.size() + " shown / " + allPersons.size() + " total",
                displayedPersons, allPersons));
    }

}
