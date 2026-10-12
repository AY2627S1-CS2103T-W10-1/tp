package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class TagsMatchPredicateTest {
    @Test
    public void test_anyWholeTagMatchesIgnoringCase_returnsTrue() {
        TagsMatchPredicate predicate = new TagsMatchPredicate(List.of("missing", "CLIENT-1_vip"));
        assertTrue(predicate.test(new PersonBuilder().withTags("client-1_VIP", "friends").build()));
    }

    @Test
    public void test_noWholeTagMatches_returnsFalse() {
        TagsMatchPredicate predicate = new TagsMatchPredicate(List.of("friend"));
        assertFalse(predicate.test(new PersonBuilder().withTags("friends").build()));
        assertFalse(predicate.test(new PersonBuilder().withName("friend").withTags().build()));
        assertFalse(new TagsMatchPredicate(List.of()).test(new PersonBuilder().withTags("friend").build()));
    }

    @Test
    public void constructor_inputListChanges_doesNotChangeSearch() {
        List<String> tags = new ArrayList<>(List.of("friends"));
        TagsMatchPredicate predicate = new TagsMatchPredicate(tags);
        tags.clear();
        tags.add("family");
        assertTrue(predicate.test(new PersonBuilder().withTags("friends").build()));
        assertFalse(predicate.test(new PersonBuilder().withTags("family").build()));
    }

    @Test
    public void equals() {
        TagsMatchPredicate predicate = new TagsMatchPredicate(List.of("friends"));
        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new TagsMatchPredicate(List.of("friends"))));
        assertFalse(predicate.equals(new TagsMatchPredicate(List.of("family"))));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals(new NameContainsKeywordsPredicate(List.of("friends"))));
    }

    @Test
    public void toStringMethod() {
        TagsMatchPredicate predicate = new TagsMatchPredicate(List.of("friends", "family"));
        assertEquals(TagsMatchPredicate.class.getCanonicalName() + "{tags=[friends, family]}", predicate.toString());
    }
}
