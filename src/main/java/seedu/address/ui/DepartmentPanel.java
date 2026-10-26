package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;

/**
 * Sidebar listing every registered department with the number of contacts assigned to it.
 * Two summary rows come first, set apart from the departments: every stored contact, and contacts without a
 * department. Counts cover all stored contacts, not only the currently displayed ones, and refresh whenever
 * the catalogue or the contacts change.
 */
public class DepartmentPanel extends UiPart<Region> {

    public static final String ALL_CONTACTS_ROW = "All contacts";
    public static final String NO_DEPARTMENT_ROW = "No department";
    public static final String SUMMARY_STYLE_CLASS = "department-summary";
    public static final String DIVIDER_STYLE_CLASS = "department-divider";
    private static final String FXML = "DepartmentPanel.fxml";
    private static final double SCROLL_BAR_ALLOWANCE = 16;

    private final ObservableList<Department> departments;
    private final ObservableList<Person> persons;
    private final ObservableList<DepartmentCount> rows = FXCollections.observableArrayList();

    @FXML
    private ListView<DepartmentCount> departmentListView;

    /**
     * Creates a {@code DepartmentPanel} that tracks the given catalogue and contacts.
     * Both lists are retained so that their change listeners stay registered.
     */
    public DepartmentPanel(ObservableList<Department> departments, ObservableList<Person> persons) {
        super(FXML);
        this.departments = requireNonNull(departments);
        this.persons = requireNonNull(persons);
        departmentListView.setItems(rows);
        departmentListView.setCellFactory(listView -> new DepartmentCountCell());
        VimKeys.install(departmentListView);
        departments.addListener((ListChangeListener<Department>) unused -> refresh());
        persons.addListener((ListChangeListener<Person>) unused -> refresh());
        refresh();
    }

    /**
     * Gives the department list keyboard focus so it can be scrolled with Up/Down or j/k.
     */
    public void focus() {
        departmentListView.requestFocus();
    }

    private void refresh() {
        Map<Department, Long> counts = persons.stream()
                .flatMap(person -> person.getDepartment().stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        long unassigned = persons.stream().filter(person -> person.getDepartment().isEmpty()).count();

        List<DepartmentCount> updated = new ArrayList<>();
        updated.add(DepartmentCount.allContacts(persons.size()));
        updated.add(DepartmentCount.noDepartment(unassigned));
        departments.forEach(department ->
                updated.add(DepartmentCount.department(department.value, counts.getOrDefault(department, 0L))));
        rows.setAll(updated);
    }

    /**
     * Kinds of sidebar row. The kind, not the name, marks a summary row, since a department may be named
     * like one.
     */
    public enum Kind { ALL_CONTACTS, NO_DEPARTMENT, DEPARTMENT }

    /**
     * One sidebar row: a summary or department name and its contact count.
     */
    public record DepartmentCount(String name, long count, Kind kind) {
        public static DepartmentCount allContacts(long count) {
            return new DepartmentCount(ALL_CONTACTS_ROW, count, Kind.ALL_CONTACTS);
        }

        public static DepartmentCount noDepartment(long count) {
            return new DepartmentCount(NO_DEPARTMENT_ROW, count, Kind.NO_DEPARTMENT);
        }

        public static DepartmentCount department(String name, long count) {
            return new DepartmentCount(name, count, Kind.DEPARTMENT);
        }
    }

    /**
     * Shows the name on the left and the count right-aligned.
     * Summary rows are dimmed, and the last one is underlined to divide them from the departments.
     */
    private static class DepartmentCountCell extends ListCell<DepartmentCount> {
        @Override
        protected void updateItem(DepartmentCount row, boolean empty) {
            super.updateItem(row, empty);
            setText(null);
            if (empty || row == null) {
                setGraphic(null);
                return;
            }

            Label name = new Label(row.name());
            name.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(name, Priority.ALWAYS);
            Label count = new Label(String.valueOf(row.count()));
            count.setMinWidth(Region.USE_PREF_SIZE);
            count.getStyleClass().add("department-count");
            HBox line = new HBox(8, name, count);
            line.getStyleClass().add("department-row");
            // Fit the row to the list so a long name is truncated instead of hiding the count.
            line.prefWidthProperty().bind(getListView().widthProperty().subtract(SCROLL_BAR_ALLOWANCE));
            if (row.kind() != Kind.DEPARTMENT) {
                line.getStyleClass().add(SUMMARY_STYLE_CLASS);
            }
            if (row.kind() == Kind.NO_DEPARTMENT) {
                line.getStyleClass().add(DIVIDER_STYLE_CLASS);
            }
            setGraphic(line);
        }
    }
}
