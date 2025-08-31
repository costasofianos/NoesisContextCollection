package org.noesis.codeanalysis.computations.kotlin.psi;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.noesis.codeanalysis.dataobjects.enums.ElementVisibility;
import org.noesis.codeanalysis.dataobjects.input.ContextCollectionInput;
import org.noesis.codeanalysis.interfaces.Computation;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiTreeReferencedElementsUtil;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KotlinRepoKtFilesToDeclarationsComputation extends Computation {

    /**
     *  Store string to KtElement.  Process element as needed to get the relevant string
     */


    KotlinRepoPsiTreeComputation kotlinPsiTreeComputation;
    Map<KtFile, EnumMap<ElementVisibility, List<PsiElement>>> ktFilesToDeclarations;

    public KotlinRepoKtFilesToDeclarationsComputation(ContextCollectionInput contextCollectionInput, KotlinRepoPsiTreeComputation kotlinPsiTreeComputation) {

        super(contextCollectionInput);
        this.kotlinPsiTreeComputation = kotlinPsiTreeComputation;
        ktFilesToDeclarations = populateDeclarationsForFiles(kotlinPsiTreeComputation.getOriginalPsiTree());

    }

    public Map<KtFile, EnumMap<ElementVisibility, List<PsiElement>>> populateDeclarationsForFiles(List<KtFile> ktFiles) {
        String repoFolder = getContextCollectionInput().getRepoFolder().toString();

        return ktFiles.stream()
                .collect(Collectors.toMap(
                        Function.identity() ,
                        KotlinRepoKtFilesToDeclarationsComputation::populateDeclarations
                ));
    }



    public static EnumMap<ElementVisibility, List<PsiElement>> populateDeclarations(KtFile ktFile) {

        Map<String, List<PsiElement>> declarations = KotlinPsiTreeReferencedElementsUtil.getDeclarations(ktFile);


//        if (ktFile.getName().equals("PackageHandler.kt")) {
//            System.out.println("Processing file: " + ktFile.getName());
//
//
//            if (declarations.containsKey("deduplicate")) {
//                System.out.println("Found 'deduplicate' in declarations map");
//                List<PsiElement> elements = declarations.get("deduplicate");
//                System.out.println("Number of elements: " + elements.size());
//                elements.forEach(element ->
//                        System.out.println("Element text: " + element.getText())
//                );
//            } else {
//                System.out.println("'deduplicate' not found in declarations map");
//            }
//        }

        EnumMap<ElementVisibility, List<PsiElement>> visibilityMap = new EnumMap<>(ElementVisibility.class);
        visibilityMap.put(ElementVisibility.PUBLIC, new ArrayList<>());
        visibilityMap.put(ElementVisibility.NON_PUBLIC, new ArrayList<>());

        declarations.forEach((fileName, elements) -> {
            elements.forEach(element -> {
                ElementVisibility visibility = KotlinPsiElementUtil.getElementVisibility(element);
                visibilityMap.get(visibility).add(element);
            });
        });

        return visibilityMap;
    }

    public static Optional<EnumMap<ElementVisibility, List<PsiElement>>> findDeclarationsForFile(
            String path,
            Map<String, EnumMap<ElementVisibility, List<PsiElement>>> ktFilesToDeclarations) {

        List<String> segments = Arrays.asList(path.split("/"));

        for (int i = segments.size() - 2; i >= 0; i--) {
            String candidateFqn = String.join(".", segments.subList(i, segments.size()))
                    .replace(".kt", "");

            if (ktFilesToDeclarations.containsKey(candidateFqn)) {
                return Optional.of(ktFilesToDeclarations.get(candidateFqn));
            }
        }

        return Optional.empty();
    }

    public List<PsiElement> getKtFileElements(String fileName, ElementVisibility... allowedVisibilities) {
        return getKtFilesToElements(allowedVisibilities)
                .getOrDefault(fileName, Collections.emptyList());
    }

    public Map<KtFile, List<PsiElement>> getKtFilesToElements(ElementVisibility... allowedVisibilities) {

        Map<KtFile, EnumMap<ElementVisibility, List<PsiElement>>> declarations = getKtFilesToDeclarations();
        Set<ElementVisibility> visibilitySet = new HashSet<>(Arrays.asList(allowedVisibilities));

        return declarations.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().entrySet().stream()
                                .filter(visibilityListEntry -> visibilitySet.contains(visibilityListEntry.getKey()))
                                .flatMap(visibilityListEntry -> visibilityListEntry.getValue().stream())
                                .collect(Collectors.toList())
                ));
    }


    public Map<KtFile, EnumMap<ElementVisibility, List<PsiElement>>> getKtFilesToDeclarations() {
        return ktFilesToDeclarations;
    }

    @Override
    public void close() throws Exception {
        kotlinPsiTreeComputation = null;
        ktFilesToDeclarations = null;
    }
}