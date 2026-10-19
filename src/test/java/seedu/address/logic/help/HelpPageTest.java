package seedu.address.logic.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;

public class HelpPageTest {
    @Test
    public void examples_extractsCommandsWithoutIndentationAndStopsAtBlankLine() {
        HelpPage page = new HelpPage("help", "Description\n\nExamples:\n  help\n    help depart\n\nOther notes.");
        assertEquals(List.of("help", "help depart"), page.examples());
        assertThrows(UnsupportedOperationException.class, () -> page.examples().clear());
    }

    @Test
    public void examples_missingOrEmptySection_returnsEmptyList() {
        for (String text : List.of("", "No examples.", "Examples:", "Examples:\n\nOther notes.")) {
            assertTrue(new HelpPage("help", text).examples().isEmpty());
        }
        assertTrue(HelpCatalog.getOverview().examples().isEmpty());
    }

    @Test
    public void examples_allCommandManualsProvideParsableExamples() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        for (HelpPage page : HelpCatalog.getPages()) {
            assertTrue(!page.examples().isEmpty(), page.title());
            for (String example : page.examples()) {
                parser.parseCommand(example);
            }
        }
    }
}
