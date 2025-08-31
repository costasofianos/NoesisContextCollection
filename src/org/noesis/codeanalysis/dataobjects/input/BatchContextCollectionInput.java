package org.noesis.codeanalysis.dataobjects.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.noesis.codeanalysis.dataobjects.enums.Language;
import org.noesis.codeanalysis.dataobjects.enums.Stage;
import org.noesis.codeanalysis.util.constants.KotlinContextCollectorConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class BatchContextCollectionInput {

        private final Stage stage;
        private final Language language;
        private final File dataFolder;
        private final String rootFolderPath;
        private final boolean submitLocally;


    private final File rootFolder;
        private final List<ContextCollectionInput> contextCollectionInputs;
        public BatchContextCollectionInput(String rootFolderPath, String language, String stage, boolean submitLocally) throws Exception {

            this.rootFolderPath = rootFolderPath;
            this.rootFolder = new File(rootFolderPath);
            this.dataFolder = new File(rootFolder, "data");
            this.language = Language.fromString(language);
            this.stage = Stage.fromString(stage);
            this.submitLocally = submitLocally;
            this.contextCollectionInputs = loadContextCollectionInputs(this.language, this.stage);

        }

    public String getBatchSummary() {
        return "LANGUAGE = "+language+", "+"STAGE = "+stage.getValue()+", "+"DATA FOLDER = "+dataFolder.getAbsolutePath();
    }

        public Stage getStage() {
            return stage;
        }

        public Language getLanguage() {
            return language;
        }

        public File getDataFolder() {
            return dataFolder;
        }

        public File getRepositoriesFolder() {
            return new File(getRepositoriesFolderPath());
        }

        public String getRepositoriesFolderPath() {
            return dataFolder.getAbsolutePath() + "/repositories-"+language.toString().toLowerCase()+"-"+stage.getValue()+"/";
        }

        public List<ContextCollectionInput> getContextCollectionInputs() {
          return contextCollectionInputs;
        }

    private List<ContextCollectionInput> loadContextCollectionInputs(Language language, Stage stage)
            throws Exception {
        if (!dataFolder.isDirectory()) {
            throw new IllegalArgumentException("Data folder root must be a directory: " + dataFolder);
        }

        Path jsonlPath = getJsonlPath();

        if (!Files.isReadable(jsonlPath)) {
            throw new IOException("File not readable or doesn't exist: " + jsonlPath);
        }

        List<ContextCollectionInput> inputs = new ArrayList<>();
        int lineNumber = 0;


        try (BufferedReader reader = Files.newBufferedReader(jsonlPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    inputs.add(new ContextCollectionInput(this, line));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (inputs.isEmpty()) {
            throw new IOException("No valid inputs were loaded from " + jsonlPath);
        }
        // Load targets if submitLocally is true
        if (submitLocally) {

            Path targetJsonlPath = getTargetJsonlPath();
            System.out.println("Loading target ["+targetJsonlPath+"]");
            if (Files.isReadable(targetJsonlPath)) {
                try (BufferedReader reader = Files.newBufferedReader(targetJsonlPath)) {
                    String line;
                    int targetLineNumber = 0;
                    while ((line = reader.readLine()) != null) {
                        if (targetLineNumber < inputs.size()) {
                            try {
                                ContextCollectionTargetJsonLine targetLine = ContextCollectionTargetJsonLine.fromJson(line);
                                //System.out.println("Setting target on input "+inputs.get(targetLineNumber).getContextCollectionInputJsonLine().revision());
                                //System.out.println("Found target : "+targetLine.target());

                                inputs.get(targetLineNumber).setTarget(targetLine.target());
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException("Failed to parse target line " + targetLineNumber, e);
                            }
                        }
                        targetLineNumber++;
                    }
                }
            }
        }
        return inputs;
    }

   public Path getJsonlPath() {
       return dataFolder.toPath().resolve(
               language.toString().toLowerCase() + "-" + stage.getValue() + ".jsonl");
   }
    public Path getTargetJsonlPath() {
        return dataFolder.toPath().resolve(
                language.toString().toLowerCase() + "-" + stage.getValue() + "-target.jsonl");
    }

    public File getRootFolder() {
        return rootFolder;
    }

    public File getRootOutputFolder() {
        File outputFolder = new File(getRootFolder(), KotlinContextCollectorConstants.OUTPUT_SUBFOLDER);
        outputFolder.mkdirs();
        return outputFolder;
    }

    public File getAnalysisOutputFolder() {
        String subfolderName = getLanguage().name() + "-" + getStage().getValue();
        File outputFolder = new File(getRootOutputFolder(), subfolderName);
        outputFolder.mkdirs();
        return outputFolder;
    }


    public String getRootFolderPath() {
        return rootFolderPath;
    }

    public BatchContextCollectionInput clone() {
        // Create new instance using the same constructor parameters
        try {
            return new BatchContextCollectionInput(
                    this.rootFolderPath,
                    this.language.toString(),
                    this.stage.getValue(),
                    this.submitLocally
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to clone ContextCollectionInputBatch", e);
        }
    }

    public String getOutputFileName() {
        return getLanguage()+"-"+getStage().getValue()+"-psi.jsonl";
    }


}