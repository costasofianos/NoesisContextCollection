package org.noesis.codeanalysis.dataobjects.associations;

import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;

public interface AssociationWeightComponentEvaluator {
    AssociationWeightComponent getAssociationWeightComponent(SemanticNodeAssociation<? extends KotlinSemanticPsiTreeChildNode<?>> semanticNodeAssociation);
}


