package org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.self;

import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPart;

public class KotlinSemanticSelfPart<T extends KotlinSemanticPsiTreeChildNode<?>> extends KotlinSemanticPart<T> {
    public KotlinSemanticSelfPart(T node) {
        super(node);
    }
}

