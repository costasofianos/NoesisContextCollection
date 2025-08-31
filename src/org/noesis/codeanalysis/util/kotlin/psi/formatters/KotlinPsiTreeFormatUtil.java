package org.noesis.codeanalysis.util.kotlin.psi.formatters;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.psi.KtClassOrObject;
import org.jetbrains.kotlin.psi.KtConstructor;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtVariableDeclaration;

public class KotlinPsiTreeFormatUtil {

    public static String getKtNamedFunctionSignature(KtNamedFunction ktNamedFunction) {
    if (ktNamedFunction.getBodyExpression() != null) {
        int bodyStart = ktNamedFunction.getBodyExpression().getStartOffsetInParent();
        String signature = ktNamedFunction.getText().substring(0, bodyStart).trim();
        if (signature.endsWith("=")) {
            signature = signature.substring(0, signature.length() - 1).trim();
        }
        return signature;
    }
    return ktNamedFunction.getText().trim();
}

    public static String getKtClassOrObjectHeader(KtClassOrObject ktClassOrObject) {
        return getTextBeforeStart(ktClassOrObject);
    }

    public static String getKtVariableSignature(KtVariableDeclaration ktVariableDeclaration) {
        return getTextBeforeBlockOrExpression(ktVariableDeclaration);
    }

    public static String getKtConstructorSignature(KtConstructor<?> ktConstructor) {
        return getTextBeforeBlockOrExpression(ktConstructor);
    }

    public static String getTextBeforeStart(PsiElement psiElement) {
        return psiElement.getText().split("\\{")[0].trim();
    }

    private static String getTextBeforeBlockOrExpression(PsiElement element) {
        String text = element.getText();
        int blockStart = text.indexOf('{');
        int equalsStart = text.indexOf('=');
        
        if (blockStart < 0 && equalsStart < 0) {
            return text.trim();
        }
        
        if (blockStart < 0) {
            return text.substring(0, equalsStart).trim();
        }
        
        if (equalsStart < 0) {
            return text.substring(0, blockStart).trim();
        }
        
        return text.substring(0, Math.min(blockStart, equalsStart)).trim();
    }
}