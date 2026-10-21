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
import seedu.address.model.person.Person;
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

    @Test
    public void toModelType_catalogueWithoutPersons_preservesUnusedDepartments() throws Exception {
        String json = "{\"departments\": [\"Engineering\"]}";
        AddressBook restored = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        assertEquals(List.of(), restored.getPersonList());
        assertEquals(List.of(new Department("Engineering")), restored.getDepartmentList());
        String saved = JsonUtil.toJsonString(new JsonSerializableAddressBook(restored));
        assertEquals(restored, JsonUtil.fromJsonString(saved, JsonSerializableAddressBook.class).toModelType());
    }

    @Test
    public void toModelType_paddedLegacyNames_normalizesAndMergesWithoutLosingContacts() throws Exception {
        String json = """
                {
                  "departments": [" abc ", "abc", " Customer  Support ", "ABC"],
                  "persons": [
                    {"name": "Alice", "department": " abc "},
                    {"name": "Bob", "department": "abc"}
                  ]
                }
                """;
        AddressBook restored = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        assertEquals(List.of(new Department("abc"), new Department("Customer  Support"), new Department("ABC")),
                restored.getDepartmentList());
        assertEquals(2, restored.getPersonList().size());
        restored.getPersonList().forEach(person ->
                assertEquals("abc", person.getDepartment().orElseThrow().value));
        String saved = JsonUtil.toJsonString(new JsonSerializableAddressBook(restored));
        assertEquals(restored, JsonUtil.fromJsonString(saved, JsonSerializableAddressBook.class).toModelType());
        String legacy = "{\"persons\": [{\"name\": \"Alice\", \"department\": \" abc \"}]}";
        AddressBook imported = JsonUtil.fromJsonString(legacy, JsonSerializableAddressBook.class).toModelType();
        assertEquals(List.of(new Department("abc")), imported.getDepartmentList());
        assertEquals("abc", imported.getPersonList().getFirst().getDepartment().orElseThrow().value);
    }

    @Test
    public void toModelType_sameNameAndEitherMatchingContactField_preservesLegacyContacts() throws Exception {
        Person first = new PersonBuilder().build();
        for (Person second : List.of(new PersonBuilder(first).withPhone("91234567").build(),
                new PersonBuilder(first).withEmail("other@example.com").build())) {
            JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                    List.of(new JsonAdaptedPerson(first), new JsonAdaptedPerson(second)), null);
            assertEquals(List.of(first, second), data.toModelType().getPersonList());
        }
    }

}
