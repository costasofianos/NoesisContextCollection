package org.noesis.codeanalysis.util.kotlin.psi;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.noesis.codeanalysis.dataobjects.output.ContextEntry;
import org.noesis.codeanalysis.dataobjects.output.ContextEntryGroup;
import org.noesis.codeanalysis.dataobjects.output.ContextEntryWeight;
import org.noesis.codeanalysis.dataobjects.returnobjects.ResultWithExplanations;
import org.noesis.codeanalysis.interfaces.PsiElementFormatter;
import org.noesis.codeanalysis.util.constants.KotlinContextCollectorConstants;
import org.noesis.codeanalysis.util.html.HtmlUtil;
import org.noesis.codeanalysis.util.kotlin.general.KotlinSourceCodeUtil;

import java.util.*;
import java.util.stream.Collectors;

public class KotlinPsiElementContextEntryUtil {

    public static List<ContextEntry> getContextEntries(
            List<PsiElement> psiElements,
            PsiElementFormatter psiElementFormatter,
            ContextEntryWeight weight, String explanation) {


        return Optional.ofNullable(psiElements)
                .stream()
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(element -> getContextEntry(element, psiElementFormatter, weight, explanation))
                .collect(Collectors.toList());
    }

    private static ContextEntry getContextEntry(
            PsiElement element,
            PsiElementFormatter psiElementFormatter,
            ContextEntryWeight weight, String explanation) {

        return new ContextEntry(
                KotlinPsiElementUtil.getContainingFile(element),
                psiElementFormatter.format(element),
                weight, explanation
        );
    }

//    public static ResultWithExplanations<List<ContextEntry>> getContextEntriesFromElementMap(
//            List<String> typeReferences,
//            Map<String, List<PsiElement>> namesToDeclarations,
//            PsiElementFormatter psiElementFormatter,
//            ContextEntryWeight contextEntryWeight) {
//
//    List<ContextEntry> matchingElements = new ArrayList<>();
//    Map<String, List<Map<String, String>>> typeToElementDetailsMap = new HashMap<>();
//
//    typeReferences.stream()
//            .filter(namesToDeclarations::containsKey)
//            .forEach(type -> {
//                List<PsiElement> elements = namesToDeclarations.get(type);
//                elements.forEach(element ->
//                    matchingElements.add(getContextEntry(element, psiElementFormatter, contextEntryWeight, type))
//                );
//
//                typeToElementDetailsMap.put(type,
//                        elements.stream()
//                                .map(element -> {
//                                    Map<String, String> details = new LinkedHashMap<>(); // Keep insertion order
//                                    details.put("Element", psiElementFormatter.format(element));
//                                    details.put("File", KotlinPsiTreeUtil.findContainingKtFile(element).getName());
//                                    details.put("Type", element.getClass().getSimpleName());
//                                    return details;
//                                })
//                                .collect(Collectors.toList()));
//            });
//
//    // Create map of all referenced elements with details
//    Map<String, List<Map<String, String>>> allReferencedElementsDetailsMap =
//            namesToDeclarations.entrySet().stream()
//                    .collect(Collectors.toMap(
//                            Map.Entry::getKey,
//                            entry -> entry.getValue().stream()
//                                    .map(element -> {
//                                        Map<String, String> details = new LinkedHashMap<>();
//                                        details.put("Element", psiElementFormatter.format(element));
//                                        details.put("File", KotlinPsiTreeUtil.findContainingKtFile(element).getName());
//                                        details.put("Type", element.getClass().getSimpleName());
//                                        return details;
//                                    })
//                                    .collect(Collectors.toList())
//                    ));
//
//    ResultWithExplanations<List<ContextEntry>> result = new ResultWithExplanations<>(matchingElements);
//    result.addExplanation("Matched References",
//            HtmlUtil.generateMapWithMultipleColumnsHtmlTable(typeToElementDetailsMap));
////    result.addExplanation("All Referenced Elements",
////            HtmlUtil.generateMapWithMultipleColumnsHtmlTable(allReferencedElementsDetailsMap));
//    return result;
//}
public static ResultWithExplanations<List<ContextEntry>> getContextEntriesFromElementMap(
        KtFile editFile,
        List<String> typeReferences,
        Map<String, List<PsiElement>> namesToDeclarations,
        PsiElementFormatter psiElementFormatter,
        ContextEntryWeight contextEntryWeight) {

    List<ContextEntry> matchingElements = new ArrayList<>();
    Map<String, List<Map<String, String>>> typeToElementDetailsMap = new HashMap<>();

    // Track entries by KtFile as we build them
    Map<org.jetbrains.kotlin.psi.KtFile, List<ContextEntry>> entriesByFile = new LinkedHashMap<>();

    typeReferences.stream()
            .filter(namesToDeclarations::containsKey)
            .forEach(type -> {
                //List<PsiElement> elements = namesToDeclarations.get(type);
                List<PsiElement> elements = getLimitedResultsBySimilarity(editFile, namesToDeclarations.get(type));
                elements.forEach(element -> {
                    ContextEntry entry = getContextEntry(element, psiElementFormatter, contextEntryWeight, type);
                    matchingElements.add(entry);

                    org.jetbrains.kotlin.psi.KtFile ktFile = KotlinPsiTreeUtil.findContainingKtFile(element);
                    entriesByFile.computeIfAbsent(ktFile, k -> new ArrayList<>()).add(entry);
                });

                typeToElementDetailsMap.put(type,
                        elements.stream()
                                .map(element -> {
                                    Map<String, String> details = new LinkedHashMap<>(); // Keep insertion order
                                    details.put("Element", psiElementFormatter.format(element));
                                    details.put("File", KotlinPsiTreeUtil.findContainingKtFile(element).getName());
                                    details.put("Type", element.getClass().getSimpleName());
                                    return details;
                                })
                                .collect(Collectors.toList()));
            });

    // Create map of all referenced elements with details
    Map<String, List<Map<String, String>>> allReferencedElementsDetailsMap =
            namesToDeclarations.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().stream()
                                    .map(element -> {
                                        Map<String, String> details = new LinkedHashMap<>();
                                        details.put("Element", psiElementFormatter.format(element));
                                        details.put("File", KotlinPsiTreeUtil.findContainingKtFile(element).getName());
                                        details.put("Type", element.getClass().getSimpleName());
                                        return details;
                                    })
                                    .collect(Collectors.toList())
                    ));

    // Group existing entries by KtFile and replace the list with one summary ContextEntry per file
    List<ContextEntry> fileSummaryEntries = new ArrayList<>();
    entriesByFile.forEach((ktFile, entries) -> {
        String joinedEntries = entries.stream()
                .map(ContextEntry::text)
                .collect(Collectors.toCollection(LinkedHashSet::new)) // preserve original order, remove duplicates
                .stream()
                .collect(Collectors.joining(System.lineSeparator()));

        String filePath =
                ktFile != null && ktFile.getVirtualFile() != null
                        ? ktFile.getVirtualFile().getPath()
                        : (ktFile != null ? ktFile.getName() : "<unknown>");

        String summaryText = String.format(
                KotlinContextCollectorConstants.FILE_COMPOSE_FORMAT,
                KotlinContextCollectorConstants.FILE_SEP_SYMBOL,
                KotlinSourceCodeUtil.getRelativePathFromFullyQualifiedName(KotlinPsiElementUtil.getFullyQualifiedName(ktFile)),
                joinedEntries
        );

        fileSummaryEntries.add(new ContextEntry(
                new java.io.File(filePath),
                summaryText,
                contextEntryWeight,
                "Grouped entries from the same file"
        ));
    });

    // Replace loose entries with grouped summaries
    matchingElements.clear();
    matchingElements.addAll(fileSummaryEntries);

    ResultWithExplanations<List<ContextEntry>> result = new ResultWithExplanations<>(matchingElements);
    result.addExplanation("Matched References",
            HtmlUtil.generateMapWithMultipleColumnsHtmlTable(typeToElementDetailsMap));
