package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.ui.DepartmentPanel.DepartmentCount;

public class DepartmentPanelTest {
    @BeforeAll
    public static void setUpToolkit() throws Exception {
        FxTestUtil.initialize();
    }

    @Test
    public void rows_summariesFirstThenCatalogueEntries_refreshOnChange() throws Exception {
        FxTestUtil.run(() -> {
            Department engineering = new Department("Engineering");
            Department finance = new Department("Finance");
            ObservableList<Department> departments = FXCollections.observableArrayList(engineering, finance);
            ObservableList<Person> persons = FXCollections.observableArrayList(
                    new PersonBuilder().withName("Amy").withDepartment("Engineering").build(),
                    new PersonBuilder().withName("Bob").withDepartment("Engineering").build(),
                    new PersonBuilder().withName("Cat").build());

            DepartmentPanel panel = new DepartmentPanel(departments, persons);
            @SuppressWarnings("unchecked")
            ListView<DepartmentCount> list = (ListView<DepartmentCount>) panel.getRoot().lookup("#departmentListView");
            assertEquals(List.of(DepartmentCount.allContacts(3), DepartmentCount.noDepartment(1),
                    DepartmentCount.department("Engineering", 2), DepartmentCount.department("Finance", 0)),
                    list.getItems());
            assertEquals("All contacts", list.getItems().get(0).name());
            assertEquals("No department", list.getItems().get(1).name());

            persons.add(new PersonBuilder().withName("Dan").withDepartment("Finance").build());
            departments.remove(engineering);
            assertEquals(List.of(DepartmentCount.allContacts(4), DepartmentCount.noDepartment(1),
                    DepartmentCount.department("Finance", 1)), list.getItems());
        });
    }

    @Test
    public void rows_departmentNamedLikeSummary_isNotTreatedAsSummary() throws Exception {
        FxTestUtil.run(() -> {
            ObservableList<Department> departments =
                    FXCollections.observableArrayList(new Department(DepartmentPanel.NO_DEPARTMENT_ROW));
            ObservableList<Person> persons = FXCollections.observableArrayList(
                    new PersonBuilder().withName("Amy").withDepartment(DepartmentPanel.NO_DEPARTMENT_ROW).build());

            DepartmentPanel panel = new DepartmentPanel(departments, persons);
            @SuppressWarnings("unchecked")
            ListView<DepartmentCount> list = (ListView<DepartmentCount>) panel.getRoot().lookup("#departmentListView");
            assertEquals(List.of(DepartmentCount.allContacts(1), DepartmentCount.noDepartment(0),
                    DepartmentCount.department(DepartmentPanel.NO_DEPARTMENT_ROW, 1)), list.getItems());
        });
    }
}
