package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEPARTMENT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Address;
import seedu.address.model.person.Department;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

public class PartialAddCommandParserTest {
    private final AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allOptionalFieldCombinations_success() {
        for (int mask = 0; mask < 32; mask++) {
            boolean phone = (mask & 1) != 0;
            boolean email = (mask & 2) != 0;
            boolean address = (mask & 4) != 0;
            boolean department = (mask & 8) != 0;
            boolean tags = (mask & 16) != 0;
            Person expected = new Person(new Name("Alice Tan"), phone ? new Phone("91234567") : Phone.notProvided(),
                    email ? new Email("alice@example.com") : Email.notProvided(),
                    address ? new Address("Main Road") : Address.notProvided(),
                    tags ? Set.of(new Tag("intern")) : Set.of(), department ? new Department("R&D") : null);
            String longArgs = " --name \"Alice Tan\"" + (phone ? " --phone 91234567" : "")
                    + (email ? " --email alice@example.com" : "") + (address ? " --address \"Main Road\"" : "")
                    + (department ? " --department R&D" : "") + (tags ? " --tag intern" : "");
            String shortArgs = " -n Alice Tan" + (phone ? " -p 91234567" : "")
                    + (email ? " -e alice@example.com" : "") + (address ? " -a Main Road" : "")
                    + (department ? " -d R&D" : "") + (tags ? " -t intern" : "");
            assertParseSuccess(parser, longArgs, new AddCommand(expected));
            assertParseSuccess(parser, shortArgs, new AddCommand(expected));
        }
    }

    @Test
    public void parse_reorderedOptionsAndWhitespace_success() {
        Person expected = partial("Bob Lee", new Phone("91234567"), Email.notProvided());
        assertParseSuccess(parser, "\t--phone\t91234567\n--name 'Bob Lee'  ", new AddCommand(expected));
        assertParseSuccess(parser, " --name Bob Lee --phone 91234567", new AddCommand(expected));
    }

    @Test
    public void parse_missingOrBlankName_failure() {
        for (String args : new String[]{" --name", " --name \"\"", " --name '  '", " --phone 91234567"}) {
            assertParseFailure(parser, args, AddCommand.MESSAGE_NAME_REQUIRED);
        }
    }

    @Test
    public void parse_phoneBoundaries_successOrFailure() {
        for (String phone : new String[]{"1234567", "123456789012345"}) {
            assertParseSuccess(parser, " --name Alice --phone " + phone,
                    new AddCommand(partial("Alice", new Phone(phone), Email.notProvided())));
        }
        for (String phone : new String[]{"", "123456", "1234567890123456", "+6591234567", "123abc4", "１２３４５６７"}) {
            assertParseFailure(parser, " --name Alice --phone " + phone, ParserUtil.MESSAGE_INVALID_PHONE);
        }
    }

    @Test
    public void parse_suppliedInvalidOptionalFields_failure() {
        assertParseFailure(parser, " --name Alice --email", Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " --name Alice --email example.com", Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " --name Alice --address \"\"", Address.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " --name Alice --department", Department.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " --name Alice --department !", Department.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " --name Alice --department " + "A".repeat(51), Department.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " --name Alice --tag", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " --name Alice --tag \"two words\"", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " --name Alice --tag comma,tag", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " --name Alice --tag " + "A".repeat(31), ParserUtil.MESSAGE_INVALID_TAG);
    }

    @Test
    public void parse_tagsNormalizedDeduplicatedAndCasePreserved_success() {
        Person expected = new Person(new Name("Alice"), Phone.notProvided(), Email.notProvided(),
                Address.notProvided(), Set.of(new Tag("Important"), new Tag("team_intern"), new Tag("new-client"),
                        new Tag("A".repeat(30))), null);
        assertParseSuccess(parser, " --name Alice --tag #Important team_intern /new-client --tag Important "
                + "A".repeat(30), new AddCommand(expected));
    }

    @Test
    public void parse_quotedOptionLikeAddress_success() {
        Person expected = new Person(new Name("Alice"), Phone.notProvided(), Email.notProvided(),
                new Address("Apartment --tag 12"), Set.of());
        assertParseSuccess(parser, " --name Alice --address \"Apartment --tag 12\"", new AddCommand(expected));
    }

    @Test
    public void parse_shortFlagsAfterLongOptions_startNewField() {
        Person expected = new Person(new Name("Alice"), new Phone("91234567"), Email.notProvided(),
                new Address("Main Road"), Set.of(), new Department("Engineering"));
        for (String separator : List.of(" ", "\t", "\n")) {
            assertParseSuccess(parser, " --name Alice --address Main Road" + separator + "-p 91234567"
                    + " -d Engineering", new AddCommand(expected));
        }
        assertParseSuccess(parser, " --name Alice -p 91234567 --address Main Road --department Engineering",
                new AddCommand(expected));
    }

    @Test
    public void parse_flagLikeWordsInLongOptionValues_success() {
        // only a flag on its own counts as a short flag, as in the short-flag syntax
        Person expected = new Person(new Name("Alice"), Phone.notProvided(), Email.notProvided(),
                new Address("Blk 5 -apt 3 -p91234567"), Set.of());
        assertParseSuccess(parser, " --name Alice --address Blk 5 -apt 3 -p91234567", new AddCommand(expected));
    }

    @Test
    public void parse_quotedShortFlagsAndNormalizedTags_success() {
        for (String quote : List.of("\"", "'")) {
            Person expected = new Person(new Name("Alice"), Phone.notProvided(), Email.notProvided(),
                    new Address("Main Road -n Alice -p 91234567 -e alice@example.com -a Block -d Sales -t client"),
                    Set.of(new Tag("client"), new Tag("intern")));
            assertParseSuccess(parser, " --name Alice --address " + quote + expected.getAddress().value + quote
                    + " --tag /client #intern", new AddCommand(expected));

            Person quotedWord = new Person(new Name("Alice"), Phone.notProvided(), Email.notProvided(),
                    new Address("Main Road -p"), Set.of());
            assertParseSuccess(parser, " --name Alice --address Main Road " + quote + "-p" + quote,
                    new AddCommand(quotedWord));
        }
    }

    @Test
    public void parse_unknownOptionsOrMalformedQuotes_failure() {
        String error = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        for (String args : List.of(" --name Alice --unknown x", " --name=Alice", " --name \"Alice",
                " --name 'Alice'junk", " --name Alice --tags client", " --name Alice --",
                " -n Alice -a Main Road --unknown x")) {
            assertParseFailure(parser, args, error);
        }

        // a quote inside a word is an ordinary character, so the name itself is invalid
        assertParseFailure(parser, " --name Ali\"ce", Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_duplicateSingleValueOptions_failure() {
        Map<String, Prefix> options = Map.of("--name", PREFIX_NAME, "--phone", PREFIX_PHONE,
                "--email", PREFIX_EMAIL, "--department", PREFIX_DEPARTMENT, "--address", PREFIX_ADDRESS);
        options.forEach((option, prefix) -> {
            String args = option.equals("--name") ? " --name Alice --name Bob"
                    : " --name Alice " + option + " value " + option + " other";
            assertParseFailure(parser, args, Messages.getErrorMessageForDuplicatePrefixes(prefix));
        });
    }

    private Person partial(String name, Phone phone, Email email) {
        return new Person(new Name(name), phone, email, Address.notProvided(), Set.of());
    }
}
