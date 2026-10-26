package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class PersonListPanelTest {

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void focus_selectsFirstContactOnlyWhenNoneIsSelected() throws Exception {
        FxTestUtil.run(() -> {
            PersonListPanel empty = new PersonListPanel(FXCollections.observableArrayList());
            ListView<?> emptyList = show(empty);
            empty.focus();
            assertSame(emptyList, emptyList.getScene().getFocusOwner());
            assertTrue(emptyList.getSelectionModel().isEmpty());

            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(ALICE, BENSON));
            ListView<?> list = show(panel);
            list.getSelectionModel().select(1);
            panel.focus();
            assertEquals(1, list.getSelectionModel().getSelectedIndex());
        });
    }

    @Test
    public void viewRequests_onlyForSelectedOrDoubleClickedContact() throws Exception {
        FxTestUtil.run(() -> {
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(ALICE, BENSON));
            List<Integer> requested = new ArrayList<>();
            panel.setOnViewRequested(requested::add);
            ListView<?> list = show(panel);

            press(list, KeyCode.ENTER);
            assertTrue(requested.isEmpty(), "Enter with no contact selected should do nothing.");
            list.getSelectionModel().select(1);
            press(list, KeyCode.ENTER);
            assertEquals(List.of(2), requested);

            click(cell(list, false), MouseButton.SECONDARY);
            click(cell(list, true), MouseButton.PRIMARY);
            assertEquals(List.of(2), requested, "Only a primary double-click on a contact should view it.");
        });
    }

    /** Shows the panel in a window tall enough to leave empty rows below the contacts. */
    private static ListView<?> show(PersonListPanel panel) {
        Stage stage = new Stage();
        stage.setScene(new Scene(panel.getRoot(), 700, 300));
        stage.show();
        return (ListView<?>) panel.getRoot().lookup("#personListView");
    }

    private static ListCell<?> cell(ListView<?> list, boolean empty) {
        return list.lookupAll(".list-cell").stream()
                .map(ListCell.class::cast)
                .filter(cell -> cell.isEmpty() == empty)
                .findFirst().orElseThrow();
    }

    private static void press(Node target, KeyCode code) {
        Event.fireEvent(target, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

    private static void click(Node target, MouseButton button) {
        Event.fireEvent(target, new MouseEvent(MouseEvent.MOUSE_CLICKED, 5, 5, 5, 5, button,
                2, false, false, false, false, button == MouseButton.PRIMARY, false,
                button == MouseButton.SECONDARY, false, false, true, null));
    }
}
