package org.noesis.codeanalysis.collectionstrategies.kotlin.psi.weightedassociations.functionevaluators;

import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightComponent;
import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightComponentEvaluator;
import org.noesis.codeanalysis.dataobjects.associations.SemanticNodeAssociation;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.util.tokens.TokenEstimator;

import java.util.Set;

public class PackageCompatabilityEvaluator implements AssociationWeightComponentEvaluator {

    @Override
    public AssociationWeightComponent getAssociationWeightComponent(SemanticNodeAssociation<? extends KotlinSemanticPsiTreeChildNode<?>> semanticNodeAssociation) {
        float weight = 1.0f;
        float value = 0.0f;
        String explanation = "Package not compatible";

        Set<String> commonMethodNames = Set.of("update", "enabled","this","it");

        if (semanticNodeAssociation.associatedNode().canBeAccessedBy(semanticNodeAssociation.kotlinSemanticReference().getContainingPart().getContainingNode())) {
            value = 1.0f;
            explanation = "Package compatible";
        } else {
            // System.out.println("Package not compatible "+semanticNodeAssociation.kotlinSemanticReference().getContainingPart().getContainingNode().getContainingKtFile().getVirtualFilePath());

            //allowed to find similar functions as examples.   The more complex the word, the more likely there is a connection

            int numberOfTokensInReference = TokenEstimator.estimateTokens(semanticNodeAssociation.kotlinSemanticReference().getReferenceName());
            if (numberOfTokensInReference == 1) {
                if (commonMethodNames.contains(semanticNodeAssociation.kotlinSemanticReference().getReferenceName())) {
                    value = 0.05f;
                    explanation = "Packages not compatible and method name is common [Number of Tokens = 1]";
                } else {
                    value = 0.1f;
                    explanation = "Package not compatible and method does not have a known common name [Number of Tokens = 1]";
                }
            } else {
                value = (float) Math.min(((float) numberOfTokensInReference) / ((float) 5), 0.9);
                explanation = "Packages not compatible, but weighting provided for complex names that are the same [Number of Tokens = " + numberOfTokensInReference + "]";
            }
        }
        return new AssociationWeightComponent(weight, value, false, explanation);
    }
}
