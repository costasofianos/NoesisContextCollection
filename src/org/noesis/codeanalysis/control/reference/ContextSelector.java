package org.noesis.codeanalysis.control.reference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class ContextSelector {

    private static final String FILE_SEP_SYMBOL = "<|file_sep|>";
    private static final String FILE_COMPOSE_FORMAT = "%s%s\n%s";
    private static String extension;

    public static void main(String[] args) throws IOException {
        String stage = "practice";
        String language = "python";
        String strategy = "random";

        for (String arg : args) {
            if (arg.startsWith("--stage=")) stage = arg.substring("--stage=".length());
            if (arg.startsWith("--lang=")) language = arg.substring("--lang=".length());
            if (arg.startsWith("--strategy=")) strategy = arg.substring("--strategy=".length());
        }

        switch (language) {
            case "python" -> extension = ".py";
            case "kotlin" -> extension = ".kt";
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        }

        System.out.println("Running the " + strategy + " baseline for stage '" + stage + "'");

        Path completionPointsPath = Paths.get("data", language + "-" + stage + ".jsonl");
        Path predictionsPath = Paths.get("predictions", language + "-" + stage + "-" + strategy + ".jsonl");

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = new JsonFactory();
        ObjectWriter writer = mapper.writer();

        try (BufferedReader reader = Files.newBufferedReader(completionPointsPath);
             BufferedWriter bw = Files.newBufferedWriter(predictionsPath, StandardOpenOption.CREATE)) {

            String line;
            while ((line = reader.readLine()) != null) {
                JsonNode datapoint = mapper.readTree(line);
                String repo = datapoint.get("repo").asText().replace("/", "__");
                String revision = datapoint.get("revision").asText();
                Path rootDirectory = Paths.get("data", "repositories-" + language + "-" + stage, repo + "-" + revision);

                String selectedFile;
                switch (strategy) {
                    case "random" -> selectedFile = findRandomFile(rootDirectory);
                    case "bm25" -> {
                        String prefix = datapoint.get("prefix").asText();
                        String suffix = datapoint.get("suffix").asText();
                        selectedFile = findBM25File(rootDirectory, prefix, suffix);
                    }
                    case "recent" -> {
                        JsonNode modified = datapoint.get("modified");
                        List<String> recentFiles = new ArrayList<>();
                        modified.forEach(node -> recentFiles.add(node.asText()));
                        selectedFile = findRandomRecentFile(rootDirectory, recentFiles);
                        if (selectedFile == null) selectedFile = findRandomFile(rootDirectory);
                    }
                    default -> throw new IllegalArgumentException("Unknown strategy: " + strategy);
                }

                if (selectedFile == null) continue;

                String fileContent = Files.readString(Path.of(selectedFile));
                String cleanName = rootDirectory.relativize(Path.of(selectedFile)).toString();
                String context = String.format(FILE_COMPOSE_FORMAT, FILE_SEP_SYMBOL, cleanName, fileContent);

                Map<String, String> output = Map.of("context", context);
                bw.write(writer.writeValueAsString(output));
                bw.newLine();
                System.out.println("Picked file: " + cleanName);
            }
        }
    }

    private static String findRandomFile(Path rootDir) throws IOException {
        List<Path> matchingFiles = Files.walk(rootDir)
                .filter(p -> p.toString().endsWith(extension))
                .filter(p -> {
                    try {
                        return Files.lines(p).count() >= 10;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        return matchingFiles.isEmpty() ? null : matchingFiles.get(new Random().nextInt(matchingFiles.size())).toString();
    }

    private static String findRandomRecentFile(Path rootDir, List<String> recentFilenames) {
        List<Path> validPaths = recentFilenames.stream()
                .map(fname -> rootDir.resolve(fname))
                .filter(Files::exists)
                .filter(p -> {
                    try {
                        return Files.lines(p).count() >= 10;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        return validPaths.isEmpty() ? null : validPaths.get(new Random().nextInt(validPaths.size())).toString();
    }

    private static String findBM25File(Path rootDir, String prefix, String suffix) throws IOException {
        // Simplified: You need to port or use a Java BM25 library (like Lucene, RankLib, etc.)
        throw new UnsupportedOperationException("BM25 strategy not implemented in Java");
    }
}
