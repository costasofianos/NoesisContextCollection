package org.noesis.codeanalysis.collectionstrategies.kotlin.psi.weightedassociations.functionevaluators;

import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightComponent;
import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightComponentEvaluator;
import org.noesis.codeanalysis.dataobjects.associations.SemanticNodeAssociation;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;

public class PackageDistanceEvaluator implements AssociationWeightComponentEvaluator {

    @Override
    public AssociationWeightComponent getAssociationWeightComponent(SemanticNodeAssociation<? extends KotlinSemanticPsiTreeChildNode<?>> semanticNodeAssociation) {
        float weight = 0.5f;
        float value;
        String explanation;

        KotlinSemanticPsiTreeChildNode<?> associatedNode = semanticNodeAssociation.associatedNode();
        KotlinSemanticPsiTreeChildNode<?> referenceNode = semanticNodeAssociation.kotlinSemanticReference()
                .getContainingPart()
                .getContainingNode();

        // Get package distance between nodes
        value = associatedNode.getPackageDistance(referenceNode);

        explanation = String.format("Package distance: %f", value);

        return new AssociationWeightComponent(weight, value, false, explanation);
    }

}
