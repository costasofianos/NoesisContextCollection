package org.noesis.codeanalysis.util.kotlin.psi.formatters;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.noesis.codeanalysis.interfaces.PsiElementFormatter;

public class KotlinDeclarationBodyFormatter implements PsiElementFormatter {

    public String format(PsiElement element) {
        if (element == null) {
            return "";
        }

        return element.getText();
    }
}