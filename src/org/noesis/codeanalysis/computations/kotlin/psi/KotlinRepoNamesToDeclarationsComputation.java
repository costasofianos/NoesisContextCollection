package org.noesis.codeanalysis.computations.kotlin.psi;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.noesis.codeanalysis.dataobjects.enums.ElementVisibility;
import org.noesis.codeanalysis.dataobjects.input.ContextCollectionInput;
import org.noesis.codeanalysis.dataobjects.output.ContextEntry;
import org.noesis.codeanalysis.dataobjects.output.ContextEntryWeight;
import org.noesis.codeanalysis.dataobjects.returnobjects.ResultWithExplanations;
import org.noesis.codeanalysis.interfaces.Computation;
import org.noesis.codeanalysis.interfaces.PsiElementFormatter;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementContextEntryUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementUtil;

import java.util.*;

public class KotlinRepoNamesToDeclarationsComputation extends Computation {

    /**
     *  Store string to KtElement.  Process element as needed to get the relevant string
     */


    KotlinRepoKtFilesToDeclarationsComputation kotlinRepoKtFilesToDeclarationsComputation;
    //Find relevant type from Referenced Types (use full package name?)
    Map<String, List<PsiElement>> namesToAllDeclarations;
    Map<String, List<PsiElement>> namesToPublicDeclarations;
    Map<String, List<PsiElement>> namesToAllModifiedDeclarations;
    Map<String, List<PsiElement>> namesToPublicModifiedDeclarations;


    public KotlinRepoNamesToDeclarationsComputation(ContextCollectionInput contextCollectionInput, KotlinRepoKtFilesToDeclarationsComputation kotlinRepoKtFilesToDeclarationsComputation, KotlinRepoPsiTreeCollectionInputComputation kotlinRepoPsiTreeCollectionInputComputation) {

        super(contextCollectionInput);
        this.kotlinRepoKtFilesToDeclarationsComputation = kotlinRepoKtFilesToDeclarationsComputation;
        namesToAllDeclarations = createReferencedElementsMap(kotlinRepoKtFilesToDeclarationsComputation.getKtFilesToDeclarations(), ElementVisibility.PUBLIC, ElementVisibility.NON_PUBLIC);
        namesToPublicDeclarations = createReferencedElementsMap(kotlinRepoKtFilesToDeclarationsComputation.getKtFilesToDeclarations(), ElementVisibility.PUBLIC);

        List<String> modifiedFiles = getContextCollectionInput().getContextCollectionInputJsonLine().modified();
        Map<KtFile, EnumMap<ElementVisibility, List<PsiElement>>> filteredDeclarations = new HashMap<>(kotlinRepoKtFilesToDeclarationsComputation.getKtFilesToDeclarations());
        filteredDeclarations.entrySet().removeIf(entry -> !kotlinRepoPsiTreeCollectionInputComputation.isModified(entry.getKey()));

        namesToAllModifiedDeclarations = createReferencedElementsMap(filteredDeclarations, ElementVisibility.PUBLIC, ElementVisibility.NON_PUBLIC);
        namesToPublicModifiedDeclarations = createReferencedElementsMap(filteredDeclarations, ElementVisibility.PUBLIC);
}

public ResultWithExplanations<List<ContextEntry>> findReferencedElements(
        KtFile editFile,
        List<String> typeReferences,
        boolean publicReferencesOnly,
        boolean modifiedFilesOnly,
        PsiElementFormatter formatter,
        ContextEntryWeight contextEntryWeight) {

    Map<String, List<PsiElement>> declarations;
   if (modifiedFilesOnly) {
       if (publicReferencesOnly) {
           declarations = namesToPublicModifiedDeclarations;
       } else {
           declarations = namesToAllModifiedDeclarations;
       }
   } else {
       if (publicReferencesOnly) {
           declarations = namesToPublicDeclarations;
       } else {
           declarations = namesToAllDeclarations;
       }
   }

//    System.out.println("[publicReferencesOnly = " + publicReferencesOnly + ", modifiedFilesOnly = " + modifiedFilesOnly + "]");
//    if (declarations.containsKey("deduplicate")) {
//        System.out.println("Found 'deduplicate' in declarations map");
//        List<PsiElement> elements = declarations.get("deduplicate");
//        System.out.println("Number of elements: " + elements.size());
//        elements.forEach(element ->
//                System.out.println("Element text: " + element.getText())
//        );
//    } else {
//        System.out.println("'deduplicate' not found in declarations map");
//    }




    return KotlinPsiElementContextEntryUtil.getContextEntriesFromElementMap(
            editFile,
            typeReferences,
            declarations,
            formatter,
            contextEntryWeight);
}



    private Map<String, List<PsiElement>> getNamesToAllDeclarations() {

        return namesToAllDeclarations;
    }

    public static Map<String, List<PsiElement>> createReferencedElementsMap(
            Map<KtFile, EnumMap<ElementVisibility, List<PsiElement>>> ktFilesToDeclarations,
            ElementVisibility... visibilityFilters) {

        Map<String, List<PsiElement>> result = new HashMap<>();
        Set<ElementVisibility> visibilitySet = visibilityFilters.length > 0
                ? EnumSet.of(visibilityFilters[0], visibilityFilters)
                : EnumSet.allOf(ElementVisibility.class);

        ktFilesToDeclarations.forEach((key, visibilityMap) -> {
            visibilityMap.entrySet().stream()
                    .filter(entry -> visibilitySet.contains(entry.getKey()))
                    .map(Map.Entry::getValue)
                    .flatMap(List::stream)
                    .forEach(element ->
                            result.computeIfAbsent(KotlinPsiElementUtil.getName(element), k -> new ArrayList<>()).add(element)
                    );
        });

        return result;
    }

    @Override
    public void close() throws Exception {
        kotlinRepoKtFilesToDeclarationsComputation = null;
        namesToAllDeclarations = null;
        namesToPublicDeclarations = null;
        namesToAllModifiedDeclarations = null;
        namesToPublicModifiedDeclarations = null;
    }
}