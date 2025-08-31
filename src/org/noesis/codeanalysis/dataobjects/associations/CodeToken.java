package org.noesis.codeanalysis.dataobjects.associations;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;

public record CodeToken(String token, PsiElement psiElement) {

    @Override
    public String token() {
        return token;
    }
}

