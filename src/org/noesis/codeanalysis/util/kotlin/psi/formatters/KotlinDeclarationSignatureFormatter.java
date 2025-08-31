package org.noesis.codeanalysis.util.kotlin.psi.formatters;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.psi.KtClassOrObject;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtProperty;
import org.jetbrains.kotlin.psi.KtTypeAlias;
import org.noesis.codeanalysis.interfaces.PsiElementFormatter;

public class KotlinDeclarationSignatureFormatter implements PsiElementFormatter {

    public String format(PsiElement element) {
        if (element == null) {
            return "";
        }

        return switch (element) {
            case KtNamedFunction ktNamedFunction -> KotlinPsiTreeFormatUtil.getKtNamedFunctionSignature(ktNamedFunction);
            case KtClassOrObject ktClassOrObject -> KotlinPsiTreeFormatUtil.getTextBeforeStart(ktClassOrObject);
            case KtProperty prop -> prop.getText();
            case KtTypeAlias alias -> alias.getText();
            default -> element.getText();
        };
    }


}