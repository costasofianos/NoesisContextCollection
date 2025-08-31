package org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;

public class KotlinSemanticPsiTypeReference extends KotlinSemanticReference {

    public KotlinSemanticPsiTypeReference(String referenceName, PsiElement psiElement, KotlinSemanticPart containingPart) {
        super(referenceName, psiElement, containingPart);

    }

}
