package com.fakenews.ml;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

/**
 * TextPreprocessor provides unified, deterministic cleaning and tokenization
 * for both training and inference to ensure zero training-serving skew.
 */
public class TextPreprocessor {

    // Common source/wire artifacts that leak ground truth labels in news datasets
    private static final List<Pattern> LEAK_PATTERNS = List.of(
            Pattern.compile("(?i)^[\\w\\s,/-]+?\\s*\\(reuters\\)\\s*[-–—:]?\\s*"),
            Pattern.compile("(?i)^[\\w\\s,/-]+?\\s*\\(ap\\)\\s*[-–—:]?\\s*"),
            Pattern.compile("(?i)^[\\w\\s,/-]+?\\s*\\(afp\\)\\s*[-–—:]?\\s*"),
            Pattern.compile("(?i)^reuters\\s*[-–—:]?\\s*"),
            Pattern.compile("(?i)^associated press\\s*[-–—:]?\\s*"),
            Pattern.compile("(?i)\\breuters\\b"),
            Pattern.compile("(?i)\\bap photo\\b"),
            Pattern.compile("(?i)\\bgetty images\\b"),
            Pattern.compile("(?i)featured image credit:.*$")
    );

    // Standard English stop words (negation words like not, no, never are kept for sentiment/context)
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but",
            "by", "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from",
            "further", "had", "has", "have", "having", "he", "her", "here", "hers", "herself", "him",
            "himself", "his", "how", "i", "if", "in", "into", "is", "it", "its", "itself", "just", "me",
            "more", "most", "my", "myself", "of", "off", "on", "once", "only", "or", "other", "ought",
            "our", "ours", "ourselves", "out", "over", "own", "same", "she", "should", "so", "some",
            "such", "than", "that", "the", "their", "theirs", "them", "themselves", "then", "there",
            "these", "they", "this", "those", "through", "to", "too", "under", "until", "up", "very",
            "was", "we", "were", "what", "when", "where", "which", "while", "who", "whom", "why",
            "with", "would", "you", "your", "yours", "yourself", "yourselves"
    ));

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s]");
    private static final Pattern MULTI_WHITESPACE = Pattern.compile("\\s+");

    /**
     * Cleans raw text:
     * 1. Strips wire/publisher leakage signatures.
     * 2. Normalizes unicode characters.
     * 3. Lowers case and strips non-alphanumeric punctuation.
     * 4. Normalizes whitespace.
     */
    public static String cleanText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }

        String cleaned = rawText;
        // Strip source leak patterns
        for (Pattern leakPattern : LEAK_PATTERNS) {
            cleaned = leakPattern.matcher(cleaned).replaceAll(" ");
        }

        // Unicode normalization (NFD) and strip combining diacritical marks (e.g., ã -> a)
        cleaned = Normalizer.normalize(cleaned, Normalizer.Form.NFD);
        cleaned = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(cleaned).replaceAll("");
        cleaned = cleaned.toLowerCase(Locale.ROOT);

        // Replace URLs and mentions with empty space to avoid domain artifacts
        cleaned = cleaned.replaceAll("https?://\\S+\\b", " ");
        cleaned = cleaned.replaceAll("www\\.\\S+\\b", " ");
        cleaned = cleaned.replaceAll("@\\w+", " ");

        // Remove punctuation and special symbols
        cleaned = NON_ALPHANUMERIC.matcher(cleaned).replaceAll(" ");

        // Normalize multiple spaces
        cleaned = MULTI_WHITESPACE.matcher(cleaned).replaceAll(" ").trim();

        return cleaned;
    }

    /**
     * Tokenizes cleaned text into normalized tokens, excluding stop words.
     */
    public static List<String> tokenize(String rawText) {
        String cleaned = cleanText(rawText);
        if (cleaned.isEmpty()) {
            return Collections.emptyList();
        }

        String[] parts = cleaned.split(" ");
        List<String> tokens = new ArrayList<>(parts.length);

        for (String part : parts) {
            if (part.length() >= 2 && part.length() <= 30 && !STOP_WORDS.contains(part)) {
                tokens.add(part);
            }
        }

        return tokens;
    }
}
