package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.logic.help.HelpPage;

public class HelpContentViewTest {
    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void overview_displaysAllCommandNamesAliasesAndShortSummaries() throws Exception {
        FxTestUtil.run(() -> {
            HelpContentView view = new HelpContentView();
            view.showPage(HelpCatalog.getOverview());
            List<Node> rows = ((Parent) view.getChildren().getFirst()).getChildrenUnmodifiable();
            assertEquals(14, rows.size());
            for (int i = 0; i < rows.size(); i++) {
                Parent row = (Parent) rows.get(i);
                Label command = (Label) row.getChildrenUnmodifiable().getFirst();
                assertEquals(HelpCatalog.getEntries().get(i).displayName(), command.getText());
                assertTrue(command.getStyleClass().contains("help-command"));
                assertTrue(visibleText(row).contains(HelpCatalog.getEntries().get(i).summary()));
            }
            assertTrue(visibleText(view).contains("list | ls"));
            assertTrue(visibleText(view).contains("help | ?"));
        });
    }

    @Test
    public void manuals_preserveEveryLineAndRenderCopyableUsageAndExamples() throws Exception {
        FxTestUtil.run(() -> {
            HelpContentView view = new HelpContentView();
            for (HelpPage page : HelpCatalog.getPages()) {
                view.showPage(page);
                String rendered = visibleText(view).replaceAll("\\s+", " ");
                for (String line : page.text().lines().toList()) {
                    String expected = line.strip().replaceFirst("^Usage: ", "").replaceFirst(":$", "")
                            .replaceAll("\\s+", " ");
                    assertTrue(rendered.contains(expected), page.title() + ": missing " + line);
                }
                assertEquals(1, view.lookupAll(".help-usage").size());
                assertEquals(page.text().split("Examples:\\n", 2)[1].lines().count(),
                        view.lookupAll(".help-example").size());
                assertTrue(view.lookupAll(".help-copy").size() >= 2);
            }
        });
    }

    @Test
    public void syntaxCommandsAndOptions_areBoldAndCyanAfterCss() throws Exception {
        FxTestUtil.run(() -> {
            HelpWindow window = new HelpWindow();
            window.showPage(AddCommand.HELP_PAGE);
            Parent root = window.getRoot().getScene().getRoot();
            root.applyCss();
            List<Text> tokens = root.lookupAll(".help-token").stream().map(Text.class::cast).toList();
            assertTrue(tokens.stream().anyMatch(token -> token.getText().equals("add")));
            assertTrue(tokens.stream().anyMatch(token -> token.getText().equals("--name")));
            for (Text token : tokens) {
                assertEquals(Color.web("#7dd3fc"), token.getFill());
                assertTrue(token.getFont().getStyle().contains("Bold"));
            }
        });
    }

    @Test
    public void copyButtons_copyExactUsageAndExampleText() throws Exception {
        FxTestUtil.run(() -> {
            HelpContentView view = new HelpContentView();
            view.showPage(HelpCommand.HELP_PAGE);
            Clipboard clipboard = Clipboard.getSystemClipboard();
            Map<DataFormat, Object> original = new HashMap<>();
            clipboard.getContentTypes().forEach(format -> {
                Object content = clipboard.getContent(format);
                if (content != null) {
                    original.put(format, content);
                }
            });
            try {
                Button copyUsage = (Button) view.lookup(".help-usage").lookup(".help-copy");
                copyUsage.fire();
                assertEquals("help [COMMAND]", clipboard.getString());
                Button copyExample = (Button) view.lookup(".help-example").lookup(".help-copy");
                copyExample.fire();
                assertEquals("help", clipboard.getString());
            } finally {
                clipboard.setContent(original);
            }
        });
    }

    @Test
    public void pageChanges_replaceContentAndPlainTextFallbackIsReadable() throws Exception {
        FxTestUtil.run(() -> {
            HelpContentView view = new HelpContentView();
            view.showPage(AddCommand.HELP_PAGE);
            view.showPage(HelpCatalog.getOverview());
            assertTrue(view.lookupAll(".help-copy").isEmpty());
            assertEquals(14, view.lookupAll(".help-command-row").size());
            view.showPage(new HelpPage("Other", "First line\nSecond line\n\nMore information."));
            assertTrue(visibleText(view).contains("First line\nSecond line"));
            assertTrue(visibleText(view).contains("More information."));
            assertFalse(visibleText(view).contains("Add a contact."));
            assertThrows(NullPointerException.class, () -> view.showPage(null));
        });
    }

