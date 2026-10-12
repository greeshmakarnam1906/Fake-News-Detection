package com.fakenews.ml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TextPreprocessor Unit Tests")
class TextPreprocessorTest {

    @Test
    @DisplayName("Should strip publisher wire leak tags from text")
    void testStripPublisherLeaks() {
        String wireText = "WASHINGTON (Reuters) - Congress passed the new budget bill today.";
        String cleaned = TextPreprocessor.cleanText(wireText);

        assertFalse(cleaned.contains("reuters"));
        assertTrue(cleaned.contains("congress passed"));
    }

    @Test
    @DisplayName("Should normalize unicode, accents, and remove punctuation")
    void testNormalizeUnicodeAndPunctuation() {
        String dirtyText = "Breaking! 100% UNPRECEDENTED crisis in São Paulo... Check out https://example.com/news!";
        String cleaned = TextPreprocessor.cleanText(dirtyText);

        assertEquals("breaking 100 unprecedented crisis in sao paulo check out", cleaned);
    }

    @Test
    @DisplayName("Should remove stop words while preserving informative terms")
    void testTokenizeAndRemoveStopWords() {
        String text = "The government is investigating this sudden corruption scandal";
        List<String> tokens = TextPreprocessor.tokenize(text);

        assertFalse(tokens.contains("the"));
        assertFalse(tokens.contains("is"));
        assertFalse(tokens.contains("this"));
        assertTrue(tokens.contains("government"));
        assertTrue(tokens.contains("investigating"));
        assertTrue(tokens.contains("corruption"));
        assertTrue(tokens.contains("scandal"));
    }

    @Test
    @DisplayName("Should handle empty and whitespace-only text safely")
    void testEmptyAndBlankText() {
        assertEquals("", TextPreprocessor.cleanText(null));
        assertEquals("", TextPreprocessor.cleanText("   "));
        assertTrue(TextPreprocessor.tokenize(null).isEmpty());
        assertTrue(TextPreprocessor.tokenize("     ").isEmpty());
    }
}
