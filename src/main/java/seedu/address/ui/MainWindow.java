package seedu.address.ui;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ViewCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.help.HelpCatalog;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * The Main Window. Lays out numbered panes, in the style of a terminal dashboard:
 * [1] departments, [2] contacts, [3] details and [4] output with the command prompt.
 */
public class MainWindow extends UiPart<Stage> {

    public static final String FOCUSED_PANE_STYLE_CLASS = "pane-focused";

    private static final String FXML = "MainWindow.fxml";

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private DepartmentPanel departmentPanel;
    private PersonListPanel personListPanel;
    private DetailsPanel detailsPanel;
    private ResultDisplay resultDisplay;
    private CommandBox commandBox;
    private HelpWindow helpWindow;

    @FXML
    private VBox departmentPane;

    @FXML
    private VBox contactPane;

    @FXML
    private VBox detailsPane;

    @FXML
    private VBox outputPane;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane departmentPanelPlaceholder;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane detailsPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();
        trackFocusedPane();

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add the following event filter to capture such key events and open
         * the help window purposely so as to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Highlights the pane that contains the keyboard focus.
     */
    private void trackFocusedPane() {
        getRoot().getScene().focusOwnerProperty().addListener((unused, oldOwner, newOwner) ->
                highlightPaneOf(newOwner));
    }

    private void highlightPaneOf(Node focusOwner) {
        List<VBox> panes = List.of(departmentPane, contactPane, detailsPane, outputPane);
        panes.forEach(pane -> pane.getStyleClass().remove(FOCUSED_PANE_STYLE_CLASS));
        for (Node node = focusOwner; node != null; node = node.getParent()) {
            if (panes.contains(node)) {
                node.getStyleClass().add(FOCUSED_PANE_STYLE_CLASS);
                return;
            }
        }
    }

    /**
     * Binds Alt+1 to Alt+4 to the numbered panes, and Esc to the command prompt.
     */
    private void setPaneShortcuts() {
        Map<KeyCombination, Runnable> focusActions = new LinkedHashMap<>();
        focusActions.put(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN), departmentPanel::focus);
        focusActions.put(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN), personListPanel::focus);
        focusActions.put(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN), detailsPanel::focus);
        focusActions.put(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN), commandBox::focus);
        focusActions.put(new KeyCodeCombination(KeyCode.ESCAPE), commandBox::focus);

        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> focusActions.entrySet().stream()
                .filter(entry -> entry.getKey().match(event))
                .findFirst()
                .ifPresent(entry -> {
                    entry.getValue().run();
                    event.consume();
                }));
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        departmentPanel = new DepartmentPanel(logic.getDepartmentList(), logic.getPersonList());
        departmentPanelPlaceholder.getChildren().add(departmentPanel.getRoot());

        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        detailsPanel = new DetailsPanel();
        detailsPanelPlaceholder.getChildren().add(detailsPanel.getRoot());
        personListPanel.selectedPersonProperty().addListener((unused, oldPerson, newPerson) -> {
            if (newPerson != null) {
                detailsPanel.showDetails(ViewCommand.formatDetails(newPerson));
            }
        });

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath, logic.getFilteredPersonList(),
                logic.getPersonList());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());

        setPaneShortcuts();
        // The prompt, not the first pane in traversal order, receives the initial focus.
        commandBox.focus();
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    /**
     * Shows the command overview in the existing help window.
     */
    @FXML
    public void handleHelp() {
        helpWindow.showPage(HelpCatalog.getOverview());
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());

            if (commandResult.isShowHelp()) {
                helpWindow.showPage(commandResult.getHelpPage().orElse(HelpCatalog.getOverview()));
            }

            commandResult.getContactDetails().ifPresent(details -> {
                personListPanel.clearSelection();
                detailsPanel.showDetails(details);
            });

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.setErrorFeedback(CommandErrorFormatter.format(commandText, e.getMessage()));
            throw e;
        }
    }
}