    @Test
    public void layout_wrapsToViewportAtDefaultAndNarrowWidths() throws Exception {
        FxTestUtil.run(() -> {
            for (HelpPage page : List.of(HelpCatalog.getOverview(), AddCommand.HELP_PAGE)) {
                for (double width : List.of(530.0, 340.0)) {
                    HelpContentView content = new HelpContentView();
                    content.showPage(page);
                    ScrollPane scroll = new ScrollPane(content);
                    scroll.setFitToWidth(true);
                    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    scroll.getStylesheets().add(HelpContentView.class
                            .getResource("/view/HelpWindow.css").toExternalForm());
                    new Scene(scroll, width, 300);
                    scroll.resize(width, 300);
                    scroll.applyCss();
                    scroll.layout();
                    assertTrue(content.getWidth() <= scroll.getViewportBounds().getWidth() + 1);
                    assertTrue(content.getHeight() > scroll.getViewportBounds().getHeight());
                    for (Node block : content.getChildren()) {
                        assertTrue(block.getBoundsInParent().getMaxX() <= content.getWidth());
                    }
                }
            }
        });
    }

    @Test
    public void manual_usageAtEndOfPage_keepsAllSyntaxLines() throws Exception {
        FxTestUtil.run(() -> {
            HelpContentView view = new HelpContentView();
            view.showPage(new HelpPage("list", "Lists contacts.\nUsage: list\n       ls"));
            assertTrue(visibleText(view).contains("Lists contacts."));
            assertEquals(1, view.lookupAll(".help-usage").size());
            assertTrue(visibleText(view.lookup(".help-usage")).contains("list\n       ls"));
            assertEquals(List.of("Usage"), sectionHeadings(view));
        });
    }

    @Test
    public void manual_optionsAtEndOfPage_keepsOptionsAndHighlighting() throws Exception {
        FxTestUtil.run(() -> {
            HelpContentView view = new HelpContentView();
            view.showPage(new HelpPage("find", "Finds contacts.\nOptions:\n  -t, --tag TAG"));
            assertTrue(visibleText(view).contains("Finds contacts."));
            assertEquals(1, view.lookupAll(".help-options").size());
            assertTrue(visibleText(view.lookup(".help-options")).contains("-t, --tag TAG"));
            assertEquals(List.of("--tag", "-t"), view.lookupAll(".help-token").stream()
                    .map(Text.class::cast).map(Text::getText).sorted().toList());
            assertEquals(List.of("Options"), sectionHeadings(view));
        });
    }

    @Test
    public void manual_multipleNotesAndNotesAfterExamples_showsEveryParagraphAndOneNotesHeading() throws Exception {
        FxTestUtil.run(() -> {
            HelpContentView view = new HelpContentView();
            view.showPage(new HelpPage("list", "Usage: list\n\nFirst note.\n\nSecond note."
                    + "\n\nExamples:\n  list\n\nFinal note."));
            String rendered = visibleText(view);
            assertTrue(rendered.contains("First note."));
            assertTrue(rendered.contains("Second note."));
            assertTrue(rendered.contains("Final note."));
            assertEquals(1, view.lookupAll(".help-example").size());
            assertTrue(visibleText(view.lookup(".help-example")).contains("list"));
            assertEquals(List.of("Usage", "Notes", "Examples"), sectionHeadings(view));
        });
    }

    @Test
    public void manual_headingsImmediatelyAfterProse_remainSeparateSections() throws Exception {
        FxTestUtil.run(() -> {
            HelpContentView view = new HelpContentView();
            view.showPage(new HelpPage("find", "Finds contacts.\nOptions:\n  --tag TAG"
                    + "\n\nSearch notes.\nExamples:\n  find --tag client"));
            assertEquals(List.of("Options", "Examples"), sectionHeadings(view));
            List<Node> descriptions = view.getChildren().stream()
                    .filter(node -> node.getStyleClass().contains("help-description")).toList();
            assertEquals(List.of("Finds contacts.", "Search notes."),
                    descriptions.stream().map(HelpContentViewTest::visibleText).toList());
            assertTrue(visibleText(view.lookup(".help-options")).contains("--tag TAG"));
            assertTrue(visibleText(view.lookup(".help-example")).contains("find --tag client"));
        });
    }

    private static List<String> sectionHeadings(HelpContentView view) {
        return view.getChildren().stream().filter(node -> node.getStyleClass().contains("help-section-heading"))
                .map(Label.class::cast).map(Label::getText).toList();
    }

    private static String visibleText(Node node) {
        if (node instanceof Label label) {
            return label.getText();
        }
        if (node instanceof Text text) {
            return text.getText();
        }
        if (node instanceof Parent parent) {
            String delimiter = node instanceof TextFlow ? "" : "\n";
            return parent.getChildrenUnmodifiable().stream().map(HelpContentViewTest::visibleText)
                    .collect(Collectors.joining(delimiter));
        }
        return "";
    }
}
