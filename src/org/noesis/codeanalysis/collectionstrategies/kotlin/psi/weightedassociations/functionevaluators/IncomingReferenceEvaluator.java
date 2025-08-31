package org.noesis.codeanalysis.collectionstrategies.kotlin.psi.weightedassociations.functionevaluators;

import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightComponent;
import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightComponentEvaluator;
import org.noesis.codeanalysis.dataobjects.associations.SemanticNodeAssociation;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeFile;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeMemberFunction;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeTopLevelFunction;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;

public class IncomingReferenceEvaluator implements AssociationWeightComponentEvaluator {

    KotlinSemanticPsiTreeFile editSemanticPsiTreeFile;
    public IncomingReferenceEvaluator(KotlinSemanticPsiTreeFile editSemanticPsiTreeFile) {
        this.editSemanticPsiTreeFile = editSemanticPsiTreeFile;
    }

    @Override
    public AssociationWeightComponent getAssociationWeightComponent(SemanticNodeAssociation<? extends KotlinSemanticPsiTreeChildNode<?>> semanticNodeAssociation) {
        float weight = 1.0f;
        float value = 0.7f;
        boolean includeBody = false;

        if (
                semanticNodeAssociation.associatedNode() instanceof KotlinSemanticPsiTreeTopLevelFunction
                ||
                semanticNodeAssociation.associatedNode() instanceof KotlinSemanticPsiTreeMemberFunction
                ||
                semanticNodeAssociation.associatedNode() instanceof KotlinSemanticPsiTreeFile

        ) {
            includeBody = true;
        }




        return new AssociationWeightComponent(weight, value, includeBody, "Incoming Reference");
    }
}
