package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<String> departments;

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("departments") List<String> departments) {
        if (persons != null) {
            this.persons.addAll(persons);
        }
        this.departments = departments == null ? null : new ArrayList<>(departments);
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        departments = source.getDepartmentList().stream().map(department -> department.value)
                .collect(Collectors.toList());
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        if (departments != null) {
            for (String name : departments) {
                if (!Department.isValidDepartment(name)) {
                    throw new IllegalValueException(Department.MESSAGE_CONSTRAINTS);
                }
                Department department = new Department(name);
                if (addressBook.getDepartmentList().contains(department)) {
                    throw new IllegalValueException("Department catalogue contains duplicates.");
                }
                addressBook.addDepartment(department);
            }
        }
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            if (departments != null && person.getDepartment().isPresent()
                    && !addressBook.getDepartmentList().contains(person.getDepartment().get())) {
                throw new IllegalValueException("Contact department is absent from the catalogue.");
            }
            addressBook.addPerson(person);
        }
        return addressBook;
    }

}
