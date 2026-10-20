package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.stream.Collectors;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.help.HelpPage;
import seedu.address.model.Model;
import seedu.address.model.person.Department;

/** Manages the shared department catalogue. */
public class DepartCommand extends Command {
    public static final String COMMAND_WORD = "depart";
    public static final String MESSAGE_USAGE = "depart --list | depart --add DEPARTMENT"
            + " | depart --edit OLD_NAME NEW_NAME | depart --delete DEPARTMENT";
    public static final String MESSAGE_EMPTY = "No departments have been registered.";
    public static final String MESSAGE_DUPLICATE = "Department already exists: %1$s";
    public static final String MESSAGE_UNKNOWN = "Department does not exist: %1$s."
            + " Use depart --list or depart --add DEPARTMENT.";
    public static final String MESSAGE_ADDED = "Added department: %1$s";
    public static final String MESSAGE_RENAMED = "Renamed department: %1$s -> %2$s";

    public static final String MESSAGE_DELETED = "Deleted department: %1$s. "
            + "Associated contacts now have no department.";

    /** Detailed usage, supported options and examples for this command. */
    public static final HelpPage HELP_PAGE = new HelpPage(COMMAND_WORD, String.join("\n",
            "Manages the department catalogue.",
            "Usage: " + MESSAGE_USAGE,
            "",
            "Options (choose one):",
            "  --list  List every registered department, including unused entries.",
            "  --add   Register one new department.",
            "  --delete  Remove a department and clear its assignments, including in undo history.",
            "  --edit  Rename a department and update all its contacts, including hidden contacts.",
            "",
            "Names are case-sensitive and contain 2-50 letters, numbers, spaces, hyphens or ampersands.",
            "Quote names containing spaces. Duplicate names are rejected.",
            "Use edit INDEX --department DEPARTMENT to assign a registered department to a contact.",
            "",
            "Examples:",
            "  depart --list",
            "  depart --add Engineering",
            "  depart --edit Engineering \"Research & Development\""));

    /** Supported catalogue operations. */
    public enum Operation { LIST, ADD, EDIT, DELETE }

    private final Operation operation;
    private final Department department;
    private final Department replacement;

    private DepartCommand(Operation operation, Department department, Department replacement) {
        this.operation = requireNonNull(operation);
        this.department = department;
        this.replacement = replacement;
    }

    /** Creates a read-only catalogue listing. */
    public static DepartCommand list() {
        return new DepartCommand(Operation.LIST, null, null);
    }

    /** Creates an addition to the catalogue. */
    public static DepartCommand add(Department department) {
        return new DepartCommand(Operation.ADD, requireNonNull(department), null);
    }

    /** Creates a catalogue rename. */
    public static DepartCommand rename(Department department, Department replacement) {
        return new DepartCommand(Operation.EDIT, requireNonNull(department), requireNonNull(replacement));
    }

    /** Creates a catalogue deletion that clears associated assignments. */
    public static DepartCommand delete(Department department) {
        return new DepartCommand(Operation.DELETE, requireNonNull(department), null);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (operation == Operation.LIST) {
            String names = model.getAddressBook().getDepartmentList().stream()
                    .map(value -> value.value).collect(Collectors.joining("\n"));
            return new CommandResult(names.isEmpty() ? MESSAGE_EMPTY : "Departments:\n" + names);
        }
        if (operation == Operation.ADD) {
            if (model.hasDepartment(department)) {
                throw new CommandException(String.format(MESSAGE_DUPLICATE, department));
            }
            model.addDepartment(department);
            return new CommandResult(String.format(MESSAGE_ADDED, department));
        }
        if (!model.hasDepartment(department)) {
            throw new CommandException(String.format(MESSAGE_UNKNOWN, department));
        }
        if (operation == Operation.DELETE) {
            model.deleteDepartment(department);
            return new CommandResult(String.format(MESSAGE_DELETED, department));
        }
        if (!department.equals(replacement) && model.hasDepartment(replacement)) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE, replacement));
        }
        model.renameDepartment(department, replacement);
        return new CommandResult(String.format(MESSAGE_RENAMED, department, replacement));
    }

    @Override
    public boolean requiresStorageSave() {
        return operation != Operation.LIST;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof DepartCommand otherCommand
                && operation == otherCommand.operation && Objects.equals(department, otherCommand.department)
                && Objects.equals(replacement, otherCommand.replacement));
    }
}
