package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
        assertTrue(Tag.isValidTagName("a"));
        assertTrue(Tag.isValidTagName("Client-01_vip"));
        assertTrue(Tag.isValidTagName("a".repeat(30)));
        assertFalse(Tag.isValidTagName("a".repeat(31)));
        assertFalse(Tag.isValidTagName(""));
        assertFalse(Tag.isValidTagName("two words"));
        assertFalse(Tag.isValidTagName("bad!"));
    }

}
