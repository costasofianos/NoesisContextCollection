package org.noesis.codeanalysis.dataobjects.associations;

import org.noesis.codeanalysis.dataobjects.output.ContextEntry;
import org.noesis.codeanalysis.dataobjects.output.ContextEntryWeight;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeFile;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.util.general.StringUtils;
import org.noesis.codeanalysis.util.kotlin.psi.formatters.KotlinSemanticNodeContextUtil;

import java.util.*;

public class AssociationWeightCalculator {

    List<AssociationWeightComponentEvaluator> associationWeightComponentEvaluators;

    public AssociationWeightCalculator(List<AssociationWeightComponentEvaluator> associationWeightComponentEvaluators) {
        this.associationWeightComponentEvaluators = associationWeightComponentEvaluators;
    }

//    public List<ContextEntry> toContextEntries(Set<SemanticNodeAssociation<? extends KotlinSemanticPsiTreeChildNode<?>>> semanticNodeAssociations) {
//
//
//
//        return semanticNodeAssociations.stream()
//                .map(association -> {
//                    AssociationWeight associationWeight = calculateWeightedAverage(association);
////                    if (associationWeight.includeBody()) {
////                        System.out.println("Include Body: "+associationWeight.includeBody());
////                    }
//                    KotlinSemanticPsiTreeChildNode<?> node = association.associatedNode();
//                    String contextText = associationWeight.includeBody() ?
//                    node.getFullTextOfElement() :
//                    node.getPublicContext();
//                    return new ContextEntry(
//                            node.getContainingFile(),
//                            contextText,
//                            new ContextEntryWeight(associationWeight.weight()),
//                            association.toString() + "\n\n---------\nASSOCIATION WEIGHT\n\n" + associationWeight.explanation()
//                    );
//                })
//                .sorted(Comparator.comparing((ContextEntry entry) -> entry.contextEntryWeight().value()).reversed())
//                .collect(Collectors.toList());
//    }

    public List<ContextEntry> toConsolidatedContextEntries(Set<SemanticNodeAssociation<? extends KotlinSemanticPsiTreeChildNode<?>>> semanticNodeAssociations) {

        List<ContextEntry> consolidatedContextEntries = new ArrayList<>();

        Map<KotlinSemanticPsiTreeFile, Set<SemanticNodeAssociation<?>>> groupedAssociations = KotlinSemanticNodeContextUtil.groupAssociationsByFile(semanticNodeAssociations);

        for (Map.Entry<KotlinSemanticPsiTreeFile, Set<SemanticNodeAssociation<?>>> fileEntry : groupedAssociations.entrySet()) {
            String context = KotlinSemanticNodeContextUtil.getFileContextWithAssociations(fileEntry.getKey(), fileEntry.getValue());
            AssociationWeight associationWeight = calculateMaximumConsolidatedWeightedAverage(fileEntry.getValue());

            StringBuilder explanationBuilder = new StringBuilder();

            // Add each association's toString() output
            for (SemanticNodeAssociation<?> association : fileEntry.getValue()) {
                explanationBuilder.append(association.toString()).append("\n");
            }

            // Add the weight calculation explanation
            explanationBuilder.append("\n=========================\n")
                    .append("WEIGHT CALCULATION\n")
                    .append("=========================\n\n")
                    .append(associationWeight.explanation());



            consolidatedContextEntries.add(
                    new ContextEntry(
                            fileEntry.getKey().getContainingFile(),
                            context,
                            new ContextEntryWeight(associationWeight.weight()),
                            explanationBuilder.toString()
                    )
            );
        }
        return consolidatedContextEntries;
    }



//    /**
//     * @param semanticNodeAssociations
//     * @return
//     *
//     * This method assembles entries that have the same parent
//     */
//    @Deprecated
//    public List<ContextEntry> toConsolidatedContextEntriesOld(Set<SemanticNodeAssociation<? extends KotlinSemanticPsiTreeChildNode<?>>> semanticNodeAssociations) {
//        // First consolidate the associations using KotlinSemanticNodeContextUtil
//        Map<KotlinSemanticPsiTreeChildNode<?>, Set<SemanticNodeAssociation<?>>> consolidatedAssociations =
//                KotlinSemanticNodeContextUtil.consolidateAssociations(semanticNodeAssociations);
//
//        // Convert each consolidated group into a ContextEntry
//        return consolidatedAssociations.entrySet().stream()
//                .map(entry -> {
//                    KotlinSemanticPsiTreeChildNode<?> parent = entry.getKey();
//                    Set<SemanticNodeAssociation<?>> associations = entry.getValue();
//
//                    // Calculate consolidated weight
//                    AssociationWeight consolidatedWeight = calculateMaximumConsolidatedWeightedAverage(associations);
//
//                    // Get consolidated context
//                    String contextText = new KotlinSemanticNodeContextUtil()
//                            .getKotlinContext(parent, associations);
//
//                    // Create explanation text combining all association explanations
//                    String explanationText = associations.stream()
//                            .map(association -> association.toString() + "\n\n---------\nASSOCIATION WEIGHT\n\n" +
//                                    calculateWeightedAverage(association).explanation())
//                            .collect(Collectors.joining("\n\n==========\n\n"));
//
//                    return new ContextEntry(
//                            parent.getContainingFile(),
//                            contextText,
//                            new ContextEntryWeight(consolidatedWeight.weight()),
//                            explanationText
//                    );
//                })
//                .sorted(Comparator.comparing((ContextEntry entry) -> entry.contextEntryWeight().value()).reversed())
//                .collect(Collectors.toList());
//    }

