package org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general;

import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class KotlinSemanticPart<T extends KotlinSemanticPsiTreeChildNode<?>> {
    
    private final LinkedHashSet<KotlinSemanticReference> semanticReferences = new LinkedHashSet<>();
    private final T containingNode;
    
    public KotlinSemanticPart(T containingNode) {
        this.containingNode = containingNode;
    }

    public Set<KotlinSemanticReference> getSemanticReferences() {
        return semanticReferences;
    }

    public T getContainingNode() {
        return containingNode;
    }
}