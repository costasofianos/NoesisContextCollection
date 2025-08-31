package org.noesis.codeanalysis.util.kotlin.psi;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.psi.KtCallExpression;
import org.jetbrains.kotlin.psi.KtNameReferenceExpression;
import org.jetbrains.kotlin.psi.KtParameter;
import org.jetbrains.kotlin.psi.KtTypeReference;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPart;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPsiCallableReference;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPsiTypeReference;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KotlinPsiElementReferenceUtil {

    public static void collectReferencesFromParameters(List<KtParameter> parameters, KotlinSemanticPart<?> kotlinSemanticPart) {
        //System.out.println("Collecting Input Parameters");
        for (KtParameter parameter : parameters) {
           // System.out.println("Found Parameter: "+ parameter.getTypeReference()+" "+ parameter.getName() + " [" + parameter.getClass() + "]");
            KtTypeReference typeReference = parameter.getTypeReference();
            if (typeReference != null) {
               // System.out.println("Found TypeReference: " + typeReference.getText() + " [" + typeReference.getClass() + "]");
                collectReferencesFromPsiElement(typeReference, kotlinSemanticPart);
            }
        }
    }

    public static float calculateReferenceOverlap(PsiElement element1, PsiElement element2) {

        if (element1 == null || element2 == null) {
            return 0.0f;
        }

        // Get KtNameReferenceExpressions for both elements
        List<KtNameReferenceExpression> refs1 = KotlinPsiTreeUtil.findEligibleElements(element1, KtNameReferenceExpression.class);
        List<KtNameReferenceExpression> refs2 = KotlinPsiTreeUtil.findEligibleElements(element2, KtNameReferenceExpression.class);

        if (refs1.isEmpty() && refs2.isEmpty()) {
            return 1.0f; // Both have no references, consider them identical
        }
        if (refs1.isEmpty() || refs2.isEmpty()) {
            return 0.0f; // One has references, other doesn't - no overlap
        }

        // Convert references to their text representation for comparison
        Set<String> refTexts1 = refs1.stream()
                .map(KtNameReferenceExpression::getText)
                .collect(Collectors.toSet());
        Set<String> refTexts2 = refs2.stream()
                .map(KtNameReferenceExpression::getText)
                .collect(Collectors.toSet());

        // Calculate intersection size
        Set<String> intersection = new HashSet<>(refTexts1);
        intersection.retainAll(refTexts2);

        // Use Jaccard similarity: size of intersection / size of union
        Set<String> union = new HashSet<>(refTexts1);
        union.addAll(refTexts2);

        return union.isEmpty() ? 0.0f : (float) intersection.size() / union.size();
    }


    public static void collectReferencesFromPsiElement(PsiElement psiElement, KotlinSemanticPart<?> kotlinSemanticPart) {

        // Find all name references and create appropriate semantic references
        List<KtNameReferenceExpression> ktNameReferenceExpressions = KotlinPsiTreeUtil.findEligibleElements(psiElement, KtNameReferenceExpression.class);
        for (KtNameReferenceExpression ktNameReferenceExpression : ktNameReferenceExpressions) {

//           if (KotlinPsiElementUtil.getContainingFile(psiElement).getName().equals("FlowExt.kt")) {
//                System.out.println("Found NameReferenceExpression: " + ktNameReferenceExpression.getText());
//            }
            if (isFunctionReference(ktNameReferenceExpression)) {
//                if (KotlinPsiElementUtil.getContainingFile(psiElement).getName().equals("FlowExt.kt")) {
//                    System.out.println("It's a function reference, adding to "+kotlinSemanticPart.getClass().getSimpleName());
//                }
                kotlinSemanticPart.getSemanticReferences().add(
                        new KotlinSemanticPsiCallableReference(
                                ktNameReferenceExpression.getText(),
                                ktNameReferenceExpression,
                                kotlinSemanticPart
                        )
                );
            } else {
//                if (KotlinPsiElementUtil.getContainingFile(psiElement).getName().equals("FlowExt.kt")) {
//                    System.out.println("It's a type reference");
//                }
                kotlinSemanticPart.getSemanticReferences().add(
                        new KotlinSemanticPsiTypeReference(
                                ktNameReferenceExpression.getText(),
                                ktNameReferenceExpression,
                                kotlinSemanticPart
                        ) {
                        }
                );
            }
        }
    }

    private static boolean isFunctionReference(KtNameReferenceExpression ref) {
        PsiElement parent = ref.getParent();

        // Check if it's used as a method call
        if (parent instanceof KtCallExpression) {
            return true;
        } else return false;


    }

}
