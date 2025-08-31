package org.noesis.codeanalysis.dataobjects.semanticpsi;

import org.jetbrains.kotlin.psi.KtConstructor;
import org.jetbrains.kotlin.psi.KtTypeReference;
import org.noesis.codeanalysis.dataobjects.enums.ElementVisibility;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPart;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticReference;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiTreeUtil;
import org.noesis.codeanalysis.util.kotlin.psi.formatters.KotlinPsiTreeFormatUtil;

import java.util.List;
import java.util.stream.Collectors;

public class KotlinSemanticPsiTreeConstructor extends KotlinSemanticPsiTreeChildNode<KtConstructor> {

    KtConstructor<?> ktConstructor;

    public KotlinSemanticPsiTreeConstructor(KtConstructor<?> ktConstructor, KotlinSemanticPsiTreeClassOrObject parent) {
        super(ktConstructor, parent);
        this.ktConstructor = ktConstructor;
    }

    @Override
    public List<KotlinSemanticPsiTreeChildNode<?>> getChildren() {
        return List.of();
    }

    @Override
    public ElementVisibility getElementVisibility() {
        return KotlinPsiElementUtil.getElementVisibility(ktConstructor);
    }

    @Override
    public String getTypeName() {
        return ktConstructor.getContainingClassOrObject().getName();
    }

    @Override
    public List<KtTypeReference> getSignatureReferencedTypes() {

        return getParameterTypeReferences();

    }

    public List<KtTypeReference> getParameterTypeReferences() {
        return ktConstructor.getValueParameters().stream()
                .flatMap(param -> KotlinPsiTreeUtil.getEligibleReferencedTypes(param).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<KtTypeReference> getBodyReferencedTypes() {
        List<KtTypeReference> bodyReferences = KotlinPsiTreeUtil.getEligibleReferencedTypes(
                ktConstructor.getBodyExpression(),
                ktConstructor.getBodyBlockExpression());

        return bodyReferences;

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
        return KotlinPsiTreeFormatUtil.getKtConstructorSignature(ktConstructor);
    }

    @Override
    public List<KotlinSemanticPart<?>> getSemanticParts() {
        return List.of();
    }

    @Override
    public KotlinSemanticReference getSemanticReference() {
        return null;
    }
}
