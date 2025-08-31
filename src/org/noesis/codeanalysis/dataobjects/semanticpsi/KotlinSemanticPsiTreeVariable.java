package org.noesis.codeanalysis.dataobjects.semanticpsi;

import org.jetbrains.kotlin.psi.KtTypeReference;
import org.jetbrains.kotlin.psi.KtVariableDeclaration;
import org.noesis.codeanalysis.dataobjects.enums.ElementVisibility;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPart;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticReference;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiTreeUtil;
import org.noesis.codeanalysis.util.kotlin.psi.formatters.KotlinPsiTreeFormatUtil;

import java.util.List;

public class KotlinSemanticPsiTreeVariable extends KotlinSemanticPsiTreeChildNode<KtVariableDeclaration> {

        private final KtVariableDeclaration ktVariableDeclaration;

        public KotlinSemanticPsiTreeVariable(KtVariableDeclaration ktVariableDeclaration, KotlinSemanticPsiTreeNode parent) {
            super(ktVariableDeclaration, parent);
            this.ktVariableDeclaration = ktVariableDeclaration;
        }

    @Override
    public KotlinSemanticReference getSemanticReference() {
        return null;
    }

    @Override
    public List<KotlinSemanticPart<?>> getSemanticParts() {
        return List.of();
    }

    @Override
    public ElementVisibility getElementVisibility() {
        return KotlinPsiElementUtil.getElementVisibility(ktVariableDeclaration);
    }

    public KtVariableDeclaration getKtVariableDeclaration() {
            return ktVariableDeclaration;
        }

    @Override
    public List<KotlinSemanticPsiTreeChildNode<?>> getChildren() {
            return List.of();
    }

    @Override
    public String getTypeName() {
        return ktVariableDeclaration.getName() != null ? ktVariableDeclaration.getName() : "<anonymous>";
    }

    @Override
    public List<KtTypeReference> getSignatureReferencedTypes() {
        return KotlinPsiTreeUtil.getEligibleReferencedTypes(ktVariableDeclaration.getTypeReference());
    }

    @Override
    public List<KtTypeReference> getBodyReferencedTypes() {
        return KotlinPsiTreeUtil.getEligibleReferencedTypes(ktVariableDeclaration.getInitializer());
    }

    @Override
    public List<String> getReferencedNames() {
        return KotlinPsiTreeUtil.findEligibleKtNameReferencedExpressionNames(getPsiElement());
    }

    @Override
    public String getPublicContext() {

            return encapsulateContextWithParentNodes(getPublicContextForParent(), false);
    }

    @Override
    public String getPublicContextForParent() {

        String context = KotlinPsiTreeFormatUtil.getKtVariableSignature(ktVariableDeclaration);
        //String context = getPsiElement().getText();

        //if (TokenEstimator.estimateTokens(context) > 30) {
        //  context = KotlinPsiTreeFormatUtil.getKtVariableSignature(ktVariableDeclaration);
        //}
        //System.out.println("Printing context for variable: " + context);
        return context;
    }

}