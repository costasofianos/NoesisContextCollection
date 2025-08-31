package org.noesis.codeanalysis.control;

import org.noesis.codeanalysis.util.constants.KotlinContextCollectorConstants;

public class BatchContextCollectorRunner {







     public static void main(String[] args) {

         String stage = KotlinContextCollectorConstants.DEFAULT_STAGE;
         String language = KotlinContextCollectorConstants.DEFAULT_LANGUAGE;
         String workspace = KotlinContextCollectorConstants.DEFAULT_WORKSPACE;


         for (String arg : args) {
             if (arg.startsWith("--stage=")) stage = arg.substring("--stage=".length());
             if (arg.startsWith("--lang=")) language = arg.substring("--lang=".length());
             if (arg.startsWith("--workspace=")) workspace = arg.substring(("--workspace=").length());
         }

         BatchContextCollector batchContextCollector;
        try {
            batchContextCollector = new BatchContextCollector(
                    workspace,
                    language,
                    stage,
                    KotlinContextCollectorConstants.STRATEGIES,
                    KotlinContextCollectorConstants.SUBMIT_LOCALLY,
                    KotlinContextCollectorConstants.CREATE_ANALYSIS_FILES

            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        batchContextCollector.collectResults();
    }



}
