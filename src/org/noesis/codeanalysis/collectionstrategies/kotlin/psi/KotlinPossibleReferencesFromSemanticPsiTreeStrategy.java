package org.noesis.codeanalysis.collectionstrategies.kotlin.psi;

import org.jetbrains.kotlin.psi.KtFile;
import org.noesis.codeanalysis.collectionstrategies.general.ContextCollectionStrategy;
import org.noesis.codeanalysis.computations.general.cache.ComputationFactory;
import org.noesis.codeanalysis.computations.kotlin.psi.KotlinEditFilePsiTreeComputation;
import org.noesis.codeanalysis.computations.kotlin.psi.KotlinEditFileSemanticPsiTreeComputation;
import org.noesis.codeanalysis.computations.kotlin.psi.KotlinRepoPsiTreeCollectionInputComputation;
import org.noesis.codeanalysis.control.ContextCollector;
import org.noesis.codeanalysis.dataobjects.enums.ElementVisibility;
import org.noesis.codeanalysis.dataobjects.output.ContextEntry;
import org.noesis.codeanalysis.dataobjects.output.ContextEntryWeight;
import org.noesis.codeanalysis.util.constants.KotlinContextCollectorConstants;
import org.noesis.codeanalysis.util.html.HtmlUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiTreeCompactHtmlUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinSemanticPsiTreeCompactHtmlUtil;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeTopLevelFunction;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeClassOrObject;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeFile;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeFunction;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeRoot;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeNode;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class KotlinPossibleReferencesFromSemanticPsiTreeStrategy extends ContextCollectionStrategy  {

    private KotlinEditFilePsiTreeComputation kotlinEditFilePsiTreeComputation;
    private KotlinEditFileSemanticPsiTreeComputation kotlinEditFileSemanticPsiTreeComputation;
    KotlinRepoPsiTreeCollectionInputComputation kotlinRepoPsiTreeCollectionInputComputation;
    private KotlinSemanticPsiTreeRoot kotlinSemanticPsiTreeRoot;

    public KotlinPossibleReferencesFromSemanticPsiTreeStrategy(ContextCollector contextCollector, ComputationFactory computationFactory) {

        super(contextCollector, computationFactory);
        kotlinEditFilePsiTreeComputation = getComputationFactory().getInstance(KotlinEditFilePsiTreeComputation.class);
        kotlinEditFileSemanticPsiTreeComputation =  getComputationFactory().getInstance(KotlinEditFileSemanticPsiTreeComputation.class);
        kotlinSemanticPsiTreeRoot = kotlinEditFileSemanticPsiTreeComputation.getKotlinSemanticPsiTreeRootContainingEditFile();
        kotlinRepoPsiTreeCollectionInputComputation = getComputationFactory().getInstance(KotlinRepoPsiTreeCollectionInputComputation.class);
        //GitComputation gitComputation = getComputationFactory().getInstance(GitComputation.class);
    }

    @Override
    public void populateCollectionStrategyResults() {

        //find edit file and matching semantic psi element
        KtFile editKtFile = kotlinEditFilePsiTreeComputation.getEditFilePsiTree();
        if (editKtFile == null) {
            System.out.println("Edit File PSI Tree not found");
            return;
        }

        Optional<KotlinSemanticPsiTreeFile> editSemanticPsiTreeFileOptional = kotlinSemanticPsiTreeRoot.findMatchingFile(editKtFile);

        if (!editSemanticPsiTreeFileOptional.isPresent()) {
            System.out.println("Edit File not found in Semantic Tree "+editKtFile.getVirtualFilePath());
            return;
        }

        KotlinSemanticPsiTreeFile editSemanticPsiTreeFile = editSemanticPsiTreeFileOptional.get();

        Set<String> referencedTypeNameInEditFile = editSemanticPsiTreeFile.getAllReferencedTypeNamesInSubtree(true);
        addExplanation("Referenced Type Names in Edit File", HtmlUtil.generateStringListHtmlTable(referencedTypeNameInEditFile));

        Map<String, Set<KotlinSemanticPsiTreeClassOrObject>> classOrObjectMap = kotlinSemanticPsiTreeRoot.collectNodeMapForType(KotlinSemanticPsiTreeClassOrObject.class, ElementVisibility.PUBLIC);

        addExplanation("Edit File Tree ", KotlinPsiTreeCompactHtmlUtil.generatePsiTreeHtml(editKtFile));
        addExplanation("Semantic Tree of Edit File", KotlinSemanticPsiTreeCompactHtmlUtil.generateHtml(editSemanticPsiTreeFile));

        addExplanation("Semantic Tree of Source", KotlinSemanticPsiTreeCompactHtmlUtil.generateHtml(kotlinEditFileSemanticPsiTreeComputation.getKotlinSemanticPsiTreeRootContainingEditFile()));

        List<ContextEntry> classOrObjectContextEntries = createContextEntries(classOrObjectMap, referencedTypeNameInEditFile, 0.7f, 0.8f, false, "Types in Edit File");
        addContextEntryInNewGroup("Classes or objects related to edit file",classOrObjectContextEntries);

        /****  Functions  ****/
        Predicate<KotlinSemanticPsiTreeNode> missingCodeChildPredicate = node ->
                node instanceof KotlinSemanticPsiTreeChildNode<?> childNode &&
                        childNode.getPsiElement().getText().contains(KotlinContextCollectorConstants.MISSING_CODE_COMMENT);
        KotlinSemanticPsiTreeFunction missingCodeFunction = editSemanticPsiTreeFile.findNodesOfType(KotlinSemanticPsiTreeFunction.class, missingCodeChildPredicate).stream().findFirst().orElse(null);

        if (missingCodeFunction != null) {
            System.out.println("Found missing function "+missingCodeFunction.getTypeName()+" in "+missingCodeFunction.getContainingKtFile().getVirtualFilePath());
            //addExplanation("Text of Missing Code Function", HtmlUtil.generateStringListHtmlTable(List.of(missingCodeFunction.getPsiElement().getText())));
            Set<String> referencedTypeNameInEditFileMissingCodeFunction =
                    KotlinPsiElementUtil.filterReferencedTypesByPosition(
                            missingCodeFunction.getPsiElement(),
                            missingCodeFunction.getAllReferencedTypeNamesInSubtree(true),
                            KotlinContextCollectorConstants.MISSING_CODE_COMMENT
                    );

            //addExplanation("Referenced Type Names in Missing Code Function", HtmlUtil.generateStringListHtmlTable(referencedTypeNameInEditFileMissingCodeFunction));

            List<ContextEntry> classOrObjectContextEntriesForMissingCodeFunction = createContextEntries(classOrObjectMap, referencedTypeNameInEditFile, 0.79f, 0.89f, false, "Classes in Edited Function");
            addContextEntryInNewGroup("Classes or objects related to current method in edit file",classOrObjectContextEntriesForMissingCodeFunction);




            /**** Get Top Level Functions ****/
            //only need the functions map here
            Map<String, Set<KotlinSemanticPsiTreeTopLevelFunction>> topLevelFunctionMap = kotlinSemanticPsiTreeRoot.collectNodeMapForType(KotlinSemanticPsiTreeTopLevelFunction.class, ElementVisibility.PUBLIC);
            List<ContextEntry> functionEntries = createContextEntries(topLevelFunctionMap, referencedTypeNameInEditFileMissingCodeFunction, 0.78f, 0.88f, false, "Functions in Edited Function");
            addContextEntryInNewGroup("Functions related to edit file",functionEntries);
        }
    }

    public <T extends KotlinSemanticPsiTreeChildNode<?>> List<ContextEntry> createContextEntries(
            Map<String, Set<T>> nodeMap,
            Set<String> referencedTypeNames,
            double unmodifiedWeight,
            double modifiedWeight,
            boolean includeFullText,
            String description
    ) {
        return referencedTypeNames.stream()
                .filter(nodeMap::containsKey)
                .flatMap(typeName ->
                        nodeMap.get(typeName).stream().map(kotlinSemanticPsiTreeChildNode -> {
                            KtFile ktFile = kotlinSemanticPsiTreeChildNode.getContainingKtFile();
                            double weight = kotlinRepoPsiTreeCollectionInputComputation.isModified(ktFile) ?
                                    modifiedWeight : unmodifiedWeight;

                            if (weight == 0.79d || weight == 0.89d) {
                                System.out.println("#### Found top level type match in missing code " +
                                        kotlinSemanticPsiTreeChildNode.getTypeName());
                            }

                            if (kotlinSemanticPsiTreeChildNode instanceof KotlinSemanticPsiTreeTopLevelFunction) {
                                System.out.println("***** Found top level function match in " +
                                        kotlinSemanticPsiTreeChildNode.getTypeName());
                            }

                            String context = includeFullText ?
                                    kotlinSemanticPsiTreeChildNode.getPsiElement().getText() :
                                    kotlinSemanticPsiTreeChildNode.getPublicContext();

                            return new ContextEntry(
                                    new File(ktFile.getVirtualFilePath()),
                                    context,
                                    new ContextEntryWeight(weight),
                                    "(" + kotlinSemanticPsiTreeChildNode.getClass().getSimpleName() +
                                            " - " + description + ") " + typeName
                            );
                        })
                )
                .collect(Collectors.toList());
    }


    @Override
    public void close() throws Exception {
        kotlinEditFilePsiTreeComputation = null;
        kotlinEditFileSemanticPsiTreeComputation = null;
        kotlinRepoPsiTreeCollectionInputComputation = null;
        kotlinSemanticPsiTreeRoot = null;
    }
}
