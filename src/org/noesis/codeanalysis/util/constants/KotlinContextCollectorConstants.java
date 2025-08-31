package org.noesis.codeanalysis.util.constants;

import org.noesis.codeanalysis.collectionstrategies.general.ContextCollectionStrategy;
import org.noesis.codeanalysis.collectionstrategies.kotlin.psi.KotlinPossibleReferencesFromEditCollectorStrategy;
import org.noesis.codeanalysis.collectionstrategies.kotlin.psi.weightedassociations.KotlinSemanticPsiTreeWeightedAssociationsStrategy;

import java.util.ArrayList;
import java.util.List;

public class KotlinContextCollectorConstants {

    public static final String DEFAULT_WORKSPACE = "/mnt/ContextCollectionWorkspace/";
//    public static final String DATA_FOLDER_ROOT_PATH = "/Users/costa/Development/JetBrainsContextCollection/";

    public static final String DEFAULT_LANGUAGE = "kotlin";
    public static final String DEFAULT_STAGE = "private";
    public static final String OUTPUT_SUBFOLDER = "predictions";
    public static final boolean SUBMIT_LOCALLY = false;
    public static final boolean CREATE_ANALYSIS_FILES = false;


    public static final List<List<Class<? extends ContextCollectionStrategy>>> STRATEGIES =
            new ArrayList<>(List.of(
                    new ArrayList<>(List.of(
                            KotlinSemanticPsiTreeWeightedAssociationsStrategy.class
                    )),
                    new ArrayList<>(List.of(
                            KotlinPossibleReferencesFromEditCollectorStrategy.class
                    ))
            ));



    public static final String MISSING_CODE_COMMENT = "/*Missing Code*/";
    public static final int MAX_TOKENS = 6000;
    public static final float METHOD_BODY_THRESHOLD = 0.85f;

    public static final String FILE_SEP_SYMBOL = "<|file_sep|>";
    public static final String FILE_COMPOSE_FORMAT = "%s%s\n%s";
}