//    result.addExplanation("All Referenced Elements",
//            HtmlUtil.generateMapWithMultipleColumnsHtmlTable(allReferencedElementsDetailsMap));
    return result;
}

    public static List<PsiElement> getLimitedResultsBySimilarity(KtFile editFile, List<PsiElement> matchedElements) {

        int limit = 3;
        if (matchedElements.size() <= limit) {
            return matchedElements;
        }

        // Sort by weight and take top 3
        //  referenceAssociations.sort((a1, a2) -> Float.compare(a2.weight(), a1.weight()));
        matchedElements.sort((psiElement1, psiElement2) -> {



            // If weights are equal, compare by reference similarity
            PsiElement associatedFile1 = psiElement1.getContainingFile();
            PsiElement associatedFile2 = psiElement2.getContainingFile();

            float similarity1 = KotlinPsiElementReferenceUtil.calculateReferenceOverlap(associatedFile1, editFile);
            float similarity2 = KotlinPsiElementReferenceUtil.calculateReferenceOverlap(associatedFile2, editFile);
            int compare = Float.compare(similarity2, similarity1);

//                    System.out.println("Similarity 1 ["+similarity1+"] "+associatedFile1.getContainingFile().getName()+" "+ KotlinPsiElementUtil.getSimpleNameOfContainingFile(referenceFile));
//                    System.out.println("Similarity 2 ["+similarity2+"] "+associatedFile2.getContainingFile().getName()+" "+KotlinPsiElementUtil.getSimpleNameOfContainingFile(referenceFile));

            return Float.compare(similarity2, similarity1); // Compare the two similarities in descending order
        });

        int min = Math.min(limit, matchedElements.size());
        return new ArrayList<>(matchedElements.subList(0, min));

    }


    public static Map<String, List<Map<String, String>>> createExplanationMap(List<ContextEntry> entries) {
        return entries.stream()
                .collect(Collectors.toMap(
                        ContextEntry::text,
                        entry -> List.of(createEntryDetails(entry)),
                        (existing, replacement) -> existing,  // Keep first occurrence if duplicates
                        LinkedHashMap::new  // Preserve order
                ));
    }

    public static Map<String, String> createEntryDetails(ContextEntry entry) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("File", entry.sourceFile().getName());
        details.put("Weight", entry.contextEntryWeight().toString());
        details.put("Explanation", entry.explanation());
        return details;
    }

    public static String createContextEntryGroupsHtmlTable(List<ContextEntryGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return "<p>No groups available</p>";
        }

        StringBuilder html = new StringBuilder();
        html.append("<table border='1' style='border-collapse: collapse; width: 100%;'>\n");

        for (ContextEntryGroup group : groups) {
            // Add group header row
            html.append("<tr style='background-color: #f2f2f2;'>\n")
                    .append("<td style='padding: 8px; font-weight: bold;'>")
                    .append(HtmlUtil.escapeHtml(group.text()))
                    .append("</td></tr>\n");

            // Add group entries details row
            html.append("<tr><td style='padding: 8px;'>\n");

            // Generate explanation table for the group's entries
            html.append(HtmlUtil.generateMapWithMultipleColumnsHtmlTable(
                    KotlinPsiElementContextEntryUtil.createExplanationMap(group.contextEntries())
            ));

            html.append("</td></tr>\n");
        }

        html.append("</table>");
        return html.toString();
    }


}