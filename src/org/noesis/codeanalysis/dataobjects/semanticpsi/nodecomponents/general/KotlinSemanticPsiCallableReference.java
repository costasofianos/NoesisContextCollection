package org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;

public class KotlinSemanticPsiCallableReference extends KotlinSemanticReference {

    public KotlinSemanticPsiCallableReference(String referenceName, PsiElement psiElement, KotlinSemanticPart containingPart) {
        super(referenceName, psiElement, containingPart);

    }

}
