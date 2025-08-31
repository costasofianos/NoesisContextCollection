package org.noesis.codeanalysis.collectionstrategies.kotlin.psi.weightedassociations.functionevaluators;

import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightComponent;
import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightComponentEvaluator;
import org.noesis.codeanalysis.dataobjects.associations.SemanticNodeAssociation;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;

public class AssociationTypeEvaluator implements AssociationWeightComponentEvaluator {

    @Override
    public AssociationWeightComponent getAssociationWeightComponent(SemanticNodeAssociation<? extends KotlinSemanticPsiTreeChildNode<?>> semanticNodeAssociation) {
        float weight = 1.0f;
        float value = 0.5f;
        String explanation = "";

        if (semanticNodeAssociation.kotlinSemanticReference().getReferenceName().equals(
                semanticNodeAssociation.associatedNode().getTypeName()
        )) {
            explanation = "Associated types match";
            value = 1.0f;
        } else {
            explanation = "Associated types do not match";

        }

        return new AssociationWeightComponent(weight, value, false, explanation);
    }
}
