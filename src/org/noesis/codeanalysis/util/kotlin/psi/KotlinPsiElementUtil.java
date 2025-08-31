package org.noesis.codeanalysis.util.kotlin.psi;

import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.com.intellij.psi.PsiFile;
import org.jetbrains.kotlin.com.intellij.psi.PsiNamedElement;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtModifierList;
import org.jetbrains.kotlin.psi.KtModifierListOwner;
import org.noesis.codeanalysis.dataobjects.enums.ElementVisibility;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

public class KotlinPsiElementUtil {



    public static String getName(PsiElement element) {
        if (element == null) {
            return null;
        }

        // Handle PsiNamedElement interface which most named elements implement
        if (element instanceof PsiNamedElement) {
            return ((PsiNamedElement) element).getName();
        }

        // For elements that don't implement PsiNamedElement but still have text
        return element.getText();
    }

    public static String getFullyQualifiedName(PsiElement element) {
        if (element == null) {
            return "";
        }

        PsiFile containingFile = element.getContainingFile();
        if (containingFile instanceof KtFile) {
            KtFile ktFile = (KtFile) containingFile;
            String packageName = ktFile.getPackageFqName().asString();
            String fileName = containingFile.getName();
            return packageName.isEmpty() ? fileName : packageName + "." + fileName;
        }

        return containingFile != null ? containingFile.getName() : "";
    }


    public static String getSimpleNameOfContainingFile(PsiElement element) {
        File file = getContainingFile(element);
        return file.getName();
    }


    public static File getContainingFile(PsiElement element) {
        VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
        return new File(virtualFile.getPath());
    }


    public static ElementVisibility getElementVisibility(PsiElement element) {
        if (element instanceof KtModifierListOwner modifierOwner) {
            if (isPublic(modifierOwner)) {
                return ElementVisibility.PUBLIC;
            } else {
                return ElementVisibility.NON_PUBLIC;
            }
        } else {
            return ElementVisibility.PUBLIC;
        }
    }

    public static boolean isPublic(KtModifierListOwner element) {
        KtModifierList modifierList = element.getModifierList();
        if (modifierList == null) return true; // default is public
        return !modifierList.hasModifier(KtTokens.PRIVATE_KEYWORD);
    }

    public static Set<String> filterReferencedTypesByPosition(PsiElement element, Set<String> referencedTypeNames, String stopAt) {
        if (element == null || referencedTypeNames == null || referencedTypeNames.isEmpty()) {
            return Set.of();
        }

        String text = element.getText();
        int stopPosition = stopAt != null ? text.indexOf(stopAt) : text.length();

        if (stopPosition == -1) {
            stopPosition = text.length();
        }

        String textUpToStop = text.substring(0, stopPosition);
        return referencedTypeNames.stream()
                .filter(typeName -> textUpToStop.contains(typeName))
                .collect(Collectors.toSet());
    }





}