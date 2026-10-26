package seedu.address.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
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
 * The Main Window. Lays out numbered panes in the style of a terminal dashboard: [1] departments, [2] contacts,
 * [3] details of the selected contact and [4] output with the command prompt.
 * {@code view}, Enter or a double-click on a contact also pops its details out into a window of its own,
 * one per contact, for side-by-side comparison.
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
    private ContactDetailsWindows contactDetailsWindows;

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
        addPaneShortcuts(primaryStage.getScene());
        trackFocusedPane();

        helpWindow = new HelpWindow();
        contactDetailsWindows = new ContactDetailsWindows(primaryStage, logic.getPersonList(),
                this::addPaneShortcuts, () -> focusInMainWindow(() -> commandBox.focus()));
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
     * Binds Alt+1 to Alt+4 to the numbered panes and Esc to the command prompt; Alt+3 also reaches the popups.
     * The same keys work in every contact details popup, so the keyboard never gets stuck in one.
     */
    private void addPaneShortcuts(Scene scene) {
        Map<KeyCombination, Runnable> shortcuts = Map.of(
                new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN), () ->
                        focusInMainWindow(() -> departmentPanel.focus()),
                new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN), () ->
                        focusInMainWindow(() -> personListPanel.focus()),
                new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN), () -> focusNextDetails(scene),
                new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN), () ->
                        focusInMainWindow(() -> commandBox.focus()),
                new KeyCodeCombination(KeyCode.ESCAPE), () -> focusInMainWindow(() -> commandBox.focus()));

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> shortcuts.entrySet().stream()
                .filter(entry -> entry.getKey().match(event))
                .findFirst()
                .ifPresent(entry -> {
                    entry.getValue().run();
                    event.consume();
                }));
    }

    private void focusInMainWindow(Runnable focusPane) {
        primaryStage.requestFocus();
        focusPane.run();
    }

    /**
     * Steps through every details view on repeated Alt+3: the [3] pane, then each popup, oldest first,
     * then back to the pane.
     */
    private void focusNextDetails(Scene scene) {
        boolean inMainWindow = scene == primaryStage.getScene();
        boolean inDetails = !inMainWindow || paneOf(scene.getFocusOwner()) == detailsPane;
        if (inDetails && contactDetailsWindows.focusAfter(scene.getWindow())) {
            return;
        }
        focusInMainWindow(() -> detailsPanel.focus());
    }

    /**
     * Highlights the pane that has the keyboard focus, and no pane while a popup has it.
     */
    private void trackFocusedPane() {
        Scene scene = primaryStage.getScene();
        InvalidationListener update = unused ->
                highlightPaneOf(primaryStage.isFocused() ? scene.getFocusOwner() : null);
        scene.focusOwnerProperty().addListener(update);
        primaryStage.focusedProperty().addListener(update);
    }

    private void highlightPaneOf(Node focusOwner) {
        panes().forEach(pane -> pane.getStyleClass().remove(FOCUSED_PANE_STYLE_CLASS));
        VBox focusedPane = paneOf(focusOwner);
        if (focusedPane != null) {
            focusedPane.getStyleClass().add(FOCUSED_PANE_STYLE_CLASS);
        }
    }

    private List<VBox> panes() {
        return List.of(departmentPane, contactPane, detailsPane, outputPane);
    }

    /**
     * Returns the numbered pane that contains {@code node}, or null if none does.
     */
    private VBox paneOf(Node node) {
        for (Node current = node; current != null; current = current.getParent()) {
            if (current instanceof VBox pane && panes().contains(pane)) {
                return pane;
            }
        }
        return null;
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        departmentPanel = new DepartmentPanel(logic.getDepartmentList(), logic.getPersonList());
        departmentPanelPlaceholder.getChildren().add(departmentPanel.getRoot());

        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        personListPanel.setOnViewRequested(this::viewContact);
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        detailsPanel = new DetailsPanel();
        detailsPanelPlaceholder.getChildren().add(detailsPanel.getRoot());
        // Selecting a contact shows it at once; view, Enter and double-click pop it out into a window instead.
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
        contactDetailsWindows.hideAll();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    ContactDetailsWindows getContactDetailsWindows() {
        return contactDetailsWindows;
    }

    /**
     * Runs {@code view} for the contact picked in [2], as if it had been typed at the prompt.
     */
    private void viewContact(int contactId) {
        try {
            executeCommand(ViewCommand.COMMAND_WORD + " " + contactId);
        } catch (CommandException | ParseException e) {
            logger.fine("Viewing the selected contact failed; the error is shown in the output pane.");
        }
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Command completed.");
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());

            if (commandResult.isShowHelp()) {
                helpWindow.showPage(commandResult.getHelpPage().orElse(HelpCatalog.getOverview()));
            }

            if (commandResult.isShowDetails()) {
                contactDetailsWindows.show(commandResult.getViewedPerson().orElseThrow(),
                        commandResult.getContactDetails().orElseThrow());
                // The popup stays in front of its owner, while typing carries on in the main window.
                primaryStage.requestFocus();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("Command execution failed: " + e.getClass().getSimpleName());
            resultDisplay.setErrorFeedback(CommandErrorFormatter.format(commandText, e.getMessage()));
            throw e;
        }
    }
}
