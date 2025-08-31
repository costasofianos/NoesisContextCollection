package org.noesis.codeanalysis.control;

import org.noesis.codeanalysis.collectionstrategies.general.ContextCollectionStrategy;
import org.noesis.codeanalysis.dataobjects.input.BatchContextCollectionInput;
import org.noesis.codeanalysis.dataobjects.input.ContextCollectionInput;
import org.noesis.codeanalysis.dataobjects.output.CollectedContext;
import org.noesis.codeanalysis.dataobjects.returnobjects.GeneratedFiles;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BatchContextCollector {


    private final BatchContextCollectionInput batchContextCollectionInput;
    private final List<List<Class<? extends ContextCollectionStrategy>>> contextCollectionStrategyClasses;
    //private final List<String> contextCollectionStrategyClasses;
    File outputFile;
    private final boolean submitLocally;
    private final boolean createAnalysisFiles;


    public BatchContextCollector(String rootFolderPath, String language, String stage,
                                 List<List<Class<? extends ContextCollectionStrategy>>> contextCollectionStrategyClasses,
                                 boolean submitLocally,
                                 boolean createAnalysisFiles) throws Exception {

        this.submitLocally = submitLocally;
        this.createAnalysisFiles = createAnalysisFiles;
        batchContextCollectionInput = new BatchContextCollectionInput(rootFolderPath, language, stage, submitLocally);
        this.contextCollectionStrategyClasses = contextCollectionStrategyClasses;


        outputFile = new File(batchContextCollectionInput.getRootOutputFolder(), getBatchContextCollectionInput().getOutputFileName());

        // Create parent directories if they don't exist
        outputFile.getParentFile().mkdirs();

        // Create or truncate the file
        try {
            Files.write(outputFile.toPath(), new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize output file: " + outputFile.getAbsolutePath(), e);
        }

    }

    public void collectResults() {

        System.out.println("[" + getBatchContextCollectionInput().getBatchSummary() + "] Collecting Results for Batch");
        Map<String, GeneratedFiles> filesByRevision = new LinkedHashMap<>();


        int i = 1;
        for (ContextCollectionInput contextCollectionInput : batchContextCollectionInput.getContextCollectionInputs()) {

            try (ContextCollector contextCollector = new ContextCollector(contextCollectionInput.clone(), contextCollectionStrategyClasses, batchContextCollectionInput.clone(), submitLocally)) {
                System.out.println("[" + (i++) + "][" + contextCollector.getContextCollectionInput().getCollectionInputSummary() + "] Collecting Results for Repo");

                CollectedContext collectedContext = contextCollector.collectResults();
                if (createAnalysisFiles) {
                    try {
                        GeneratedFiles generatedFiles = collectedContext.writeToOutputFolder();
                        filesByRevision.put(
                                collectedContext.getContextCollector().getContextCollectionInput().getContextCollectionInputJsonLine().revision(),
                                generatedFiles);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                appendToFile(collectedContext.toJsonLine());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (createAnalysisFiles) {
            writeNavigationFile(filesByRevision);
        }
    }

    public void appendToFile(String jsonLine) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                outputFile.toPath(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            writer.write(jsonLine);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to append to file: " + outputFile.getAbsolutePath(), e);
        }
    }


//    public void appendToFile(String jsonLine) {
//        try {
//            // Append the line with a newline character
//            Files.write(
//                    outputFile.toPath(),
//                    (jsonLine + "\n").getBytes(),
//                    StandardOpenOption.CREATE,
//                    StandardOpenOption.APPEND
//            );
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to append to file: " + outputFile.getAbsolutePath(), e);
//        }
//    }

    private void writeNavigationFile(Map<String, GeneratedFiles> filesByRevision) {
        try {
            File navigationFile = new File(getAnalysisOutputFolder(), "collectedContext.html");
            String navigationContent = generateNavigationHtml(filesByRevision);
            Files.writeString(navigationFile.toPath(), navigationContent);
        } catch (IOException e) {
            System.err.println("Failed to write navigation file: " + e.getMessage());
        }
    }


    private String generateNavigationHtml(Map<String, GeneratedFiles> filesByRevision) {
        StringBuilder html = new StringBuilder();
        html.append("""
        <!DOCTYPE html>
        <html>
        <head>
            <title>Navigation</title>
            <style>
                body {
                    margin: 0;
                    padding: 0;
                    display: flex;
                    min-height: 100vh;
                }
                .nav-sidebar {
                    width: 300px;
                    background-color: #f8f9fa;
                    border-right: 1px solid #dee2e6;
                    padding: 20px;
                    overflow-y: auto;
                }
                .main-content {
                    flex-grow: 1;
                    padding: 20px;
                }
                .tree {
                    font-family: monospace;
                }
                .tree ul {
                    list-style-type: none;
                    padding-left: 20px;
                    margin: 0;
                }
                .node {
                    padding: 4px;
                    border-radius: 3px;
                    margin: 2px 0;
                }
                .node:hover {
                    background-color: #e9ecef;
                }
                .folder {
                    color: #495057;
                    font-weight: bold;
                    cursor: pointer;
                    user-select: none;
                    white-space: nowrap;
                }
                .folder::before {
                    content: '▶';
                    display: inline-block;
                    width: 12px;
                    transition: transform 0.2s;
                }
                .folder:not(.collapsed)::before {
                    transform: rotate(90deg);
                }
                .folder.collapsed + ul {
                    display: none;
                }
                .file {
                    color: #2196F3;
                    margin-left: 12px;
                }
                a {
                    text-decoration: none;
                    color: inherit;
                }
                a:hover {
                    text-decoration: underline;
                }
                iframe {
                    width: 100%;
                    height: 100%;
                    border: none;
                }
            </style>
            <script>
            function toggleNode(element) {
                element.classList.toggle('collapsed');
            }
            </script>
        </head>
        <body>
            <div class="nav-sidebar">
                <div class="tree">
                    <ul>
                        <li class="node">
                            <span class="folder" onclick="toggleNode(this)">Main</span>
                            <ul>
                                <li class="node"><a href=""");
        html.append("../").append(getBatchContextCollectionInput().getOutputFileName());

        html.append("""
                            target="content-frame" class="file">Main Result</a></li>
                            </ul>
                        </li>
                        """);

        // Add revision nodes with their files
        for (Map.Entry<String, GeneratedFiles> entry : filesByRevision.entrySet()) {
            String revision = entry.getKey();
            String truncatedRevision = revision.length() > 10 ? revision.substring(0, 10) + "..." : revision;
            GeneratedFiles files = entry.getValue();
            //System.out.println("Analysis Output Folder: " + getAnalysisOutputFolder().getAbsolutePath());
            String inputPath = getRelativePath(getAnalysisOutputFolder(), new File(files.inputFilePath()));
            String resultPath = getRelativePath(getAnalysisOutputFolder(), new File(files.resultFilePath()));


            html.append(String.format("""
                        <li class="node">
                            <span class="folder collapsed" onclick="toggleNode(this)">%s</span>
                            <ul>
                                <li class="node"><a href="%s" target="content-frame" class="file">Input</a></li>
                                <li class="node"><a href="%s" target="content-frame" class="file">Result</a></li>
                            </ul>
                        </li>
                """, truncatedRevision, inputPath, resultPath));
        }

        html.append("""
                    </ul>
                </div>
            </div>
            <div class="main-content">
                <iframe name="content-frame" src="mainResult.jsonl"></iframe>
            </div>
        </body>
        </html>
    """);

        return html.toString();
    }

    private String getRelativePath(File root, File target) {
        return root.toURI().relativize(target.toURI()).getPath();
    }


    private File getRootOutputFolder() {
        return this.getBatchContextCollectionInput().getRootOutputFolder();
    }
    private File getAnalysisOutputFolder() {
        return this.getBatchContextCollectionInput().getAnalysisOutputFolder();
    }

    public BatchContextCollectionInput getBatchContextCollectionInput() {
        return batchContextCollectionInput;
    }

    public boolean isSubmitLocally() {
        return submitLocally;
    }
}
