package org.noesis.codeanalysis.util.kotlin.psi;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.psi.KtNameReferenceExpression;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.types.KotlinType;
import org.noesis.codeanalysis.util.general.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KotlinPsiTreeResolvedReferencedElementsUtil {


    private static final String ERROR_PREFIX = "[Error type: Unresolved type for ";


    public static List<String> findReferencedTypes(PsiElement psiElement, BindingContext bindingContext) {

        List<KtNameReferenceExpression> references = KotlinPsiTreeUtil.findEligibleReferences(psiElement);
        ArrayList<String> result = new ArrayList<>();
        for (KtNameReferenceExpression reference : references) {
            KotlinType referencedKotlinType = bindingContext.getType(reference);
            DeclarationDescriptor descriptor = bindingContext.get(org.jetbrains.kotlin.resolve.BindingContext.REFERENCE_TARGET, reference);

            Optional<String> resultTypeName = Optional.empty();

            String referencedKotlinTypeResult = "null";
            String descriptorResult = "null";

            if (referencedKotlinType != null) {
                referencedKotlinTypeResult = referencedKotlinType.toString();
                if (referencedKotlinTypeResult.startsWith(ERROR_PREFIX)) {
                    resultTypeName = Optional.of(referencedKotlinTypeResult.substring(ERROR_PREFIX.length(),
                            referencedKotlinTypeResult.length() - 1));


                } else if (referencedKotlinTypeResult.contains("Error type")) {
                    resultTypeName = Optional.of(reference.getText());
                } else {
                    resultTypeName = Optional.of(referencedKotlinTypeResult);
                }
            } else {

                if (descriptor != null) {

                    descriptorResult = descriptor.getName().asString();
                    resultTypeName = Optional.of(descriptorResult);

                } else {

                    resultTypeName = Optional.empty();

                }
            }

            System.out.println("[ORIGINAL TEXT] "+ StringUtils.padToLength(reference.getText(),20) +
                           " -> [RESULT] " + StringUtils.padToLength(resultTypeName.orElse("Not Set"), 20) +
                           " [RESOLVED TYPE] " + StringUtils.padToLength(referencedKotlinTypeResult,40) +
                           " [DESCRIPTOR] "+descriptorResult);
            ;

            resultTypeName.ifPresent(result::add);

        }
        return result;
    }
}
