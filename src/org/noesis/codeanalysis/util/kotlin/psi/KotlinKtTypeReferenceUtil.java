package org.noesis.codeanalysis.util.kotlin.psi;

import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.kotlin.psi.KtTypeArgumentList;
import org.jetbrains.kotlin.psi.KtTypeProjection;
import org.jetbrains.kotlin.psi.KtTypeReference;
import org.jetbrains.kotlin.psi.KtUserType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class KotlinKtTypeReferenceUtil {


    public static List<String> extractTypeNames(List<KtTypeReference> typeReferences) {
        return typeReferences.stream()
                .flatMap(typeRef -> KotlinKtTypeReferenceUtil.extractTypeNames(typeRef).stream())
                .collect(Collectors.toList());
    }


    /**
     * Extracts all type names from a Kotlin type reference, including generic type arguments.
     * For qualified types, only the last part (actual type name) is included.
     * Returns types in order of appearance, with the main type first, followed by generic parameters.
     *
     * Examples:
     * <pre>
     * String                    -> ["String"]
     * List<String>             -> ["List", "String"]
     * Map<String, Int>         -> ["Map", "String", "Int"]
     * java.util.List<String>   -> ["List", "String"]
     * Map<List<String>, Int>   -> ["Map", "List", "String", "Int"]
     * Set<Map<String, Int>>    -> ["Set", "Map", "String", "Int"]
     * </pre>
     *
     * @param typeReference the KtTypeReference to analyze
     * @return List of type names found in the type reference, in order of appearance
     */
    public static List<String> extractTypeNames(KtTypeReference typeReference) {
        if (typeReference == null) {
            return Collections.emptyList();
        }

        List<String> typeNames = new ArrayList<>();

        KtUserType userType = PsiTreeUtil.findChildOfType(typeReference, KtUserType.class);
        if (userType != null) {
            // Get only the last part of qualified name
            String referenceName = userType.getReferencedName();
            if (referenceName != null) {
                typeNames.add(referenceName);
            }

            // Get type arguments (generic parameters)
            KtTypeArgumentList typeArgumentList = userType.getTypeArgumentList();
            if (typeArgumentList != null) {
                List<KtTypeProjection> typeArguments = typeArgumentList.getArguments();
                for (KtTypeProjection projection : typeArguments) {
                    KtTypeReference typeArg = projection.getTypeReference();
                    if (typeArg != null) {
                        typeNames.addAll(extractTypeNames(typeArg));
                    }
                }
            }
        }
        return typeNames;
    }
}
