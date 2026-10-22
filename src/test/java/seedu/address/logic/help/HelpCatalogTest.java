package seedu.address.logic.help;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;

public class HelpCatalogTest {
    @Test
    public void pages_coverEverySupportedCommandAndAliases() {
        assertEquals(List.of("add", "edit", "depart", "find", "view", "list", "sort", "delete", "undo",
                "export", "clear", "help", "exit"), HelpCatalog.getPages().stream().map(HelpPage::title).toList());
        for (HelpPage page : HelpCatalog.getPages()) {
            assertEquals(page, HelpCatalog.findPage(page.title()).orElseThrow());
            assertTrue(page.text().contains("Usage: " + page.title()));
            assertTrue(HelpCatalog.getOverview().text().contains(page.title() + " "));
        }
        assertEquals(HelpCatalog.findPage("list"), HelpCatalog.findPage(" LS "));
        assertEquals(HelpCatalog.findPage("help"), HelpCatalog.findPage("?"));
        assertTrue(HelpCatalog.findPage("unknown").isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> HelpCatalog.getPages().clear());
    }

    @Test
    public void examples_areAcceptedByActualCommandParsers() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        for (HelpPage page : HelpCatalog.getPages()) {
            List<String> examples = page.text().substring(page.text().indexOf("Examples:\n")
                    + "Examples:\n".length()).lines().map(String::trim).toList();
            assertFalse(examples.isEmpty());
            for (String example : examples) {
                parser.parseCommand(example);
            }
        }
    }

    @Test
    public void helpPage_rejectsNullTitleAndText() {
        assertThrows(NullPointerException.class, () -> new HelpPage(null, "text"));
        assertThrows(NullPointerException.class, () -> new HelpPage("title", null));
    }
    @Test
    public void findPage_nullName_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> HelpCatalog.findPage(null));
    }

    @Test
    public void entries_haveShortSummariesAndResolveEveryAlias() {
        assertEquals(HelpCatalog.getPages(), HelpCatalog.getEntries().stream().map(HelpEntry::page).toList());
        assertThrows(UnsupportedOperationException.class, () -> HelpCatalog.getEntries().clear());
        for (HelpEntry entry : HelpCatalog.getEntries()) {
            assertTrue(entry.summary().length() <= 35);
            assertTrue(HelpCatalog.getOverview().text().contains(entry.displayName() + " — " + entry.summary()));
            for (String alias : entry.aliases()) {
                assertEquals(entry.page(), HelpCatalog.findPage(alias).orElseThrow());
            }
        }
    }

    @Test
    public void entry_copiesAliasesAndRejectsNullFields() {
        HelpPage page = new HelpPage("list", "Manual");
        List<String> aliases = new ArrayList<>(List.of("ls"));
        HelpEntry entry = new HelpEntry(page, "List contacts.", aliases);
        aliases.clear();
        assertEquals(List.of("ls"), entry.aliases());
        assertThrows(UnsupportedOperationException.class, () -> entry.aliases().clear());
        assertThrows(NullPointerException.class, () -> new HelpEntry(null, "Summary", List.of()));
        assertThrows(NullPointerException.class, () -> new HelpEntry(page, null, List.of()));
        assertThrows(NullPointerException.class, () -> new HelpEntry(page, "Summary", null));
    }

}
