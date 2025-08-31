package org.noesis.codeanalysis.util.general;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtils {
        public static String padToLength(String input, int length) {
            if (input == null) {
                input = "";
            }
            if (input.length() > length) {
                return input.substring(0, length);
            }
            return String.format("%-" + length + "s", input);
        }

    public static String normalizeNewLines(String input) {
        if (input == null) return null;
        return input.trim()              // Remove leading/trailing whitespace including newlines
                .replaceAll("\\n\\s*\\n+", "\n")  // Replace multiple newlines with single
                .replaceAll("^\\n+", "")          // Remove any remaining leading newlines
                .replaceAll("\\n+$", "");         // Remove any remaining trailing newlines
    }

    /**
     * Calculates the similarity between two package paths based on their common parts
     * @param package1 First package path (e.g. "example.com.light")
     * @param package2 Second package path (e.g. "example.com")
     * @return Float between 0 and 1 representing the similarity between packages
     */
    public static float calculatePackageDistance(String package1, String package2) {
        if (package1 == null || package2 == null) {
            return 0.0f;
        }

        // Split packages into their components
        String[] parts1 = package1.split("\\.");
        String[] parts2 = package2.split("\\.");

        // Find the common prefix length
        int commonLength = 0;
        int minLength = Math.min(parts1.length, parts2.length);

        while (commonLength < minLength && parts1[commonLength].equals(parts2[commonLength])) {
            commonLength++;
        }

        // Calculate similarity using the formula: 2 * common parts / (total parts in both packages)
        return (2.0f * commonLength) / (parts1.length + parts2.length);
    }

    public static String getLastToken(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        int lastIndex = input.lastIndexOf('/');
        return lastIndex == -1 ? input : input.substring(lastIndex + 1);
    }

    public static String indentAllLines(String input, int spaces) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String indent = " ".repeat(spaces);
        return input.lines()
                .map(line -> indent + line)
                .collect(Collectors.joining("\n"));
    }

    public static String stripCodeBlockMarkers(String input) {
        String[] lines = input.split("\n");
        List<String> outputLines = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            // Skip lines that start with ``` (with or without a language specifier)
            if (!line.trim().startsWith("```")) {
                outputLines.add(line);
            }
        }

        return String.join("\n", outputLines);
    }
}
