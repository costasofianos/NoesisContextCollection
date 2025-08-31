package org.noesis.codeanalysis.collectionstrategies.kotlin.psi.weightedassociations;

import org.jetbrains.kotlin.psi.KtFile;
import org.noesis.codeanalysis.collectionstrategies.general.ContextCollectionStrategy;
import org.noesis.codeanalysis.collectionstrategies.kotlin.psi.weightedassociations.functionevaluators.*;
import org.noesis.codeanalysis.computations.general.cache.ComputationFactory;
import org.noesis.codeanalysis.computations.kotlin.psi.KotlinEditFilePsiTreeComputation;
import org.noesis.codeanalysis.computations.kotlin.psi.KotlinEditFileSemanticPsiTreeComputation;
import org.noesis.codeanalysis.computations.kotlin.psi.KotlinRepoPsiTreeCollectionInputComputation;
import org.noesis.codeanalysis.control.ContextCollector;
import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightCalculator;
import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightComponentEvaluator;
import org.noesis.codeanalysis.dataobjects.associations.SemanticNodeAssociation;
import org.noesis.codeanalysis.dataobjects.enums.ElementVisibility;
import org.noesis.codeanalysis.dataobjects.output.ContextEntry;
import org.noesis.codeanalysis.dataobjects.output.ContextEntryGroup;
import org.noesis.codeanalysis.dataobjects.output.ContextEntryWeight;
import org.noesis.codeanalysis.dataobjects.semanticpsi.*;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPart;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPsiCallableReference;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPsiTypeReference;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticReference;
import org.noesis.codeanalysis.util.constants.KotlinContextCollectorConstants;
import org.noesis.codeanalysis.util.files.FileSearchUtil;
import org.noesis.codeanalysis.util.html.HtmlUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementUtil;
import org.noesis.codeanalysis.util.kotlin.psi.associations.KotlinSemanticTreeCodeAssociationsUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class KotlinSemanticPsiTreeWeightedAssociationsStrategy extends ContextCollectionStrategy {

    private KotlinEditFilePsiTreeComputation kotlinEditFilePsiTreeComputation;
    private KotlinEditFileSemanticPsiTreeComputation kotlinEditFileSemanticPsiTreeComputation;
    KotlinRepoPsiTreeCollectionInputComputation kotlinRepoPsiTreeCollectionInputComputation;
    private KotlinSemanticPsiTreeRoot kotlinSemanticPsiTreeRoot;

    public KotlinSemanticPsiTreeWeightedAssociationsStrategy(ContextCollector contextCollector, ComputationFactory computationFactory) {

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
        } else {
            //System.out.println("Edit File found in Semantic Tree "+editSemanticPsiTreeFileOptional.get().getSimpleFileName());
        }

//        String fileToBeInvestigatedName = "AppBar.kt";
//        List<KotlinSemanticPsiTreeFile> filesToBeInvestigated = kotlinSemanticPsiTreeRoot.findFileNodesByName(fileToBeInvestigatedName);
//        for (KotlinSemanticPsiTreeFile fileToBeInvestigated : filesToBeInvestigated) {
//            addExplanation("[SEMANTIC]"+fileToBeInvestigated.getContainingKtFile().getVirtualFilePath(), KotlinSemanticPsiTreeCompactHtmlUtil.generateHtml(fileToBeInvestigated));
//            addExplanation("[PSI]"+fileToBeInvestigated.getContainingKtFile().getVirtualFilePath(), KotlinPsiTreeCompactHtmlUtil.generatePsiTreeHtml(fileToBeInvestigated.getPsiElement()));
//        }

        //Get all maps
        //Map<String, KotlinSemanticPsiTreeTopLevelFunction> topLevelFunctionMap = kotlinSemanticPsiTreeRoot.collectNodeMapForType(KotlinSemanticPsiTreeTopLevelFunction.class, ElementVisibility.PUBLIC);
        //Map<String, KotlinSemanticPsiTreeMemberFunction> membrFunctionMap = kotlinSemanticPsiTreeRoot.collectNodeMapForType(KotlinSemanticPsiTreeMemberFunction.class, ElementVisibility.PUBLIC, ElementVisibility.NON_PUBLIC);

//        Map<String, KotlinSemanticPsiTreeChildNode<?>> allNodesMap = new HashMap<>();
//        allNodesMap.putAll(classOrObjectMap);
//        allNodesMap.putAll(topLevelFunctionMap);
//        allNodesMap.putAll(membrFunctionMap);

        //Get all the code tokens
        KotlinSemanticPsiTreeFile editSemanticPsiTreeFile = editSemanticPsiTreeFileOptional.get();
        List<KotlinSemanticPsiTypeReference> semanticTypeReferences = editSemanticPsiTreeFile.getAllReferencesOfTypeInSubtree(KotlinSemanticPsiTypeReference.class);
        //addExplanation("Semantic Referenced Types", HtmlUtil.generateStringListHtmlTable(semanticTypeReferences.stream().map(Objects::toString).toList()));

        //addSemanticReferencesExplanation("Semantic Type References", semanticTypeReferences);

        List<KotlinSemanticPsiCallableReference> semanticCallableReferences = editSemanticPsiTreeFile.getAllReferencesOfTypeInSubtree(KotlinSemanticPsiCallableReference.class);
        //addExplanation("Semantic Type References", HtmlUtil.generateStringListHtmlTable(semanticCallableReferences.stream().map(Objects::toString).toList()));

        //addSemanticReferencesExplanation("Semantic Callable References", semanticCallableReferences);

        //addExplanation("Edit File Tree ", KotlinPsiTreeCompactHtmlUtil.generatePsiTreeHtml(editKtFile));
        //addExplanation("Semantic Tree of Edit File", KotlinSemanticPsiTreeCompactHtmlUtil.generateHtml(editSemanticPsiTreeFile));

        List<KotlinSemanticReference> allSemanticReferences = new ArrayList<>();
        allSemanticReferences.addAll(semanticTypeReferences);
        allSemanticReferences.addAll(semanticCallableReferences);

       // addExplanation("Psi Tree of source", KotlinPsiTreeCompactHtmlUtil.generatePsiTreeHtml(kotlinEditFilePsiTreeComputation.getSourceFolderPsiTreeContainingEditedFile());
        //addExplanation("Semantic Tree of Source", KotlinSemanticPsiTreeCompactHtmlUtil.generateHtml(kotlinEditFileSemanticPsiTreeComputation.getKotlinSemanticPsiTreeRootContainingEditFile()));

        //Edit Function
        Predicate<KotlinSemanticPsiTreeNode> missingCodeChildPredicate = node ->
                node instanceof KotlinSemanticPsiTreeChildNode<?> childNode &&
                        childNode.getPsiElement().getText().contains(KotlinContextCollectorConstants.MISSING_CODE_COMMENT);
        KotlinSemanticPsiTreeFunction missingCodeFunction = editSemanticPsiTreeFile.findNodesOfType(KotlinSemanticPsiTreeFunction.class, missingCodeChildPredicate).stream().findFirst().orElse(null);



        //Create List of Class Evaluators
        List<AssociationWeightComponentEvaluator> evaluators = new ArrayList<>();
        ModifiedFileAssociationEvaluator modifiedFileAssociationEvaluator = new ModifiedFileAssociationEvaluator(kotlinRepoPsiTreeCollectionInputComputation);
        EditFunctionAssociationEvaluator editFunctionAssociationEvaluator = new EditFunctionAssociationEvaluator(missingCodeFunction);
        PackageCompatabilityEvaluator packageCompatabilityEvaluator = new PackageCompatabilityEvaluator();
        PackageDistanceEvaluator packageDistanceEvaluator = new PackageDistanceEvaluator();
        evaluators.add(modifiedFileAssociationEvaluator);
        evaluators.add(editFunctionAssociationEvaluator);
        evaluators.add(packageCompatabilityEvaluator);
        //evaluators.add(packageDistanceEvaluator);


        AssociationWeightCalculator associationWeightCalculator = new AssociationWeightCalculator(evaluators);

        List<AssociationWeightComponentEvaluator> incomingReferenceEvaluators = new ArrayList<>();
        IncomingReferenceEvaluator incomingReferenceEvaluator = new IncomingReferenceEvaluator(editSemanticPsiTreeFile);
        incomingReferenceEvaluators.add(incomingReferenceEvaluator);
        //incomingReferenceEvaluators.add(packageCompatabilityEvaluator);
        incomingReferenceEvaluators.add(modifiedFileAssociationEvaluator);

        AssociationWeightCalculator incomingReferenceCalculator = new AssociationWeightCalculator(incomingReferenceEvaluators);



        /***** Get All Nodes Map ******/
        Map<String, Set<KotlinSemanticPsiTreeChildNode>> allNodesMap = kotlinSemanticPsiTreeRoot.collectNodeMapForType(KotlinSemanticPsiTreeChildNode.class, ElementVisibility.PUBLIC);


        //first add the actual file

        addContextEntryGroup(
                createContextEntryGroup("Code which is referenced in the edited file", editSemanticPsiTreeFile, allSemanticReferences, kotlinSemanticPsiTreeRoot, associationWeightCalculator, allNodesMap, 3, 0));

        /**
         * Add code which uses the same references
         */
//         if (missingCodeFunction != null) {
//
//             List<KotlinSemanticPsiTypeReference> editFunctionTypeReferences = missingCodeFunction.getAllReferencesOfTypeInSubtree(KotlinSemanticPsiTypeReference.class);
//             List<KotlinSemanticPsiCallableReference> editFunctionCallableReferences = missingCodeFunction.getAllReferencesOfTypeInSubtree(KotlinSemanticPsiCallableReference.class);
//
//             List<KotlinSemanticReference> editFunctionSemanticReferences = new ArrayList<>();
//             editFunctionSemanticReferences.addAll(editFunctionTypeReferences);
//             editFunctionSemanticReferences.addAll(editFunctionCallableReferences);
//
//
//             List<Class<? extends KotlinSemanticPart<?>>> partTypes = new ArrayList<>();
//            partTypes.add(KotlinSemanticFunctionInputParameterPart.class);
//            partTypes.add(KotlinSemanticFunctionBodyPart.class);
//
//            Map<String, Set<KotlinSemanticPsiTreeChildNode>> usedReferencesMap = kotlinSemanticPsiTreeRoot.collectReferencesFromParts(partTypes);
//            //used references
//        //System.out.println("Size of used references map: " + usedReferencesMap.size());
//        addContextEntryGroup(
//                  createContextEntryGroup("Code which uses the same types as the edited file", editSemanticPsiTreeFile, editFunctionSemanticReferences, kotlinSemanticPsiTreeRoot, associationWeightCalculator, usedReferencesMap, 10, 1));
//      }
//        if (getTotalContextEntriesCount() < 2 || getMaxWeight() <0.5) {
//            System.out.println("Not enough context, including usages");
//            addContextEntryGroup(
//                    createUsageEntryGroup("Code which uses the edited code", editSemanticPsiTreeFile, kotlinSemanticPsiTreeRoot, incomingReferenceCalculator, allNodesMap)
//            );
//        }
//        addContextEntryGroup(
//              createReadmeContextEntryGroup("Related Documentation: ",getContextCollector().getContextCollectionInput().getRepoFolder())
//        );

        //now loop through additional source folders to get other references

//        for (Map.Entry<String, KotlinSemanticPsiTreeRoot> otherKotlinSemanticPsiTreeRootEntries : kotlinEditFileSemanticPsiTreeComputation.getOtherSourceFolderRoots().entrySet()) {
//
//            ContextEntryGroup contextEntryGroup = createContextEntryGroup("Related code in source folder "+otherKotlinSemanticPsiTreeRootEntries.getKey()+" which is outside the folder the developer is working on but may be of relevance", editSemanticPsiTreeFile, allSemanticReferences, otherKotlinSemanticPsiTreeRootEntries.getValue(), associationWeightCalculator, allNodesMap);
////            if (contextEntryGroup.contextEntries().size() > 0) {
////                System.out.println("Found "+contextEntryGroup.contextEntries().size()+" context entries for source folder "+otherKotlinSemanticPsiTreeRootEntries.getKey());
////            }
//            addContextEntryGroup(contextEntryGroup);
//
//            ContextEntryGroup contextEntryGroupForALternativeFile =
//                    createContextGroupOfAlternativeVersionOfEditedFile(
//                            editSemanticPsiTreeFile, otherKotlinSemanticPsiTreeRootEntries.getKey(), otherKotlinSemanticPsiTreeRootEntries.getValue());
//            if (contextEntryGroupForALternativeFile != null) {
//                addContextEntryGroup(contextEntryGroupForALternativeFile);
//            }
//
 //        }

    }


    private ContextEntryGroup createContextGroupOfAlternativeVersionOfEditedFile(KotlinSemanticPsiTreeFile editSemanticPsiTreeFile, String sourceFolderRelativePath, KotlinSemanticPsiTreeRoot kotlinSemanticPsiTreeRoot) {


        String editFileName = KotlinPsiElementUtil.getSimpleNameOfContainingFile(editSemanticPsiTreeFile.getPsiElement());



        //System.out.println("Searching for alternative version of "+editFileName+" outside of main source folder in "+sourceFolderRelativePath+" ...");
        List<KotlinSemanticPsiTreeFile> alternativeFiles = kotlinSemanticPsiTreeRoot.findFileNodesByName(editFileName);
        for (KotlinSemanticPsiTreeFile alternativeFile : alternativeFiles) {
            ContextEntry contextEntry = new ContextEntry(alternativeFile.getContainingFile(),  alternativeFile.getPsiElement().getText(), new ContextEntryWeight(1.0f), "Alternative Version of File");
            System.out.println("Alternative version of "+editFileName+" found outside of main source folder in "+sourceFolderRelativePath);
            ContextEntryGroup contextEntryGroup = new ContextEntryGroup("Alternative version of "+editFileName+" found outside of main source folder in "+sourceFolderRelativePath, new ArrayList<>());
            contextEntryGroup.contextEntries().add(contextEntry);
            return contextEntryGroup;
        }
        return null;
    }

    private ContextEntryGroup   createContextEntryGroup(
            String title,
            KotlinSemanticPsiTreeFile editSemanticPsiTreeFile,
            List<KotlinSemanticReference> allSemanticReferences,
            KotlinSemanticPsiTreeRoot kotlinSemanticPsiTreeRoot,
            AssociationWeightCalculator associationWeightCalculator,
            Map<String, Set<KotlinSemanticPsiTreeChildNode>> allNodesMap,
            int numberOfAmbiguousMatchesToInclude,
            int limit

    ) {

        // Get Class Associations
        Set<SemanticNodeAssociation<?>> nodeAssociations = KotlinSemanticTreeCodeAssociationsUtil
                .createAllSemanticNodeAssociations(allSemanticReferences, allNodesMap, true, associationWeightCalculator, 3)
                .stream()
                .filter(association -> !association.associatedNode().isInSameFile(editSemanticPsiTreeFile))
//                .filter(association ->
//                        association.associatedNode().getClass() == KotlinSemanticPsiTreeClassOrObject.class
//                        ||
//                        association.associatedNode().getClass() == KotlinSemanticPsiTreeTopLevelFunction.class
//                )
                .collect(Collectors.toSet());

        List<ContextEntry> allContextEntries = associationWeightCalculator.toConsolidatedContextEntries(nodeAssociations);

        if (limit > 0) {
            allContextEntries = allContextEntries.stream()
                    .sorted((e1, e2) -> Double.compare(e2.contextEntryWeight().value(), e1.contextEntryWeight().value()))
                    .limit(limit)
                    .collect(Collectors.toList());

        }


        ContextEntryGroup contextEntryGroup = new ContextEntryGroup(title, allContextEntries);
        return contextEntryGroup;

    }

    private void addSemanticReferencesExplanation(String title, Set<? extends KotlinSemanticReference> kotlinSemanticReferences) {
        Set<String> explanations = new LinkedHashSet<>();
        for (KotlinSemanticReference kotlinSemanticReference : kotlinSemanticReferences) {
            StringBuilder entryExplanation = new StringBuilder();
            for (Map.Entry<String, String> entry : kotlinSemanticReference.toExplnationMap().entrySet()) {
                entryExplanation.append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append("\n");
            }
            explanations.add(entryExplanation.toString().trim());
        }

        addExplanation(title, HtmlUtil.generateStringListHtmlTable(explanations));
    }

    private ContextEntryGroup createUsageEntryGroup(String title, KotlinSemanticPsiTreeFile editSemanticPsiTreeFile, KotlinSemanticPsiTreeRoot kotlinSemanticPsiTreeRoot, AssociationWeightCalculator associationWeightCalculator, Map<String, Set<KotlinSemanticPsiTreeChildNode>> allNodesMap) {
        List<Class<? extends KotlinSemanticPsiTreeNode>> selfReferenceClasses = List.of(
                KotlinSemanticPsiTreeClassOrObject.class,
                KotlinSemanticPsiTreeTopLevelFunction.class);
                //KotlinSemanticPsiTreeMemberFunction.class);

        List<? extends KotlinSemanticPsiTreeNode> selfReferencedNodes = editSemanticPsiTreeFile.findNodesOfTypes(selfReferenceClasses, ElementVisibility.PUBLIC);

        System.out.println("Self Referenced Nodes: " + selfReferencedNodes.stream()
                .map(KotlinSemanticPsiTreeNode::getTypeName)
                .collect(Collectors.joining(", ")));

        Set<KotlinSemanticReference> selfReferences = new HashSet<>();
        for (KotlinSemanticPsiTreeNode selfReferenceNode : selfReferencedNodes) {
            selfReferences.addAll(selfReferenceNode.getSelfPart().getSemanticReferences());
        }

        Set<SemanticNodeAssociation<?>> nodeAssociations = getNodeAssociationsForIncomingReferences(selfReferences, kotlinSemanticPsiTreeRoot);

        List<ContextEntry> allContextEntries = associationWeightCalculator.toConsolidatedContextEntries(nodeAssociations).stream()
                .filter(entry -> !editSemanticPsiTreeFile.getContainingFile().equals(entry.sourceFile()))

                .sorted(Comparator.comparing(entry -> entry.contextEntryWeight().value(), Comparator.naturalOrder()))
        .limit(1)
        .collect(Collectors.toList());
        System.out.println("Number of Self Referencing Entries " + allContextEntries.size());

        ContextEntryGroup contextEntryGroup = new ContextEntryGroup(title, allContextEntries);
     return contextEntryGroup;
}



    private Set<SemanticNodeAssociation<?>> getNodeAssociationsForIncomingReferences(
            Set<KotlinSemanticReference> incomingReferences,
            KotlinSemanticPsiTreeRoot kotlinSemanticPsiTreeRoot) {



        Set<String> referenceNames = incomingReferences.stream()
                .map(KotlinSemanticReference::getReferenceName)
                .collect(Collectors.toSet());

        return getSemanticNodeAssociationsForIncomingReferenceNames(referenceNames, incomingReferences, kotlinSemanticPsiTreeRoot);
    }


    private Set<SemanticNodeAssociation<?>> getSemanticNodeAssociationsForIncomingReferenceNames(
            Set<String> incomingReferenceNames,
            Set<KotlinSemanticReference> originalReferences,
            KotlinSemanticPsiTreeRoot kotlinSemanticPsiTreeRoot) {

        //System.out.println("Incoming Reference Names :"+incomingReferenceNames.stream().collect(Collectors.joining(",")));

        Set<SemanticNodeAssociation<?>> allAssociations = new LinkedHashSet<>();

        Map<String, KotlinSemanticReference> referencesByName = originalReferences.stream()
                .collect(Collectors.toMap(
                        KotlinSemanticReference::getReferenceName,
                        reference -> reference,
                        (existing, replacement) -> existing
                ));

        for (String referenceName : incomingReferenceNames) {
            List<KotlinSemanticReference> matchingReferences = findMatchingReferences(
                    referenceName,
                    kotlinSemanticPsiTreeRoot
            );

            KotlinSemanticReference originalReference = referencesByName.get(referenceName);
            allAssociations.addAll(createAssociations(originalReference, matchingReferences));
        }

        return allAssociations;
    }



    private List<KotlinSemanticReference> findMatchingReferences(String incomingReferenceName, KotlinSemanticPsiTreeRoot root) {
        List<KotlinSemanticReference> matchingReferences = new ArrayList<>();

        // Get all nodes in the tree
        List<KotlinSemanticPsiTreeChildNode> allNodes = root.findNodesOfType(
                KotlinSemanticPsiTreeChildNode.class
        );

        // For each node, check all its parts for matching references
        for (KotlinSemanticPsiTreeChildNode<?> node : allNodes) {
            for (KotlinSemanticPart<?> part : node.getSemanticParts()) {
                for (KotlinSemanticReference reference : part.getSemanticReferences()) {
                    if (reference.getReferenceName().equals(incomingReferenceName)) {
                        matchingReferences.add(reference);
                    }
                }
            }
        }

        return matchingReferences;
    }



    private List<SemanticNodeAssociation<?>> createAssociations(
            KotlinSemanticReference incomingReference,
            List<KotlinSemanticReference> matchingReferences) {

        List<SemanticNodeAssociation<?>> associations = new ArrayList<>();
        KotlinSemanticPsiTreeFile incomingReferenceFile;
        try {
            incomingReferenceFile = incomingReference.getContainingPart()
                    .getContainingNode()
                    .getContainingKotlinSemanticPsiTreeFile();
        } catch (Exception e) {
            System.out.println("Error finding containing file");
            e.printStackTrace();
            return associations;
        }

        for (KotlinSemanticReference matchingReference : matchingReferences) {
            KotlinSemanticPsiTreeChildNode<?> matchingNode = matchingReference.getContainingPart().getContainingNode();

            //System.out.println("Matching Node : "+matchingNode.getContainingKtFile().getName());

            // Skip if in same file
            if (!matchingNode.getContainingKotlinSemanticPsiTreeFile().equals(incomingReferenceFile)) {
                associations.add(new SemanticNodeAssociation<>(
                        incomingReference,
                        matchingNode.getContainingKotlinSemanticPsiTreeFile(),
                        1.0f, false
                ));
            }
        }
        return associations;
    }




    private ContextEntryGroup createReadmeContextEntryGroup(String title, File rootFolder) {
        List<File> readmeFiles = FileSearchUtil.findFilesByNamePattern(rootFolder, "readme\\..*");

        List<ContextEntry> readmeEntries = readmeFiles.stream()
                .map(file -> new ContextEntry(
                        file,
                        readFileContent(file),
                        new ContextEntryWeight(1.0f),
                        "README file found in: " + file.getParentFile().getName()
                ))
                .collect(Collectors.toList());

        return new ContextEntryGroup(title, readmeEntries);
    }

    private String readFileContent(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            return "Error reading file: " + e.getMessage();
        }
    }


    @Override
    public void close() throws Exception {

        kotlinEditFilePsiTreeComputation = null;
        kotlinEditFileSemanticPsiTreeComputation = null;
        kotlinRepoPsiTreeCollectionInputComputation = null;
        kotlinSemanticPsiTreeRoot = null;
    }
}