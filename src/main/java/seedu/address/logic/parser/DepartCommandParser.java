package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_SET;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DepartCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Department;

/** Parses arguments for {@link DepartCommand}. */
public class DepartCommandParser implements Parser<DepartCommand> {
    public static final String MESSAGE_INVALID_CONTACT_ID = "Invalid Contact ID: Must be a positive integer.";
    public static final String MESSAGE_EMPTY_DEPARTMENT = "Department name cannot be empty.";

    @Override
    public DepartCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_SET);
        String contactId = arguments.getPreamble().trim();
        if (!StringUtil.isNonZeroUnsignedInteger(contactId)) {
            throw new ParseException(MESSAGE_INVALID_CONTACT_ID);
        }
        if (arguments.getAllValues(PREFIX_SET).size() != 1 || !arguments.getValue(PREFIX_SET).isPresent()
                || arguments.getValue(PREFIX_SET).get().isEmpty()) {
            throw new ParseException(MESSAGE_EMPTY_DEPARTMENT);
        }

        String departmentName = arguments.getValue(PREFIX_SET).get();
        if (!Department.isValidDepartment(departmentName)) {
            throw new ParseException(Department.MESSAGE_CONSTRAINTS);
        }
        try {
            return new DepartCommand(Index.fromOneBased(Integer.parseInt(contactId)), contactId,
                    new Department(departmentName));
        } catch (NumberFormatException exception) {
            throw new ParseException(MESSAGE_INVALID_CONTACT_ID);
        }
    }
}
