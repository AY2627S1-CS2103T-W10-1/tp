package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

public class ArgumentTokenizerTest {

    private final Prefix unknownPrefix = new Prefix("--u");
    private final Prefix pSlash = new Prefix("p/");
    private final Prefix dashT = new Prefix("-t");
    private final Prefix dashN = new Prefix("-n", "--name");
    private final Prefix hatQ = new Prefix("^Q");

    @Test
    public void tokenize_emptyArgsString_noValues() throws Exception {
        String argsString = "  ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);

        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);
    }

    private void assertPreamblePresent(ArgumentMultimap argMultimap, String expectedPreamble) {
        assertEquals(expectedPreamble, argMultimap.getPreamble());
    }

    private void assertPreambleEmpty(ArgumentMultimap argMultimap) {
        assertTrue(argMultimap.getPreamble().isEmpty());
    }

    /**
     * Asserts all the arguments in {@code argMultimap} with {@code prefix} match the {@code expectedValues}
     * and only the last value is returned upon calling {@code ArgumentMultimap#getValue(Prefix)}.
     */
    private void assertArgumentPresent(ArgumentMultimap argMultimap, Prefix prefix, String... expectedValues) {

        // Verify the last value is returned
        assertEquals(expectedValues[expectedValues.length - 1], argMultimap.getValue(prefix).get());

        // Verify the number of values returned is as expected
        assertEquals(expectedValues.length, argMultimap.getAllValues(prefix).size());

        // Verify all values returned are as expected and in order
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], argMultimap.getAllValues(prefix).get(i));
        }
    }

    private void assertArgumentAbsent(ArgumentMultimap argMultimap, Prefix prefix) {
        assertFalse(argMultimap.getValue(prefix).isPresent());
    }

    @Test
    public void tokenize_noPrefixes_allTakenAsPreamble() throws Exception {
        String argsString = "  some random string /t tag with leading and trailing spaces ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString);

        // Same string expected as preamble, but leading/trailing spaces should be trimmed
        assertPreamblePresent(argMultimap, argsString.trim());

    }

    @Test
    public void tokenize_oneArgument() throws Exception {
        // Preamble present
        String argsString = "  Some preamble string p/ Argument value ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreamblePresent(argMultimap, "Some preamble string");
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

        // No preamble
        argsString = " p/   Argument value ";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreambleEmpty(argMultimap);
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

    }

    @Test
    public void tokenize_multipleArguments() throws Exception {
        // Only two arguments are present
        String argsString = "SomePreambleString -t dashT-Value p/ pSlash value";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentAbsent(argMultimap, hatQ);

        // All three arguments are present
        argsString = "Different Preamble String ^Q 111 -t dashT-Value p/ pSlash value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "Different Preamble String");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentPresent(argMultimap, hatQ, "111");

        /* Also covers: Reusing of the tokenizer multiple times */

        // Reuse tokenizer on an empty string to ensure ArgumentMultimap is correctly reset
        // (i.e. no stale values from the previous tokenizing remain)
        argsString = "";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);

        /* Also covers: testing for prefixes not specified as a prefix */

        // Long options not given to the tokenizer are rejected
        assertThrows(ParseException.class, () ->
                ArgumentTokenizer.tokenize(unknownPrefix + " some value", pSlash, dashT, hatQ));
        assertThrows(ParseException.class, () ->
                ArgumentTokenizer.tokenize("preamble -t value " + unknownPrefix, pSlash, dashT, hatQ));
    }

    @Test
    public void tokenize_multipleArgumentsWithRepeats() throws Exception {
        // Two arguments repeated, some have empty values
        String argsString = "SomePreambleString -t dashT-Value ^Q ^Q -t another dashT value p/ pSlash value -t";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value", "another dashT value", "");
        assertArgumentPresent(argMultimap, hatQ, "", "");
    }

    @Test
    public void tokenize_multipleArgumentsJoined() throws Exception {
        String argsString = "SomePreambleStringp/ pSlash joined-tjoined -t not joined^Qjoined";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleStringp/ pSlash joined-tjoined");
        assertArgumentAbsent(argMultimap, pSlash);
        assertArgumentPresent(argMultimap, dashT, "not joined^Qjoined");
        assertArgumentAbsent(argMultimap, hatQ);
    }

    @Test
    public void tokenize_prefixNotFollowedByWhitespace_notRecognised() throws Exception {
        // Value glued to the prefix
        String argsString = "SomePreambleString -tglued -n name";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, dashT, dashN);
        assertPreamblePresent(argMultimap, "SomePreambleString -tglued");
        assertArgumentAbsent(argMultimap, dashT);
        assertArgumentPresent(argMultimap, dashN, "name");

        // Short-prefix-like text inside a value
        argsString = " -n Blk 5 -apt 3";
        argMultimap = ArgumentTokenizer.tokenize(argsString, dashT, dashN);
        assertPreambleEmpty(argMultimap);
        assertArgumentPresent(argMultimap, dashN, "Blk 5 -apt 3");

        // Long-option-like text must be quoted, otherwise it is an unknown option
        assertThrows(ParseException.class, () -> ArgumentTokenizer.tokenize(" -n Blk 5 --names", dashT, dashN));
        argMultimap = ArgumentTokenizer.tokenize(" -n Blk 5 \"--names\"", dashT, dashN);
        assertArgumentPresent(argMultimap, dashN, "Blk 5 --names");
    }

    @Test
    public void tokenize_prefixFollowedByOtherWhitespace_recognised() throws Exception {
        String argsString = " -n\tname -t\nvalue";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, dashT, dashN);
        assertArgumentPresent(argMultimap, dashN, "name");
        assertArgumentPresent(argMultimap, dashT, "value");
    }

    @Test
    public void tokenize_longAlias_mapsToSamePrefix() throws Exception {
        // Long form only
        String argsString = "SomePreambleString --name long value -t dashT-Value";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, dashT, dashN);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, dashN, "long value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");

        // Long form with an empty value at the end of the string
        argsString = " -t value --name";
        argMultimap = ArgumentTokenizer.tokenize(argsString, dashT, dashN);
        assertArgumentPresent(argMultimap, dashN, "");
    }

    @Test
    public void tokenize_shortAndLongFormsMixed_valuesAccumulatedInOrder() throws Exception {
        String argsString = " -n first --name second -n third";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, dashN);
        assertPreambleEmpty(argMultimap);
        assertArgumentPresent(argMultimap, dashN, "first", "second", "third");
    }

    @Test
    public void tokenize_shortAndLongFormsMixed_detectedAsDuplicate() throws Exception {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" -n first --name second", dashN);
        assertThrows(ParseException.class, () -> argMultimap.verifyNoDuplicatePrefixesFor(dashN));
    }

    @Test
    public void tokenize_quotedValues_keptAsOneWord() throws Exception {
        // a prefix inside quotes is part of the value
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" -n \"Blk 5 -t 3\" -t 'x'", dashT, dashN);
        assertArgumentPresent(argMultimap, dashN, "Blk 5 -t 3");
        assertArgumentPresent(argMultimap, dashT, "x");

        // quoted and unquoted words are joined
        argMultimap = ArgumentTokenizer.tokenize(" -n Main \"--name\" Road", dashN);
        assertArgumentPresent(argMultimap, dashN, "Main --name Road");

        // quotes inside an unquoted word are ordinary characters
        argMultimap = ArgumentTokenizer.tokenize(" -n O'Brien Road", dashN);
        assertArgumentPresent(argMultimap, dashN, "O'Brien Road");

        // empty quotes give an empty value
        argMultimap = ArgumentTokenizer.tokenize(" -n \"\"", dashN);
        assertArgumentPresent(argMultimap, dashN, "");
    }

    @Test
    public void tokenize_malformedQuotes_throwsParseException() throws Exception {
        assertThrows(ParseException.class, () -> ArgumentTokenizer.tokenize(" -n \"Alice", dashN));
        assertThrows(ParseException.class, () -> ArgumentTokenizer.tokenize(" -n 'Alice'junk", dashN));
    }

    @Test
    public void tokenize_eachWordAsValue_wordsStoredSeparately() throws Exception {
        Prefix tags = new Prefix("-t", "--tag").withEachWordAsValue();

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" -t a b --tag c \"d e\"", tags);
        assertArgumentPresent(argMultimap, tags, "a", "b", "c", "d e");

        // an empty value is still stored once
        argMultimap = ArgumentTokenizer.tokenize(" -t", tags);
        assertArgumentPresent(argMultimap, tags, "");
    }

    @Test
    public void tokenize_whitespaceBetweenWords_normalised() throws Exception {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize("  1 \t2 -n  Main \n Road ", dashN);
        assertPreamblePresent(argMultimap, "1 2");
        assertArgumentPresent(argMultimap, dashN, "Main Road");
    }

    @Test
    public void getAllForms() {
        assertEquals(List.of("-n", "--name"), dashN.getAllForms());
        assertEquals(List.of("-t"), dashT.getAllForms());
    }

    @Test
    public void equalsMethod() {
        Prefix aaa = new Prefix("aaa");

        assertEquals(aaa, aaa);
        assertEquals(aaa, new Prefix("aaa"));

        assertNotEquals(aaa, "aaa");
        assertNotEquals(aaa, new Prefix("aab"));

        // equality depends only on the primary form
        assertEquals(dashN, new Prefix("-n"));
        assertNotEquals(dashN, new Prefix("--name"));
    }

}
