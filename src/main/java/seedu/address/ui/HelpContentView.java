package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.logic.help.HelpEntry;
import seedu.address.logic.help.HelpPage;

/**
 * Renders help as native JavaFX text, command rows and copyable syntax blocks.
 * Existing manual headings delimit presentation sections; command syntax is never interpreted or executed.
 */
public final class HelpContentView extends VBox {
    private static final String USAGE_PREFIX = "Usage: ";
    private static final Pattern CODE_TOKEN = Pattern.compile(
            "^(?:[a-z]+|\\?)(?=\\s|$)|(?<![\\w-])--?[a-z][a-z-]*\\b", Pattern.MULTILINE);
    private static final Pattern OPTION_TOKEN = Pattern.compile("(?<![\\w-])--?[a-z][a-z-]*\\b");

    /**
     * Creates a wrapping help content container.
     */
    public HelpContentView() {
        getStyleClass().add("help-content");
        setSpacing(12);
    }

    /**
     * Replaces the current content with the overview or formatted command manual.
     * Must be called on the JavaFX Application Thread.
     *
     * @param page Help page to render.
     * @throws NullPointerException If {@code page} is null.
     */
    public void showPage(HelpPage page) {
        requireNonNull(page);
        getChildren().clear();
        setAccessibleText(page.text());
        if (page.equals(HelpCatalog.getOverview())) {
            renderOverview();
        } else {
            renderManual(page.text());
        }
    }

    private void renderOverview() {
        VBox commands = new VBox(10);
        commands.getStyleClass().add("help-command-list");
        for (HelpEntry entry : HelpCatalog.getEntries()) {
            Label name = new Label(entry.displayName());
            name.getStyleClass().add("help-command");
            name.setMinWidth(110);
            name.setPrefWidth(110);
            name.setWrapText(true);
            TextFlow summary = text(entry.summary(), "help-summary");
            HBox.setHgrow(summary, Priority.ALWAYS);
            HBox row = new HBox(12, name, summary);
            row.getStyleClass().add("help-command-row");
            commands.getChildren().add(row);
        }
        getChildren().addAll(commands, text(HelpCatalog.getOverviewGuidance(), "help-guidance"));
    }

    private void renderManual(String manual) {
        List<String> lines = manual.lines().toList();
        int index = 0;
        boolean hasUsage = false;
        boolean hasNotesHeading = false;
        while (index < lines.size()) {
            String line = lines.get(index);
            if (line.isBlank()) {
                index++;
                continue;
            }
            if (line.startsWith(USAGE_PREFIX)) {
                List<String> usage = new ArrayList<>();
                usage.add(line.substring(USAGE_PREFIX.length()));
                index++;
                while (index < lines.size() && !lines.get(index).isBlank()) {
                    usage.add(lines.get(index++));
                }
                getChildren().addAll(heading("Usage"), codeBlock(String.join("\n", usage), "usage"));
                hasUsage = true;
            } else if (line.equals("Options:")) {
                getChildren().add(heading("Options"));
                index++;
                VBox options = new VBox(6);
                options.getStyleClass().add("help-options");
                while (index < lines.size() && !lines.get(index).isBlank()) {
                    options.getChildren().add(highlighted(lines.get(index++).stripLeading(), OPTION_TOKEN));
                }
                getChildren().add(options);
            } else if (line.equals("Examples:")) {
                getChildren().add(heading("Examples"));
                index++;
                while (index < lines.size() && !lines.get(index).isBlank()) {
                    getChildren().add(codeBlock(lines.get(index++).stripLeading(), "example"));
                }
            } else {
                if (hasUsage && !hasNotesHeading) {
                    getChildren().add(heading("Notes"));
                    hasNotesHeading = true;
                }
                List<String> paragraph = new ArrayList<>();
                while (index < lines.size() && !lines.get(index).isBlank()
                        && !lines.get(index).startsWith(USAGE_PREFIX)
                        && !lines.get(index).equals("Options:") && !lines.get(index).equals("Examples:")) {
                    paragraph.add(lines.get(index++));
                }
                getChildren().add(text(String.join("\n", paragraph), "help-description"));
            }
        }
    }

    private static Label heading(String title) {
        Label label = new Label(title);
        label.getStyleClass().add("help-section-heading");
        return label;
    }

    private static HBox codeBlock(String content, String kind) {
        TextFlow syntax = highlighted(content, CODE_TOKEN);
        syntax.getStyleClass().add("help-code-text");
        HBox.setHgrow(syntax, Priority.ALWAYS);
        Button copy = new Button("Copy");
        copy.getStyleClass().add("help-copy");
        copy.setMinWidth(Button.USE_PREF_SIZE);
        copy.setAccessibleText("Copy " + kind);
        copy.setTooltip(new Tooltip("Copy " + kind));
        copy.setOnAction(event -> {
            ClipboardContent clipboard = new ClipboardContent();
            clipboard.putString(content);
            Clipboard.getSystemClipboard().setContent(clipboard);
        });
        HBox block = new HBox(10, syntax, copy);
        block.getStyleClass().addAll("help-code-block", "help-" + kind);
        return block;
    }

    private static TextFlow highlighted(String content, Pattern pattern) {
        TextFlow flow = new TextFlow();
        flow.setMinWidth(0);
        flow.setMinHeight(Region.USE_PREF_SIZE);
        flow.setAccessibleText(content);
        Matcher matcher = pattern.matcher(content);
        int end = 0;
        while (matcher.find()) {
            flow.getChildren().add(new Text(content.substring(end, matcher.start())));
            Text token = new Text(matcher.group());
            token.getStyleClass().add("help-token");
            flow.getChildren().add(token);
            end = matcher.end();
        }
        flow.getChildren().add(new Text(content.substring(end)));
        return flow;
    }

    private static TextFlow text(String content, String styleClass) {
        TextFlow flow = new TextFlow(new Text(content));
        flow.setMinWidth(0);
        flow.setMinHeight(Region.USE_PREF_SIZE);
        flow.setAccessibleText(content);
        flow.getStyleClass().add(styleClass);
        return flow;
    }
}
