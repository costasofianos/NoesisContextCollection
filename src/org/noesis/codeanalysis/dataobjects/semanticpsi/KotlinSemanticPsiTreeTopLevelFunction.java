package org.noesis.codeanalysis.dataobjects.semanticpsi;

import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeContextEncapsulatorNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPsiCallableReference;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticReference;
import org.noesis.codeanalysis.util.kotlin.psi.formatters.KotlinPsiTreeFormatUtil;

public class KotlinSemanticPsiTreeTopLevelFunction extends KotlinSemanticPsiTreeFunction implements KotlinSemanticPsiTreeContextEncapsulatorNode {
    public KotlinSemanticPsiTreeTopLevelFunction(KtNamedFunction ktNamedFunction, KotlinSemanticPsiTreeChildNode<?> parent) {
        super(ktNamedFunction, parent);
        initialise();

    }

    @Override
    public String getPublicContext() {

       return encapsulateContextWithParentNodes(KotlinPsiTreeFormatUtil.getKtNamedFunctionSignature(getPsiElement()), false);
    }

    @Override
    public String encapsulateChildContext(String context) {
        return context;
    }

    @Override
    public KotlinSemanticReference getSemanticReference() {
        return new KotlinSemanticPsiCallableReference(getTypeName(), getPsiElement(), getSelfPart());
    }

    @Override
    public String getPublicContextForParent() {
        return KotlinPsiTreeFormatUtil.getKtNamedFunctionSignature(getPsiElement());
    }
}
