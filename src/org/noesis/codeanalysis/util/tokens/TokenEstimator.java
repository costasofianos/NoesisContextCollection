package org.noesis.codeanalysis.util.tokens;

import java.util.regex.Pattern;

public class TokenEstimator {

    public static int estimateTokens(String prompt) {
        if (prompt == null || prompt.isEmpty()) {
            return 0;
        }

        int tokenCount = 0;

        // Count words (split by whitespace)
        String[] words = prompt.split("\\s+");
        tokenCount += words.length;

        // Count punctuation and special characters
        int specialChars = (int) prompt.chars()
                .filter(ch -> !Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch))
                .count();
        tokenCount += specialChars;

        // Add tokens for numbers (typically tokenized separately)
        int numberTokens = (int) prompt.chars()
                .mapToObj(ch -> String.valueOf((char) ch))
                .filter(s -> s.matches("\\d"))
                .count();
        tokenCount += numberTokens / 2;  // Assume numbers are often merged into larger tokens

        // Account for CamelCase and snake_case splits
        int camelCaseSplits = countCamelCaseSplits(prompt);
        int snakeCaseSplits = countSnakeCaseSplits(prompt);
        tokenCount += camelCaseSplits + snakeCaseSplits;

        // Add extra tokens for code-specific elements
        int codeTokens = countCodeSpecificTokens(prompt);
        tokenCount += codeTokens;

        // Add a safety margin (15%)
        return tokenCount;
    }

    private static int countCamelCaseSplits(String text) {
        return (int) text.chars()
                .mapToObj(ch -> String.valueOf((char) ch))
                .filter(s -> s.matches("[A-Z]"))
                .count();
    }

    private static int countSnakeCaseSplits(String text) {
        return (int) text.chars()
                .filter(ch -> ch == '_')
                .count();
    }

    private static int countCodeSpecificTokens(String text) {
        int count = 0;

        // Count common programming symbols
        String[] codeElements = {
                "//", "/\\*", "\\*/", "import", "class", "public", "private",
                "protected", "static", "final", "\\{", "\\}", "\\(", "\\)", "\\[", "\\]",
                "=>", "->", "::", ";;", "\\.\\.\\.", "@"
        };

        for (String element : codeElements) {
            count += text.split(Pattern.quote(element), -1).length - 1;
        }

        // Count indentation levels
        count += text.split("\n\\s+").length - 1;

        return count;
    }

}