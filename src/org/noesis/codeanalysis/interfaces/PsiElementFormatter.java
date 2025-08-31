package org.noesis.codeanalysis.interfaces;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;

public interface PsiElementFormatter {
    public String format(PsiElement element);
}
