package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.person.Person;

public class PartialContactStorageTest {
    @TempDir
    public Path folder;

    @Test
    public void read_omittedNullOrEmptyOptionalFields_returnsPartialContacts() throws Exception {
        for (String fields : List.of("", ",\"phone\":null,\"email\":null,\"address\":null",
                ",\"phone\":\"\",\"email\":\"\",\"address\":\"\"")) {
            Person person = readPerson(fields);
            assertFalse(person.getPhone().isProvided());
            assertFalse(person.getEmail().isProvided());
            assertFalse(person.getAddress().isProvided());
        }
    }

    @Test
    public void read_existingShortPhoneAndLongTag_preservesLegacyData() throws Exception {
        Person person = readPerson(",\"phone\":\"911\",\"tags\":[\"" + "a".repeat(31) + "\"]");
        assertEquals("911", person.getPhone().value);
        assertEquals("a".repeat(31), person.getTags().iterator().next().tagName);
    }

    @Test
    public void read_invalidSuppliedFields_stillRejectsCorruptedData() {
        for (String fields : List.of(",\"phone\":\"abc\"", ",\"email\":\"broken\"", ",\"address\":\" \"",
                ",\"department\":\"!\"", ",\"tags\":[\"bad tag\"]")) {
            assertThrows(DataLoadingException.class, () -> readPerson(fields));
        }
    }

    private Person readPerson(String fields) throws Exception {
        Path path = folder.resolve("contacts.json");
        Files.writeString(path, "{\"persons\":[{\"name\":\"Alice\"" + fields + "}]}");
        return new JsonAddressBookStorage(path).readAddressBook().orElseThrow().getPersonList().getFirst();
    }
}
