package org.noesis.codeanalysis.collectionstrategies.kotlin.psi.weightedassociations.functionevaluators;

import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightComponent;
import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightComponentEvaluator;
import org.noesis.codeanalysis.dataobjects.associations.SemanticNodeAssociation;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeFunction;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeMemberFunction;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeTopLevelFunction;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.function.KotlinSemanticFunctionBodyPart;

public class EditFunctionAssociationEvaluator implements AssociationWeightComponentEvaluator {

    KotlinSemanticPsiTreeFunction editSemanticPsiTreeFunction;

    public EditFunctionAssociationEvaluator(KotlinSemanticPsiTreeFunction editSemanticPsiTreeFunction) {
        this.editSemanticPsiTreeFunction = editSemanticPsiTreeFunction;
    }

    @Override
    public AssociationWeightComponent getAssociationWeightComponent(SemanticNodeAssociation<?> semanticNodeAssociation) {

        //set to invalid value and ensure logic covers all cases
        float weight = 1.0f;
        float value = -1.0f;
        boolean includeBody = false;
        String explanation = "("+(editSemanticPsiTreeFunction==null?"null":editSemanticPsiTreeFunction.getTypeName())+")";

        KotlinSemanticPsiTreeChildNode<?> kotlinSemanticPsiTreeChildNode = semanticNodeAssociation.associatedNode();

        if (editSemanticPsiTreeFunction == null) {
            value = 0.7f;
            explanation = "No Modified Function"+explanation;
        } else {
            if (semanticNodeAssociation.kotlinSemanticReference().getContainingPart().getContainingNode() == editSemanticPsiTreeFunction) {
                value = 1.0f;
                if (
                        semanticNodeAssociation.associatedNode().getClass() == KotlinSemanticPsiTreeMemberFunction.class
                        ||
                        semanticNodeAssociation.associatedNode().getClass() == KotlinSemanticPsiTreeTopLevelFunction.class
                ) {
                    includeBody = false;
                }
                explanation = "Association in Modified Function "+explanation;
                //System.out.println("Association in Modified Function "+explanation);
            } else {
                if (semanticNodeAssociation.kotlinSemanticReference().getContainingPart().getClass() == KotlinSemanticFunctionBodyPart.class) {
                    value = 0.4f;
                    explanation = "Association in Body of Non Modified Function "+explanation;
                } else {
                    explanation = "Association outside of Function Body "+explanation;
                    value = 0.7f;
                }
            }
        }
        return new AssociationWeightComponent(weight, value, includeBody, explanation);
    }
}
