package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class DetailsPanelTest {
    private static final String DETAILS = "Name: Amy Bee\nPhone: 85355255\nEmail: Not provided\n"
            + "Address: 123, Jurong West Ave 6, #08-111\n\n\nDepartment: R&D\nTags: None";

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void showDetails_buildsGreyNamesAndDimmedEmptyValues() throws Exception {
        FxTestUtil.run(() -> {
            DetailsPanel panel = new DetailsPanel();
            GridPane grid = grid(panel);
            assertEquals(DetailsPanel.PLACEHOLDER, ((Label) grid.getChildren().getFirst()).getText());

            panel.showDetails(DETAILS);
            assertEquals(List.of("Name", "Phone", "Email", "Address", "Department", "Tags"),
                    labels(grid).stream().map(Label::getText).toList());
            List<TextArea> values = values(grid);
            assertEquals(List.of("Amy Bee", "85355255", "Not provided", "123, Jurong West Ave 6, #08-111", "R&D",
                    "None"), values.stream().map(TextArea::getText).toList());
            assertEquals(List.of(false, false, true, false, false, true), values.stream()
                    .map(value -> value.getStyleClass().contains(DetailsPanel.EMPTY_STYLE_CLASS)).toList());
            values.forEach(value -> {
                assertFalse(value.isEditable());
                assertTrue(value.isWrapText());
            });
            assertEquals(DETAILS, grid.getAccessibleText());

            panel.showDetails("Name: Bob");
            assertEquals(List.of("Bob"), values(grid).stream().map(TextArea::getText).toList());
            assertThrows(NullPointerException.class, () -> panel.showDetails(null));
            assertEquals("Name: Bob", grid.getAccessibleText());
        });
    }

    @Test
    public void keyboard_movesBetweenValuesAndSelectsThemForCopying() throws Exception {
        FxTestUtil.run(() -> {
            DetailsPanel panel = new DetailsPanel();
            Stage stage = new Stage();
            stage.setScene(new Scene(panel.getRoot(), 500, 400));
            stage.show();
            panel.showDetails(DETAILS);
            List<TextArea> values = values(grid(panel));
            Scene scene = stage.getScene();

            panel.focus();
            assertEquals(values.get(0), scene.getFocusOwner());
            assertEquals("Amy Bee", values.get(0).getSelectedText());

            press(scene, KeyCode.J, false);
            press(scene, KeyCode.TAB, false);
            assertEquals(values.get(2), scene.getFocusOwner());
            assertEquals("Not provided", values.get(2).getSelectedText());
            assertEquals("", values.get(0).getSelectedText());
            assertEquals("", values.get(1).getSelectedText());

            press(scene, KeyCode.K, false);
            press(scene, KeyCode.TAB, true);
            assertEquals(values.get(0), scene.getFocusOwner());
            press(scene, KeyCode.K, false);
            assertEquals(values.get(0), scene.getFocusOwner());
        });
    }

    /** Returns the grid inside the scroll pane, which a lookup cannot reach before the pane is shown. */
    private static GridPane grid(DetailsPanel panel) {
        return (GridPane) ((ScrollPane) panel.getRoot()).getContent();
    }

    private static List<Label> labels(GridPane grid) {
        return grid.getChildren().stream().filter(node -> node.getStyleClass().contains(DetailsPanel.LABEL_STYLE_CLASS))
                .map(Label.class::cast).toList();
    }

    private static List<TextArea> values(GridPane grid) {
        return grid.getChildren().stream().filter(node -> node.getStyleClass().contains(DetailsPanel.VALUE_STYLE_CLASS))
                .map(TextArea.class::cast).toList();
    }

    private static void press(Scene scene, KeyCode key, boolean shift) {
        Node target = scene.getFocusOwner();
        Event.fireEvent(target, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", key, shift, false, false, false));
    }
}
