package org.noesis.codeanalysis.collectionstrategies.kotlin.psi;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.noesis.codeanalysis.collectionstrategies.general.ContextCollectionStrategy;
import org.noesis.codeanalysis.computations.general.cache.ComputationFactory;
import org.noesis.codeanalysis.computations.kotlin.psi.KotlinRepoKtFilesToDeclarationsComputation;
import org.noesis.codeanalysis.control.ContextCollector;
import org.noesis.codeanalysis.dataobjects.enums.ElementVisibility;
import org.noesis.codeanalysis.dataobjects.output.ContextEntry;
import org.noesis.codeanalysis.dataobjects.output.ContextEntryWeight;
import org.noesis.codeanalysis.interfaces.PsiElementFormatter;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementContextEntryUtil;
import org.noesis.codeanalysis.util.kotlin.psi.formatters.KotlinDeclarationSignatureFormatter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KotlinPossibleReferencesFromModifiedFilesCollectorStrategy extends ContextCollectionStrategy {
    public KotlinPossibleReferencesFromModifiedFilesCollectorStrategy(ContextCollector contextCollector, ComputationFactory computationFactory) {
        super(contextCollector, computationFactory);
    }

    @Override
    public void populateCollectionStrategyResults() {

        KotlinRepoKtFilesToDeclarationsComputation kotlinRepoKtFilesToDeclarationsComputation = getComputationFactory().getInstance(KotlinRepoKtFilesToDeclarationsComputation.class);
        List<String> modifiedFiles = getContextCollector().getContextCollectionInput().getContextCollectionInputJsonLine().modified();

        addContextEntryInNewGroup("Code Context from Modified Files",getContextEntriesForModifiedFiles(
                modifiedFiles,
                new KotlinDeclarationSignatureFormatter(),
                ContextEntryWeight.of(0.5),
                kotlinRepoKtFilesToDeclarationsComputation
        ));
    }

    public List<ContextEntry> getContextEntriesForModifiedFiles(
            List<String> modifiedFiles,
            PsiElementFormatter psiElementFormatter,
            ContextEntryWeight weight,
            KotlinRepoKtFilesToDeclarationsComputation kotlinRepoKtFilesToDeclarationsComputation) {

        List<PsiElement> allElements = modifiedFiles.stream()
                .flatMap(modifiedFile -> {
                    List<PsiElement> elements = kotlinRepoKtFilesToDeclarationsComputation.getKtFileElements(modifiedFile, ElementVisibility.PUBLIC);
//                    System.out.println("Modified file: " + modifiedFile +
//                            " -> Elements found: " + (elements != null ? elements.size() : "null"));
                    return elements != null ? elements.stream() : Stream.empty();
                })
                .collect(Collectors.toList());


        return KotlinPsiElementContextEntryUtil.getContextEntries(
                allElements,
                psiElementFormatter,
                weight, "getContextEntriesForModifiedFiles");
    }

    @Override
    public void close() throws Exception {

    }
}
