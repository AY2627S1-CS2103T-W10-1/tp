package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

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
import seedu.address.model.person.Address;
import seedu.address.model.person.Department;
import seedu.address.model.person.Email;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

public class DetailsPanelTest {
    private static final Person PERSON = new Person(new PersonBuilder().build().getName(), new Phone("85355255"),
            Email.notProvided(), new Address(PersonBuilder.DEFAULT_ADDRESS), Set.of(),
            new Department("R&D"));
    private static final String DETAILS = "Name: Amy Bee\nPhone: 85355255\nEmail: Not provided\n"
            + "Address: 123, Jurong West Ave 6, #08-111\n\n\nDepartment: R&D\nTags: None";
    private static final String LONG_ADDRESS = "Blk 123, Jurong West Avenue 6, #08-111, Singapore 640123, "
            + "beside the community club";

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

            panel.showDetails(PERSON);
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

            Person bob = new PersonBuilder().withName("Bob").withTags("vendor", "client").build();
            panel.showDetails(bob);
            assertEquals(List.of("Bob", PersonBuilder.DEFAULT_PHONE, PersonBuilder.DEFAULT_EMAIL,
                    PersonBuilder.DEFAULT_ADDRESS, "Not provided", "client, vendor"),
                    values(grid).stream().map(TextArea::getText).toList());
            String bobDetails = grid.getAccessibleText();
            assertThrows(NullPointerException.class, () -> panel.showDetails(null));
            assertEquals(bobDetails, grid.getAccessibleText());
        });
    }

    @Test
    public void showDetails_valuesMatchingPlaceholders_areNotDimmed() throws Exception {
        FxTestUtil.run(() -> {
            DetailsPanel panel = new DetailsPanel();
            for (String text : List.of("None", "Not provided")) {
                Person person = new PersonBuilder().withName(text).withAddress(text).withDepartment(text)
                        .withTags("None").build();
                panel.showDetails(person);

                List<TextArea> fields = values(grid(panel));
                assertEquals(List.of(text, PersonBuilder.DEFAULT_PHONE, PersonBuilder.DEFAULT_EMAIL, text, text,
                        "None"), fields.stream().map(TextArea::getText).toList());
                fields.forEach(field -> assertFalse(field.getStyleClass().contains(DetailsPanel.EMPTY_STYLE_CLASS)));
            }
        });
    }

    @Test
    public void showDetails_missingFields_areDimmed() throws Exception {
        FxTestUtil.run(() -> {
            DetailsPanel panel = new DetailsPanel();
            Person defaults = new PersonBuilder().build();
            panel.showDetails(new Person(defaults.getName(), Phone.notProvided(), defaults.getEmail(),
                    Address.notProvided(), Set.of()));

            List<TextArea> fields = values(grid(panel));
            assertEquals(List.of("Amy Bee", "Not provided", PersonBuilder.DEFAULT_EMAIL, "Not provided",
                    "Not provided", "None"), fields.stream().map(TextArea::getText).toList());
            assertEquals(List.of(false, true, false, true, true, true), fields.stream()
                    .map(field -> field.getStyleClass().contains(DetailsPanel.EMPTY_STYLE_CLASS)).toList());
        });
    }

    @Test
    public void clearDetails_restoresPlaceholderAndResetsNavigationAndScroll() throws Exception {
        FxTestUtil.run(() -> {
            DetailsPanel panel = new DetailsPanel();
            Stage stage = new Stage();
            stage.setScene(new Scene(panel.getRoot(), 500, 400));
            stage.show();
            Scene scene = stage.getScene();
            ScrollPane scroll = (ScrollPane) panel.getRoot();
            panel.showDetails(PERSON);
            scroll.setVvalue(1);

            panel.clearDetails();
            panel.clearDetails();

            assertEquals(1, grid(panel).getChildren().size());
            assertEquals(DetailsPanel.PLACEHOLDER, ((Label) grid(panel).getChildren().getFirst()).getText());
            assertTrue(values(grid(panel)).isEmpty());
            assertNull(grid(panel).getAccessibleText());
            assertEquals(0, scroll.getVvalue());
            panel.focus();
            assertEquals(scroll, scene.getFocusOwner());
            press(scene, KeyCode.J, false);
            press(scene, KeyCode.K, false);
            assertEquals(scroll, scene.getFocusOwner());

            panel.showDetails(new PersonBuilder().withName("Bob").withTags().build());
            panel.focus();
            assertEquals("Bob", ((TextArea) scene.getFocusOwner()).getSelectedText());
            assertEquals("Name: Bob\nPhone: " + PersonBuilder.DEFAULT_PHONE
                    + "\nEmail: " + PersonBuilder.DEFAULT_EMAIL
                    + "\nAddress: " + PersonBuilder.DEFAULT_ADDRESS
                    + "\n\n\nDepartment: Not provided\nTags: None",
                    grid(panel).getAccessibleText());
        });
    }

    @Test
    public void keyboard_movesBetweenValuesAndSelectsThemForCopying() throws Exception {
        FxTestUtil.run(() -> {
            DetailsPanel panel = new DetailsPanel();
            Stage stage = new Stage();
            stage.setScene(new Scene(panel.getRoot(), 500, 400));
            stage.show();
            panel.showDetails(PERSON);
            List<TextArea> values = values(grid(panel));
            Scene scene = stage.getScene();

            panel.focus();
            assertEquals(values.get(0), scene.getFocusOwner());
            assertEquals("Amy Bee", values.get(0).getSelectedText());
            values.get(0).selectRange(0, 3);
            panel.focus();
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

    @Test
    public void keyboard_modifiedKeysAndLastValue_keepFocus() throws Exception {
        FxTestUtil.run(() -> {
            DetailsPanel panel = new DetailsPanel();
            Stage stage = new Stage();
            stage.setScene(new Scene(panel.getRoot(), 500, 400));
            stage.show();
            panel.showDetails(PERSON);
            List<TextArea> values = values(grid(panel));
            Scene scene = stage.getScene();
            panel.focus();

            // Ctrl, Alt and Meta shortcuts, and Shift with j/k, are left to the value.
            for (int modifier = 0; modifier < 3; modifier++) {
                Event.fireEvent(scene.getFocusOwner(), new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.J,
                        false, modifier == 0, modifier == 1, modifier == 2));
                assertEquals(values.get(0), scene.getFocusOwner());
            }
            press(scene, KeyCode.J, true);
            press(scene, KeyCode.K, true);
            assertEquals(values.get(0), scene.getFocusOwner());

            values.getLast().requestFocus();
            press(scene, KeyCode.J, false);
            assertEquals(values.getLast(), scene.getFocusOwner());
        });
    }

    @Test
    public void keyboard_jkWhilePanelHasFocus_movesToFirstValue() throws Exception {
        FxTestUtil.run(() -> {
            DetailsPanel panel = new DetailsPanel();
            Stage stage = new Stage();
            stage.setScene(new Scene(panel.getRoot(), 500, 400));
            stage.show();
            Scene scene = stage.getScene();

            // With only the placeholder there is no value to move to.
            panel.getRoot().requestFocus();
            press(scene, KeyCode.J, false);
            assertEquals(panel.getRoot(), scene.getFocusOwner());

            panel.showDetails(PERSON);
            List<TextArea> values = values(grid(panel));
            // Shift, Ctrl, Alt or Meta with j, and keys other than j/k, are left to the panel.
            for (int modifier = 0; modifier < 4; modifier++) {
                Event.fireEvent(panel.getRoot(), new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.J,
                        modifier == 0, modifier == 1, modifier == 2, modifier == 3));
                assertEquals(panel.getRoot(), scene.getFocusOwner());
            }
            press(scene, KeyCode.A, false);
            assertEquals(panel.getRoot(), scene.getFocusOwner());

            press(scene, KeyCode.J, false);
            assertEquals(values.get(0), scene.getFocusOwner());
            assertEquals("Amy Bee", values.get(0).getSelectedText());
            press(scene, KeyCode.J, false);
            assertEquals(values.get(1), scene.getFocusOwner());

            panel.getRoot().requestFocus();
            press(scene, KeyCode.K, false);
            assertEquals(values.get(0), scene.getFocusOwner());
        });
    }

    @Test
    public void showDetails_longValueGrowsToShowEveryWrappedLine() throws Exception {
        FxTestUtil.run(() -> {
            DetailsPanel panel = new DetailsPanel();
            Stage stage = new Stage();
            stage.setScene(new Scene(panel.getRoot(), 360, 400));
            stage.show();
            panel.showDetails(new PersonBuilder().withAddress(LONG_ADDRESS).build());
            // Wrapping changes the text's height after the first layout, so the value needs further passes.
            for (int pass = 0; pass < 3; pass++) {
                panel.getRoot().applyCss();
                panel.getRoot().layout();
            }

            List<TextArea> values = values(grid(panel));
            TextArea name = values.get(0);
            TextArea address = values.get(3);
            double addressTextHeight = address.lookup(".text").getBoundsInLocal().getHeight();
            assertTrue(addressTextHeight > 1.5 * name.lookup(".text").getBoundsInLocal().getHeight(),
                    "The address should wrap onto more than one line.");
            assertTrue(address.getHeight() >= addressTextHeight, "Every wrapped line should be visible.");
            assertTrue(address.getHeight() > 1.5 * name.getHeight(),
                    "The wrapped value should be taller than a one-line value.");
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
