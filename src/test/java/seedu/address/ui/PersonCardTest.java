package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class PersonCardTest {
    /** Narrow enough that the other columns leave the tags column at its minimum width. */
    private static final double NARROW_ROW_WIDTH = 640;

    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void tags_narrowRow_wrapOntoFurtherLinesWithoutCuttingAnyOff() throws Exception {
        FxTestUtil.run(() -> {
            Region oneTag = layOut(new PersonBuilder().withTags("friends").build());
            Region manyTags = layOut(new PersonBuilder()
                    .withTags("classmates", "colleagues", "family", "neighbours").build());

            assertTrue(manyTags.getHeight() > 1.5 * oneTag.getHeight(), "The row should grow to fit wrapped tags.");
            FlowPane tags = (FlowPane) manyTags.lookup("#tags");
            List<Label> labels = tags.getChildren().stream().map(Label.class::cast).toList();
            assertEquals(List.of("classmates", "colleagues", "family", "neighbours"),
                    labels.stream().map(Label::getText).toList());
            labels.forEach(label -> {
                assertTrue(label.getWidth() >= label.prefWidth(-1), label.getText() + " should not be shortened.");
                assertTrue(label.getBoundsInParent().getMaxX() <= tags.getWidth(),
                        label.getText() + " should stay inside the tags column.");
            });
        });
    }

    @Test
    public void tags_tagWiderThanColumn_isShortenedToFit() throws Exception {
        FxTestUtil.run(() -> {
            Region row = layOut(new PersonBuilder().withTags("a_very_long_tag_name_for_tests").build());

            FlowPane tags = (FlowPane) row.lookup("#tags");
            Label tag = (Label) tags.getChildren().getFirst();
            assertTrue(tag.getWidth() < tag.prefWidth(-1), "The tag should be shortened with an ellipsis.");
            assertTrue(tag.getWidth() <= tags.getWidth(), "The tag should stay inside the tags column.");
        });
    }

    /**
     * Lays out {@code person}'s row at {@link #NARROW_ROW_WIDTH} with the app's stylesheet, which sets the
     * column widths, and returns the row at its preferred height.
     */
    private static Region layOut(Person person) {
        Region row = new PersonCard(person, 1).getRoot();
        VBox container = new VBox(row);
        Scene scene = new Scene(container, NARROW_ROW_WIDTH, 400);
        scene.getStylesheets().add("view/DarkTheme.css");
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
        // Wrapped tags change the row's height after the first pass, so lay it out again.
        for (int pass = 0; pass < 3; pass++) {
            container.applyCss();
            container.layout();
        }
        return row;
    }
}
