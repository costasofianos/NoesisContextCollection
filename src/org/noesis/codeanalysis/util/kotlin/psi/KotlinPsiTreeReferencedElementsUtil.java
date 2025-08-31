package org.noesis.codeanalysis.util.kotlin.psi;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.kotlin.psi.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KotlinPsiTreeReferencedElementsUtil {




    public static Map<String, List<PsiElement>> getDeclarations(PsiElement psiElement) {

        //System.out.println("Getting public declarations for element: " + psiElement.getClass().getSimpleName() + " [" + psiElement.getClass() + "]");

        Map<String, List<PsiElement>> result = new HashMap<>();

        // First process the input element itself if it's a valid declaration
        if (isValidDeclaration(psiElement)) {
            KtNamedDeclaration named = (KtNamedDeclaration) psiElement;
            String name = named.getName();
            if (name != null) {
                //System.out.println("Adding public declaration: " + name+ " [" + psiElement.getClass() + "]");
                result.computeIfAbsent(name, k -> new ArrayList<>()).add(psiElement);
            }
        }

        // Collect all declarations in the tree, including nested ones
        for (KtDeclaration ktDeclaration : PsiTreeUtil.collectElementsOfType(psiElement, KtDeclaration.class)) {
            // Skip the input element since we've already processed it
            if (ktDeclaration == psiElement) {
                continue;
            }

            if (!isValidDeclaration(ktDeclaration)) {
                continue;
            }

            KtNamedDeclaration named = (KtNamedDeclaration) ktDeclaration;
            String name = named.getName();
            if (name == null) continue;

            result.computeIfAbsent(name, k -> new ArrayList<>()).add(ktDeclaration);

            // If it's a class or object, recursively process it to maintain proper naming hierarchy
            if (ktDeclaration instanceof KtClassOrObject) {
                Map<String, List<PsiElement>> nestedDeclarations = getDeclarations(ktDeclaration);
//                nestedDeclarations.forEach((key, value) ->
//                    result.computeIfAbsent(key, k -> new ArrayList<>()).addAll(value)
//                );
            }
        }

        return result;
    }

    private static boolean isValidDeclaration(PsiElement element) {
        if (!(element instanceof KtNamedDeclaration)) {
            return false;
        }

        // Check if it's one of the supported declaration types
        if (!(element instanceof KtNamedFunction ||
                element instanceof KtClassOrObject ||
                element instanceof KtProperty ||
                element instanceof KtTypeAlias ||
                element instanceof KtConstructor)) {
            return false;
        }

        // If it's a property, perform additional checks
        if (element instanceof KtProperty) {
            KtProperty property = (KtProperty) element;

            // Check if it's a local variable
            if (property.isLocal()) {
                return false;
            }

            // Check if the property is declared at class/file level
            PsiElement parent = property.getParent();
            if (!(parent instanceof KtClassBody || parent instanceof KtFile)) {
                return false;
            }
        }

        // Check if the declaration is public
        return true;
    }


    /**************  Find Types in Changed File ***********/

    public static List<String> findReferencedTypes(KtFile ktFile) {
        List<String> referencedTypes = new ArrayList<>();

        // Process imports first
        for (KtImportDirective importDirective : ktFile.getImportDirectives()) {
            String importedReference = importDirective.getImportedReference().getText();
            if (importedReference != null) {
                // Get the last part of the import path which is the type name
                String importedType = importedReference.substring(importedReference.lastIndexOf('.') + 1);
                referencedTypes.add(importedType);
            }
        }

        // Process references in the code
        PsiTreeUtil.processElements(ktFile, element -> {
            if (element instanceof KtNameReferenceExpression nameReferenceExpression) {

               if (nameReferenceExpression.getReference() != null) {
                   PsiElement psiElement = nameReferenceExpression.getReference().getElement();

                   if (psiElement == null) {
                       referencedTypes.add(psiElement.getText());
                   }
               }else {
                    referencedTypes.add(nameReferenceExpression.getReferencedName());
                }
            }
            return true;
        });

        return referencedTypes;
    }
}