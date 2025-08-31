package org.noesis.codeanalysis.dataobjects.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.noesis.codeanalysis.control.ContextCollector;
import org.noesis.codeanalysis.dataobjects.input.BatchContextCollectionInput;
import org.noesis.codeanalysis.dataobjects.returnobjects.GeneratedFiles;
import org.noesis.codeanalysis.util.constants.KotlinContextCollectorConstants;
import org.noesis.codeanalysis.util.general.CHRF;
import org.noesis.codeanalysis.util.general.FileHelper;
import org.noesis.codeanalysis.util.general.StringUtils;
import org.noesis.codeanalysis.util.html.HtmlResources;
import org.noesis.codeanalysis.util.html.HtmlUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementContextEntryUtil;
import org.noesis.codeanalysis.util.lmstudio.CodeCompletionPromptGenerator;
import org.noesis.codeanalysis.util.lmstudio.LmStudioClient;
import org.noesis.codeanalysis.util.tokens.TokenEstimator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class CollectedContext {

    private List<ContextEntrySetFromStrategy> contextEntriesFromStrategies;
    private final ContextCollector contextCollector;
    private final List<ContextEntryGroup> combinedContextEntryGroups;
    boolean submitLocally;
    String contextCollectionString;
    BatchContextCollectionInput batchContextCollectionInput;

    public CollectedContext(List<ContextEntrySetFromStrategy> contextEntriesFromStrategies, ContextCollector contextCollector, BatchContextCollectionInput batchContextCollectionInput, boolean submitLocally) {
        this.contextEntriesFromStrategies = contextEntriesFromStrategies;
        this.contextCollector = contextCollector;
        this.combinedContextEntryGroups = calculateCombinedContextEntryGroups();
        this.contextCollectionString = calculateContextCollectionString();
        this.batchContextCollectionInput = batchContextCollectionInput;
        this.submitLocally = submitLocally;
    }

    public List<ContextEntrySetFromStrategy> getContextEntrySetsFromStrategies() {
        return contextEntriesFromStrategies;
    }

    public void setContextCollectionResults(List<ContextEntrySetFromStrategy> contextEntriesFromStrategies) {
        this.contextEntriesFromStrategies = contextEntriesFromStrategies;
    }

    private List<ContextEntryGroup> calculateCombinedContextEntryGroups() {
        Set<String> seenTexts = new HashSet<>();
        List<ContextEntryGroup> result = new ArrayList<>();

        // Process each strategy
        for (ContextEntrySetFromStrategy strategy : getContextEntrySetsFromStrategies()) {
            // Process each group in the strategy
            for (ContextEntryGroup group : strategy.contextEntryGroups()) {
                List<ContextEntry> validEntries = new ArrayList<>();

                // Process each entry in the group
                for (ContextEntry entry : group.contextEntries()) {
                    // Skip entries that don't meet our criteria
                    if (entry.contextEntryWeight().value() <= 0.0) {
                        continue;
                    }
                    if (entry.text() == null || entry.text().isEmpty()) {
                        continue;
                    }
                    if (!seenTexts.add(entry.text())) {
                        continue;
                    }

                    validEntries.add(entry);
                }

//                // Sort valid entries by weight in descending order
//                validEntries.sort((e1, e2) -> Double.compare(
//                        e2.contextEntryWeight().value(),
//                        e1.contextEntryWeight().value()
//                ));
                // Sort valid entries by weight in descending order
                validEntries.sort(Comparator
                        .comparing((ContextEntry e) -> e.contextEntryWeight().value(), Comparator.reverseOrder())
                        .thenComparing(e -> e.text().length())
                );

//                                float scoreThreshold = 0.6f;
//                int entriesThreshold = 10;
//                int originalSize = validEntries.size();
//
//                // Count how many entries have weight >= 0.6
//                long entriesAboveThreshold = validEntries.stream()
//                        .filter(entry -> entry.contextEntryWeight().value() >= scoreThreshold)
//                        .count();
//
//                // Only filter if we have more than 5 entries with high weights
//                if (entriesAboveThreshold > 5) {
//
//                    validEntries = validEntries.stream()
//                            .filter(entry -> entry.contextEntryWeight().value() >= 0.6)
//                            .limit(entriesThreshold)
//                            .collect(Collectors.toList());
//                }
//
//                int removedCount = originalSize - validEntries.size();
//                if (removedCount > 0) {
//                    System.out.println("Filtered out " + removedCount + " entries");
//                }


                // Only add groups that have valid entries
                if (!validEntries.isEmpty()) {
                    result.add(new ContextEntryGroup(group.text(), validEntries));
                }
            }
        }

        return result;
    }


    public String toContextCollectionString() {
        return contextCollectionString;
    }

    public String calculateContextCollectionString() {

        List<ContextEntryGroup> groups = calculateCombinedContextEntryGroups();
        return trimGroupsToFitTokenLimit(groups);

    }


    private String trimGroupsToFitTokenLimit(List<ContextEntryGroup> groups) {
        // First try with all content
        String preview = toContextCollectionStringPreview(groups, groups.size() - 1, Integer.MAX_VALUE);
        if (TokenEstimator.estimateTokens(preview) <= KotlinContextCollectorConstants.MAX_TOKENS) {
            return preview;
        }

        int removedEntries = 0;
        // Try different combinations of group and entry indices
        for (int groupIndex = groups.size() - 1; groupIndex >= 0; groupIndex--) {
            for (int entryIndex = groups.get(groupIndex).contextEntries().size() - 1; entryIndex >= 0; entryIndex--) {


                String entryText = groups.get(groupIndex).contextEntries().get(entryIndex).text();

                preview = toContextCollectionStringPreview(groups, groupIndex, entryIndex);


                if (TokenEstimator.estimateTokens(preview) <= KotlinContextCollectorConstants.MAX_TOKENS) {
                    if (removedEntries > 0) {
                        System.out.println("Warning: Removed " + removedEntries + " entries to fit token limit of "+KotlinContextCollectorConstants.MAX_TOKENS);
                    }
                    return preview;
                }
                removedEntries++;

            }
        }

        // If we get here, we couldn't get under the token limit even after trying all combinations
        System.out.println("Warning: Could not reduce context size below " + KotlinContextCollectorConstants.MAX_TOKENS + " tokens. Keeping only first entry.");
        return toContextCollectionStringPreview(groups, 1, 1); // Empty result
    }

    private String toContextCollectionStringPreview(List<ContextEntryGroup> groups, int maxGroupIndex, int maxEntryIndex) {
        StringBuilder result = new StringBuilder();
//        result.append("<|repo_name|>")
//                .append(StringUtils.getLastToken(getContextCollector().getContextCollectionInput().getContextCollectionInputJsonLine().repo()))
//                .append("\n");

        boolean isFirstGroup = true;
        for (int groupIndex = 0; groupIndex <= Math.min(maxGroupIndex, groups.size() - 1); groupIndex++) {
            ContextEntryGroup group = groups.get(groupIndex);

            // Add separator between groups
            if (!isFirstGroup) {
                result.append("\n\n");
            }
            isFirstGroup = false;

            // Add entries from the group, up to maxEntryIndex
            List<ContextEntry> entries = group.contextEntries();
            for (int entryIndex = 0; entryIndex <= Math.min(maxEntryIndex, entries.size() - 1); entryIndex++) {
                result.append(entries.get(entryIndex).text()).append("\n\n");
            }
        }

        return result.toString();
    }


    public ContextCollector getContextCollector() {
        return contextCollector;
    }

    public GeneratedFiles writeToOutputFolder() throws IOException {
        File inputFile = writeInputToOutputFolder();
        File resultFile = writeResultToOutputFolder();
        return new GeneratedFiles(
                inputFile.getAbsolutePath(),
                resultFile.getAbsolutePath()
        );
    }


    private String getLmStudioResult() {

        String prompt = CodeCompletionPromptGenerator.createCodeCompletionPrompt(
                getContextCollector().getContextCollectionInput().getContextCollectionInputJsonLine().prefix(),
                getContextCollector().getContextCollectionInput().getContextCollectionInputJsonLine().suffix(),
                toContextCollectionString());

        try {
            LmStudioClient lmStudioClient = new LmStudioClient();
           String response = lmStudioClient.getCompletion(prompt);
           response = StringUtils.stripCodeBlockMarkers(response);
            //System.out.println("RESPONSE: "+response);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get completion from LM Studio", e);
        }
    }

    public File writeResultToOutputFolder() throws IOException {
        File resultFile = new File(getContextCollectionOutputRootFolder(), "result.html");

        StringBuilder localLmStudioResult = null;
        if (submitLocally) {
            localLmStudioResult = new StringBuilder();
            String target = getContextCollector().getContextCollectionInput().getTarget();
            String completionCode = getLmStudioResult();
            //System.out.println("TARGET: " + target);
            //System.out.println("COMPLETION CODE: " + completionCode);
            CHRF chrf = new CHRF();
            CHRF.Score  score = chrf.compute(target, completionCode);
            double result = score.fscore;
            localLmStudioResult.append("Target\n\n");
            localLmStudioResult.append(target);
            localLmStudioResult.append("\n\nCode Completion\n\n");
            localLmStudioResult.append(completionCode);
            localLmStudioResult.append("\n\nResult ");
            localLmStudioResult.append(result);
            Files.writeString(resultFile.toPath(), HtmlResources.wrapInHtmlPage(toHtml(localLmStudioResult.toString())));

        } else {
            Files.writeString(resultFile.toPath(), HtmlResources.wrapInHtmlPage(toHtml(null)));
        }

        return resultFile;
    }

    public File writeInputToOutputFolder() throws IOException {
        File inputFile = new File(getContextCollectionOutputRootFolder(), "input.html");
        inputFile.getParentFile().mkdirs();

        if (!inputFile.exists()) {
            boolean created = inputFile.createNewFile();
            if (!created) {
                throw new IOException("Failed to create file: " + inputFile.getAbsolutePath());
            }
        } else if (inputFile.isDirectory()) {
            // If it exists but is a directory, handle the error
            throw new IOException("Path exists but is a directory: " + inputFile.getAbsolutePath());
        }

        Files.writeString(inputFile.toPath(), HtmlResources.wrapInHtmlPage(getContextCollector().getContextCollectionInput().toHtml()));
        return inputFile;
    }


    public String toHtml(String localLmStudioResult) {
        StringBuilder html = new StringBuilder();
        html.append("<table>\n");

        // Add LM Studio result with light pastel green background and a copy button
        if (localLmStudioResult != null && !localLmStudioResult.isEmpty()) {
            html.append("<tr><th>LM Studio Result</th></tr>\n")
                    .append("<tr><td style='background-color: #e8f5e9; position: relative;'><pre>")
                    .append(localLmStudioResult)
                    .append("</pre></td></tr>\n");
        }


        html.append("<tr><th>Collected Context</th></tr>\n");
// Add context collection string with light blue background and a copy button
        html.append("<tr><td style='background-color: #f0f8ff; position: relative;'><pre>")
//                .append("<button onclick='navigator.clipboard.writeText(`")
//                .append(escapeJavaScript(toContextCollectionString()))
//                .append("`)' ")
//                .append("style='position: absolute; top: 10px; right: 10px;'>Copy</button>")
                .append(HtmlUtil.escapeHtml(toContextCollectionString()))
                .append("</pre></td></tr>\n");





        html.append("<tr><th>Collection Analysis</th></tr>\n");
        html.append("<tr><td><pre>");
        html.append(KotlinPsiElementContextEntryUtil.createContextEntryGroupsHtmlTable(combinedContextEntryGroups));
        html.append("</pre></td></tr>\n");

        for (ContextEntrySetFromStrategy contextEntrySetFromStrategy : contextEntriesFromStrategies) {
            html.append("<tr><th>")
                    .append(contextEntrySetFromStrategy.contextCollectionStrategy().getClass().getName())
                    .append("</th></tr>\n")
                    .append("<tr><td><pre>")
                    .append(contextEntrySetFromStrategy.toHtml())
                    .append("</pre></td></tr>\n");
        }

        html.append("</table>");
        return html.toString();
    }

    // Helper method for JavaScript string escaping
    private String escapeJavaScript(String text) {
        return text.replace("\\", "\\\\")
                .replace("`", "\\`")
                .replace("$", "\\$");
    }



    private File getContextCollectionOutputRootFolder() {
        return
                new File(FileHelper.addToPath(
                        contextCollector.getContextCollectionInput().getContextCollectionInputBatch().getAnalysisOutputFolder(),
                        contextCollector.getContextCollectionInput().getContextCollectionInputJsonLine().getRepoFolderString()
                ));
    }

    public String toJsonLine() {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("context", toContextCollectionString());
        try {
            return mapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JSON", e);
        }

    }

    private File getRootOutputFolder() {
        return batchContextCollectionInput.getRootOutputFolder();
    }


    public String getContextCollectionString() {
        return contextCollectionString;
    }

    public List<ContextEntryGroup> getCombinedContextEntryGroups() {
        return combinedContextEntryGroups;
    }
}