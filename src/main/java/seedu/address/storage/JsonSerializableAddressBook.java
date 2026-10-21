package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Department;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;

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
            Set<String> originalNames = new HashSet<>();
            for (String name : departments) {
                if (!Department.isValidDepartment(name)) {
                    throw new IllegalValueException(Department.MESSAGE_CONSTRAINTS);
                }
                Department department = new Department(name);
                if (!originalNames.add(name)) {
                    throw new IllegalValueException("Department catalogue contains duplicates.");
                }
                // Legacy names differing only in surrounding whitespace share one canonical entry.
                if (!addressBook.getDepartmentList().contains(department)) {
                    addressBook.addDepartment(department);
                }
            }
        }
        List<Person> restoredPersons = new ArrayList<>();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (departments != null && person.getDepartment().isPresent()
                    && !addressBook.getDepartmentList().contains(person.getDepartment().get())) {
                throw new IllegalValueException("Contact department is absent from the catalogue.");
            }
            restoredPersons.add(person);
        }
        try {
            // Loading is restoration, not a new add: keep every contact accepted by the old identity rule.
            addressBook.setPersons(restoredPersons);
        } catch (DuplicatePersonException e) {
            throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
        }
        return addressBook;
    }

}
