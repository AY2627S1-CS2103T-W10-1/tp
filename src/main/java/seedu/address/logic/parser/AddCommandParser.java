package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Set;

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

    @Override
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments;
        try {
            arguments = ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                    PREFIX_ADDRESS, PREFIX_DEPARTMENT, PREFIX_TAG);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE), pe);
        }

        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }
        if (arguments.getValue(PREFIX_NAME).isEmpty()) {
            throw new ParseException(AddCommand.MESSAGE_NAME_REQUIRED);
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_ADDRESS, PREFIX_DEPARTMENT);
        String nameValue = arguments.getValue(PREFIX_NAME).orElseThrow();
        if (nameValue.isBlank()) {
            throw new ParseException(AddCommand.MESSAGE_NAME_REQUIRED);
        }
        Name name = ParserUtil.parseName(nameValue);
        Phone phone = arguments.getValue(PREFIX_PHONE).isPresent()
                ? ParserUtil.parsePhone(arguments.getValue(PREFIX_PHONE).orElseThrow()) : Phone.notProvided();
        Email email = arguments.getValue(PREFIX_EMAIL).isPresent()
                ? ParserUtil.parseEmail(arguments.getValue(PREFIX_EMAIL).orElseThrow()) : Email.notProvided();
        Address address = arguments.getValue(PREFIX_ADDRESS).isPresent()
                ? ParserUtil.parseAddress(arguments.getValue(PREFIX_ADDRESS).orElseThrow()) : Address.notProvided();
        Department department = null;
        if (arguments.getValue(PREFIX_DEPARTMENT).isPresent()) {
            department = ParserUtil.parseDepartment(arguments.getValue(PREFIX_DEPARTMENT).orElseThrow());
        }
        Set<Tag> tags = ParserUtil.parseTags(arguments.getAllValues(PREFIX_TAG));
        return new AddCommand(new Person(name, phone, email, address, tags, department));
    }
}
