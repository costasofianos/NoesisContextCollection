package org.noesis.codeanalysis.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.noesis.codeanalysis.dataobjects.input.ContextCollectionInputJsonLine;
import org.noesis.codeanalysis.dataobjects.input.ContextCollectionTargetJsonLine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LocalDataCreator {
    private static final String ROOT_DATA_FOLDER = "/Users/costa/Development/JetBrainsContextCollection/data";
    private static final String INPUT_FILE = "kotlin-local.jsonl";
    private static final Random random = new Random();

    public static void main(String[] args) {
        try {
            processAndWriteFiles();
        } catch (Exception e) {
            System.err.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
        }
    }




    private static void processAndWriteFiles() throws IOException {
        Path inputPath = Paths.get(ROOT_DATA_FOLDER, INPUT_FILE);
        Path outputPath = Paths.get(ROOT_DATA_FOLDER, "kotlin-local-target.jsonl");
        ObjectMapper objectMapper = new ObjectMapper();

        // Read all input lines
        List<ContextCollectionInputJsonLine> inputLines = readInputJsonLines();
        List<String> updatedInputLines = new ArrayList<>();
        List<String> targetLines = new ArrayList<>();

        for (ContextCollectionInputJsonLine inputLine : inputLines) {
            try {
                // Construct the full file path
                Path filePath = Paths.get(
                        ROOT_DATA_FOLDER,
                        "repositories-kotlin-local",
                        inputLine.getRepoFolderString(),
                        inputLine.path()
                );

                if (Files.exists(filePath)) {
                    System.out.println("Found file "+"<"+filePath+">" + " for input: " + inputLine.id());
                    String fileContent = Files.readString(filePath);
                    List<String> lines = Arrays.asList(fileContent.split("\n"));

                    if (!lines.isEmpty()) {
                        // Extract target text and get prefix/suffix
                        ExtractResult result = extractTargetText(lines);
                        //System.out.println("Extracted target text: " + result.target);

                        // Create updated input line
                        ContextCollectionInputJsonLine updatedInputLine = new ContextCollectionInputJsonLine(
                                inputLine.id(),
                                inputLine.repo(),
                                inputLine.revision(),
                                inputLine.path(),
                                inputLine.modified(),
                                result.prefix,
                                result.suffix,
                                inputLine.archive()
                        );

                        // Create target line
                        ContextCollectionTargetJsonLine targetLine = new ContextCollectionTargetJsonLine(
                                inputLine.id(),
                                inputLine.repo(),
                                inputLine.revision(),
                                inputLine.path(),
                                inputLine.modified(),
                                result.prefix,
                                result.suffix,
                                inputLine.archive(),
                                result.target
                        );

                        // Create an ObjectNode to control exactly which fields are included
                        ObjectNode inputNode = objectMapper.createObjectNode();
                        inputNode.put("id", inputLine.id());
                        inputNode.put("repo", inputLine.repo());
                        inputNode.put("revision", inputLine.revision());
                        inputNode.put("path", inputLine.path());
                        inputNode.set("modified", objectMapper.valueToTree(inputLine.modified()));
                        inputNode.put("prefix", result.prefix);
                        inputNode.put("suffix", result.suffix);
                        inputNode.put("archive", inputLine.archive());

                        ObjectNode targetNode = objectMapper.createObjectNode();
                        targetNode.put("id", inputLine.id());
                        targetNode.put("repo", inputLine.repo());
                        targetNode.put("revision", inputLine.revision());
                        targetNode.put("path", inputLine.path());
                        targetNode.set("modified", objectMapper.valueToTree(inputLine.modified()));
                        targetNode.put("prefix", result.prefix);
                        targetNode.put("suffix", result.suffix);
                        targetNode.put("archive", inputLine.archive());
                        targetNode.put("target", result.target);


                        // Add to respective lists
                        updatedInputLines.add(objectMapper.writeValueAsString(inputNode));
                        targetLines.add(objectMapper.writeValueAsString(targetNode));
                    }
                } else {
                    System.err.println("File not found: " + filePath);
                    // Keep original input line if file not found
                    updatedInputLines.add(objectMapper.writeValueAsString(inputLine));
                }
            } catch (IOException e) {
                System.err.println("Error processing file for input: " + inputLine.id() + " - " + e.getMessage());
                // Keep original input line if there's an error
                updatedInputLines.add(objectMapper.writeValueAsString(inputLine));
            }
        }

        System.out.println("Input Path "+"<"+inputPath+">");
        System.out.println("Output Path "+"<"+outputPath+">");
        // Write both files
        Files.write(inputPath, updatedInputLines);
        Files.write(outputPath, targetLines);
    }


    private static ExtractResult extractTargetText(List<String> lines) {
        if (lines.isEmpty()) {
            return new ExtractResult("", "", "");
        }

        // Join with newlines, ensuring we don't add an extra newline at the end
        String fullText = String.join("\n", lines);

        // Find the last import line
        int lastImportLine = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().startsWith("import ")) {
                lastImportLine = i;
            }
        }

        // Select random number of lines between 3 and 7
        int numLines = random.nextInt(4) + 3;

        // Start from the larger of: halfway point or after last import
        int minStartIdx = Math.max(lines.size() / 2, lastImportLine + 1);

        // Ensure we don't try to select more lines than available
        int maxStartIdx = Math.max(minStartIdx, lines.size() - numLines - 1);
        int startIdx = minStartIdx;
        if (maxStartIdx > minStartIdx) {
            startIdx += random.nextInt(maxStartIdx - minStartIdx);
        }
        int endIdx = Math.min(startIdx + numLines, lines.size() - 1);

        // Calculate character positions
        int targetStart = 0;
        for (int i = 0; i < startIdx; i++) {
            targetStart += lines.get(i).length() + 1; // +1 for newline
        }

        int targetEnd = targetStart;
        for (int i = startIdx; i < endIdx; i++) {
            targetEnd += lines.get(i).length() + 1;
        }

        // Ensure we don't exceed string bounds
        targetEnd = Math.min(targetEnd, fullText.length());
        targetStart = Math.min(targetStart, targetEnd);

        // Extract the three parts
        String prefix = fullText.substring(0, targetStart);
        String target = fullText.substring(targetStart, targetEnd);
        String suffix = fullText.substring(targetEnd);

        // Verify the concatenation matches original
        assert (prefix + target + suffix).equals(fullText) : "Text reconstruction failed";

        return new ExtractResult(prefix, target, suffix);
    }


    private static void writeOutputJsonLines(List<ContextCollectionTargetJsonLine> outputLines) {
        Path outputPath = Paths.get(ROOT_DATA_FOLDER, "kotlin-local-with-target.jsonl");
        ObjectMapper objectMapper = new ObjectMapper();

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            for (ContextCollectionTargetJsonLine line : outputLines) {
                writer.write(objectMapper.writeValueAsString(line));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
        }
    }

    private static List<ContextCollectionInputJsonLine> readInputJsonLines() throws IOException {
        Path inputPath = Paths.get(ROOT_DATA_FOLDER, INPUT_FILE);
        List<ContextCollectionInputJsonLine> inputLines = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    ContextCollectionInputJsonLine jsonLine = ContextCollectionInputJsonLine.fromJson(line);
                    inputLines.add(jsonLine);
                } catch (JsonProcessingException e) {
                    System.err.println("Error parsing JSON line: " + e.getMessage());
                }
            }
        }
        return inputLines;
    }

    private static class ExtractResult {
        final String prefix;
        final String target;
        final String suffix;

        ExtractResult(String prefix, String target, String suffix) {
            this.prefix = prefix;
            this.target = target;
            this.suffix = suffix;
        }
    }

}