package org.noesis.codeanalysis.collectionstrategies.kotlin.psi.weightedassociations.functionevaluators;

import org.jetbrains.kotlin.psi.KtFile;
import org.noesis.codeanalysis.computations.kotlin.psi.KotlinRepoPsiTreeCollectionInputComputation;
import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightComponent;
import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightComponentEvaluator;
import org.noesis.codeanalysis.dataobjects.associations.SemanticNodeAssociation;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeMemberFunction;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeTopLevelFunction;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;

public class ModifiedFileAssociationEvaluator implements AssociationWeightComponentEvaluator {

    KotlinRepoPsiTreeCollectionInputComputation kotlinRepoPsiTreeCollectionInputComputation;

    public ModifiedFileAssociationEvaluator(KotlinRepoPsiTreeCollectionInputComputation kotlinRepoPsiTreeCollectionInputComputation) {
        this.kotlinRepoPsiTreeCollectionInputComputation = kotlinRepoPsiTreeCollectionInputComputation;
    }

    @Override
    public AssociationWeightComponent getAssociationWeightComponent(SemanticNodeAssociation<?> semanticNodeAssociation) {

        float weight = 1.0f;
        float value = 0.7f;
        boolean includeBody = false;
        String explanation = "File Not Modified";

        KotlinSemanticPsiTreeChildNode<?> kotlinSemanticPsiTreeChildNode = semanticNodeAssociation.associatedNode();
        KtFile ktFile = kotlinSemanticPsiTreeChildNode.getContainingKtFile();

        if (kotlinRepoPsiTreeCollectionInputComputation.isModified(ktFile)) {
            value = 1.0f;
            if (
                    semanticNodeAssociation.associatedNode().getClass() == KotlinSemanticPsiTreeMemberFunction.class
                    ||
                    semanticNodeAssociation.associatedNode().getClass() == KotlinSemanticPsiTreeTopLevelFunction.class
                    ) {
                includeBody = false;
            }

            //includeBody = true;
            explanation = "File Modified";
        }
        return new AssociationWeightComponent(weight, value, includeBody, explanation);
    }
}
