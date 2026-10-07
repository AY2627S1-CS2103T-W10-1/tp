package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

class DepartmentTest {
    @Test
    void constructor_invalidDepartment_throwsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> new Department(null));
        assertThrows(IllegalArgumentException.class, () -> new Department("!"));
    }

    @Test
    void isValidDepartment() {
        assertFalse(Department.isValidDepartment(null));
        assertFalse(Department.isValidDepartment("A"));
        assertFalse(Department.isValidDepartment("Engineering!"));
        assertTrue(Department.isValidDepartment("R&D-42"));
    }

    @Test
    void valueObjectMethods() {
        Department department = new Department("Engineering");
        assertEquals("Engineering", department.toString());
        assertTrue(department.equals(new Department("Engineering")));
        assertTrue(department.equals(department));
        assertFalse(department.equals(null));
        assertFalse(department.equals("Engineering"));
        assertFalse(department.equals(new Department("Sales")));
        assertEquals(department.hashCode(), new Department("Engineering").hashCode());
    }
}
