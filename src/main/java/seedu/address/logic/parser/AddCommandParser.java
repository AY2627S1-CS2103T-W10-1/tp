package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Department;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/** Parses an add command with required name and optional contact information. */
public class AddCommandParser implements Parser<AddCommand> {
    private static final Pattern LONG_OPTION = Pattern.compile("(?<!\\S)--\\S+");

    @Override
    public AddCommand parse(String args) throws ParseException {
        boolean longOptions = args.stripLeading().startsWith("--");
        if (!longOptions && LONG_OPTION.matcher(args).find()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }
        ArgumentMultimap arguments = longOptions ? AddArgumentTokenizer.tokenize(args)
                : ArgumentTokenizer.tokenize(" " + args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                        PREFIX_ADDRESS, PREFIX_DEPARTMENT, PREFIX_TAG);

        if (!arguments.getPreamble().isEmpty() || arguments.getValue(PREFIX_NAME).isEmpty()) {
            throw new ParseException(longOptions ? AddCommand.MESSAGE_NAME_REQUIRED
                    : String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_ADDRESS, PREFIX_DEPARTMENT);
        String nameValue = arguments.getValue(PREFIX_NAME).orElseThrow();
        if (longOptions && nameValue.isBlank()) {
            throw new ParseException(AddCommand.MESSAGE_NAME_REQUIRED);
        }
        Name name = ParserUtil.parseName(nameValue);
        Phone phone = Phone.notProvided();
        if (arguments.getValue(PREFIX_PHONE).isPresent()) {
            String value = arguments.getValue(PREFIX_PHONE).orElseThrow().trim();
            if (!value.matches("[0-9]{7,15}")) {
                throw new ParseException(AddCommand.MESSAGE_INVALID_PHONE);
            }
            phone = ParserUtil.parsePhone(value);
        }
        Email email = arguments.getValue(PREFIX_EMAIL).isPresent()
                ? ParserUtil.parseEmail(arguments.getValue(PREFIX_EMAIL).orElseThrow()) : Email.notProvided();
        Address address = arguments.getValue(PREFIX_ADDRESS).isPresent()
                ? ParserUtil.parseAddress(arguments.getValue(PREFIX_ADDRESS).orElseThrow()) : Address.notProvided();
        Department department = null;
        if (arguments.getValue(PREFIX_DEPARTMENT).isPresent()) {
            String value = arguments.getValue(PREFIX_DEPARTMENT).orElseThrow().trim();
            if (!Department.isValidDepartment(value)) {
                throw new ParseException(Department.MESSAGE_CONSTRAINTS);
            }
            department = new Department(value);
        }
        Set<Tag> tags = new HashSet<>();
        for (String value : arguments.getAllValues(PREFIX_TAG)) {
            String normalized = value.trim().replaceFirst("^[#/]+", "");
            Tag tag = ParserUtil.parseTag(normalized);
            if (normalized.length() > 30) {
                throw new ParseException(AddCommand.MESSAGE_INVALID_TAG);
            }
            tags.add(tag);
        }
        return new AddCommand(new Person(name, phone, email, address, tags, department));
    }
}
