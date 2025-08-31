package org.noesis.codeanalysis.util.kotlin.psi;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.com.intellij.psi.PsiFile;
import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.kotlin.psi.*;
import org.noesis.codeanalysis.util.kotlin.psi.formatters.KotlinDeclarationSignatureFormatter;

import java.util.*;

public class KotlinPsiTreeUtil {

    static Set<String> standardKotlinIdentifiers = Set.of(
            // Common Kotlin standard library types
            "Any", "Unit", "String", "Int", "Long", "Double", "Float", "Boolean", "Byte", "Short", "Char",
            "Array", "List", "Set", "Map", "Pair", "Triple", "Sequence", "Collection",
            "MutableList", "MutableSet", "MutableMap",
            // Common type modifiers and standard types
            "Default", "Companion", "Builder", "Factory",
            // Common collection operations
            "isEmpty", "isNotEmpty", "size", "contains", "forEach", "map", "filter", "first", "last",
            // Standard scope functions
            "let", "run", "with", "apply", "also",
            // Common property accessors
            "get", "set", "getValue", "setValue"
    );

    public static Optional<KtFile> findKtFileByName(List<KtFile> ktFiles, String fileName) {
        if (ktFiles == null || fileName == null) {
            return Optional.empty();
        }

        return ktFiles.stream()
                .filter(ktFile -> fileName.equals(ktFile.getName()))
                .findFirst();
    }


    public static List<KtNameReferenceExpression> findEligibleReferences(PsiElement psiElement) {
        return findEligibleElements(psiElement, KtNameReferenceExpression.class);
    }

    public static List<String> findEligibleKtNameReferencedExpressionNames(PsiElement psiElement) {
        return findEligibleReferences(psiElement)
                .stream()
                .map(KtNameReferenceExpression::getText)
                .toList();
    }

    public static List<KtTypeReference> getEligibleReferencedTypes(PsiElement... psiElements) {
        return findEligibleElements(KtTypeReference.class, psiElements);
    }

    public static <T extends PsiElement> List<T> findEligibleElements(Class<T> elementClass, PsiElement... psiElements) {
        List<T> elements = new ArrayList<>();
        for (PsiElement psiElement : psiElements) {
            elements.addAll(findEligibleElements(psiElement, elementClass));
        }
        return elements;
    }

    public static <T extends PsiElement> List<T> findEligibleElements(PsiElement psiElement, Class<T> elementClass) {
        Collection<T> elements = PsiTreeUtil.findChildrenOfType(psiElement, elementClass);
        return elements.stream()
                .filter(element -> isEligibleIdentifier(element.getText()))
                .toList();
    }

    private static boolean isEligibleIdentifier(String name) {
        // Split on dots first to handle qualified names
        String[] parts = name.split("\\.");
        // Check each part before any generic parameters
        for (String part : parts) {
            // Get the type name without generic parameters
            String simpleName = part.split("<")[0].trim();
            if (standardKotlinIdentifiers.contains(simpleName)
                    || simpleName.startsWith("kotlin.")
                    || simpleName.startsWith("java.")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds comment element containing the specified text
     *
     * @param rootElement The PSI element to start search from
     * @param commentText The text to search for in comments
     * @return Optional containing the found comment element, or empty if not found
     */
    public static Optional<PsiElement> findDeepestElementWithText(PsiElement rootElement, String commentText) {
        if (rootElement == null || commentText == null) {
            return Optional.empty();
        }

//        System.out.println("Finding element with text: '" + commentText + "', checking element: "
//            + rootElement.getClass().getSimpleName() + " [" + rootElement.getText() + "]");

        // First check if any child contains the text - if so, recurse into that child
        for (PsiElement child : rootElement.getChildren()) {
            if (child.getText().contains(commentText)) {
                return findDeepestElementWithText(child, commentText);
            }
        }

        // If no child contains the text but this is a matching comment, return it
        if (rootElement.getText().contains(commentText)) {
            //System.out.println("Found deepest matching element: " + rootElement.getClass().getSimpleName()
            //        + " [" + rootElement.getText() + "]");
            return Optional.of((PsiElement) rootElement);
        }

        return Optional.empty();
    }


    public static List<PsiElement> findParentElements(PsiElement element) {
        List<PsiElement> parents = new ArrayList<>();
        PsiElement current = element.getParent();

        while (current != null) {
            parents.add(current);
            current = current.getParent();
        }

        return parents;
    }


    public static <T extends PsiElement> Optional<T> findFirstParentOfType(PsiElement element, Class<T> targetClass) {
        return findParentsOfType(element, targetClass).stream()
                .findFirst();
    }



    public static <T extends PsiElement> List<T> findParentsOfType(PsiElement element, Class<T> targetClass) {
        List<T> elements = new ArrayList<>();
        
        if (element == null) {
            return elements;
        }

        PsiElement currentElement = element;
        while (currentElement != null) {
            if (targetClass.isInstance(currentElement)) {
                elements.add(targetClass.cast(currentElement));
            }
            currentElement = currentElement.getParent();
        }

        return elements;
    }

    public static KtFile findContainingKtFile(PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        if (containingFile instanceof KtFile) {
            return (KtFile) containingFile;
        }
        return null; // Or handle this case as needed
    }



    private static void processDeclaration(KtDeclaration declaration, String container, List<String> collector) {
        if (declaration instanceof KtNamedFunction || declaration instanceof KtProperty) {
            KtModifierListOwner member = (KtModifierListOwner) declaration;
            if (KotlinPsiElementUtil.isPublic(member)) {
                String name = declaration.getName();
                if (name != null) {
                    String qualified = (container != null) ? container + "." + name : name;
                    collector.add(new KotlinDeclarationSignatureFormatter().format(declaration));
                }
            }
        } else if (declaration instanceof KtClassOrObject) {
            traverseClassOrObject((KtClassOrObject) declaration, container, collector);
        }
    }

    private static void traverseClassOrObject(KtClassOrObject klass, String parent, List<String> collector) {
        String containerName = klass.getName();
        if (containerName == null) return;

        String qualifiedContainer = (parent != null) ? parent + "." + containerName : containerName;
        KtClassBody body = klass.getBody();
        if (body == null) return;

        for (KtDeclaration member : body.getDeclarations()) {
            processDeclaration(member, qualifiedContainer, collector);
        }
    }



    public static String getFullyQualifiedFileName(KtFile ktFile) {
        String packageName = ktFile.getPackageFqName().asString();  // e.g. "com.example"
        String fileName = ktFile.getName();                         // e.g. "MyFile.kt"

       return packageName.isEmpty()
                ? fileName
                : packageName + "." + fileName;

    }

}