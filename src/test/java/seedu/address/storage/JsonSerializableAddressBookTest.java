package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Department;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON,
                dataFromFile::toModelType);
    }

    @Test
    public void toModelType_legacyData_importsContactDepartments() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(new PersonBuilder().withDepartment("Engineering").build());
        AddressBook restored = new JsonSerializableAddressBook(List.of(person), null).toModelType();
        assertEquals(List.of(new Department("Engineering")), restored.getDepartmentList());
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(restored));
        assertEquals(restored, JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType());
    }

    @Test
    public void toModelType_invalidCatalogue_rejected() {
        for (List<String> departments : List.of(List.of("Sales", "Sales"), List.of("!"),
                List.of("  "), Arrays.asList((String) null))) {
            assertThrows(IllegalValueException.class, () ->
                    new JsonSerializableAddressBook(List.of(), departments).toModelType());
        }
        JsonAdaptedPerson person = new JsonAdaptedPerson(new PersonBuilder().withDepartment("Engineering").build());
        assertThrows(IllegalValueException.class, () ->
                new JsonSerializableAddressBook(List.of(person), List.of()).toModelType());
    }

}