    private AssociationWeight calculateMaximumConsolidatedWeightedAverage(Set<SemanticNodeAssociation<?>> associations) {
        // Calculate weight for each association and return the maximum
        return associations.stream()
                .map(this::calculateWeightedAverage)
                .max(Comparator.comparing(AssociationWeight::weight))
                .orElseThrow(() -> new IllegalArgumentException("Empty associations set"));
    }









    /**
     * Calculates a weighted average based on component evaluators for the given semantic node association
     * @param semanticNodeAssociation The semantic node association to process
     * @return An AssociationWeight containing the weighted average (between 0 and 1) and explanation
     * @throws IllegalArgumentException if any component weight or value is outside [0,1] range or if total weight is zero
     */

    public AssociationWeight calculateWeightedAverage(SemanticNodeAssociation<? extends KotlinSemanticPsiTreeChildNode<?>> semanticNodeAssociation) {
        float totalWeight = 0.0f;
        float weightedSum = 0.0f;
        boolean includeBody = false;

        StringBuilder explanationBuilder = new StringBuilder();

        boolean associationInvalid = false;

        // Calculate the maximum length of evaluator class names for alignment
        int maxEvaluatorNameLength = associationWeightComponentEvaluators.stream()
                .map(e -> e.getClass().getSimpleName())
                .mapToInt(String::length)
                .max()
                .orElse(0);

        for (AssociationWeightComponentEvaluator evaluator : associationWeightComponentEvaluators) {
            AssociationWeightComponent weightComponent = evaluator.getAssociationWeightComponent(semanticNodeAssociation);

            float componentWeight = weightComponent.impact();
            float componentValue = weightComponent.value();

            // Validate weight
            if (componentWeight < 0.0f || componentWeight > 1.0f) {
                throw new IllegalArgumentException(
                        "Weight must be between 0 and 1, got: " + componentWeight);
            }



            // Validate value
            if (componentValue < 0.0f || componentValue > 1.0f) {
                throw new IllegalArgumentException(
                        "Value must be between -1 and 1, got: " + componentValue);
            }

            if (componentValue == 0.0f) {
                associationInvalid = true;
            }


            // If any component wants to include body, set the flag
            includeBody = includeBody || weightComponent.includeBody();

            totalWeight += componentWeight;
            weightedSum += componentWeight * componentValue;

            // Add formatted explanation line
            String evaluatorName = evaluator.getClass().getSimpleName();
            explanationBuilder.append(StringUtils.padToLength(evaluatorName, maxEvaluatorNameLength))
                    .append(": ")
                    .append(String.format("%.2f", componentValue))
                    .append(" (")
                    .append(weightComponent.explanation())
                    .append(")\n");
        }

        float finalWeight = 0.0f;
        if (!associationInvalid) {
            // Avoid division by zero
            if (totalWeight == 0.0f) {
                throw new IllegalArgumentException("Sum of weights cannot be zero");
            }
            finalWeight = weightedSum / totalWeight;
        }


        return new AssociationWeight(finalWeight, includeBody, explanationBuilder.toString());
    }

    }