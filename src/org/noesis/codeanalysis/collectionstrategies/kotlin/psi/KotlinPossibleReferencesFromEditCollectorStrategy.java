package org.noesis.codeanalysis.collectionstrategies.kotlin.psi;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.noesis.codeanalysis.collectionstrategies.general.ContextCollectionStrategy;
import org.noesis.codeanalysis.computations.general.cache.ComputationFactory;
import org.noesis.codeanalysis.computations.kotlin.psi.KotlinEditFilePsiTreeComputation;
import org.noesis.codeanalysis.computations.kotlin.psi.KotlinRepoNamesToDeclarationsComputation;
import org.noesis.codeanalysis.control.ContextCollector;
import org.noesis.codeanalysis.dataobjects.output.ContextEntry;
import org.noesis.codeanalysis.dataobjects.output.ContextEntryWeight;
import org.noesis.codeanalysis.dataobjects.returnobjects.ResultWithExplanations;
import org.noesis.codeanalysis.interfaces.PsiElementFormatter;
import org.noesis.codeanalysis.util.html.HtmlUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiTreeCompactHtmlUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiTreeReferencedElementsUtil;
import org.noesis.codeanalysis.util.kotlin.psi.formatters.KotlinDeclarationSignatureFormatter;

import java.util.List;
import java.util.Map;

public class KotlinPossibleReferencesFromEditCollectorStrategy extends ContextCollectionStrategy {

    private  KotlinRepoNamesToDeclarationsComputation kotlinRepoNamesToDeclarationsComputation;
    private  KotlinEditFilePsiTreeComputation kotlinEditFilePsiTreeComputation;

    public KotlinPossibleReferencesFromEditCollectorStrategy(ContextCollector contextCollector, ComputationFactory computationFactory) {
        super(contextCollector, computationFactory);
        kotlinRepoNamesToDeclarationsComputation = getComputationFactory().getInstance(KotlinRepoNamesToDeclarationsComputation.class);
        kotlinEditFilePsiTreeComputation = getComputationFactory().getInstance(KotlinEditFilePsiTreeComputation.class);
    }

    @Override
    public void populateCollectionStrategyResults() {

        List<PsiElement> elementsAboveCode = kotlinEditFilePsiTreeComputation.findParentsOfMissingCode();

        /*********************** Basic Explanations for Analysis **************************************/
        addExplanation("Relevant Parent Elements", generatePsiElementTable(elementsAboveCode));
        addExplanation("Edit File Tree ", KotlinPsiTreeCompactHtmlUtil.generatePsiTreeHtml(kotlinEditFilePsiTreeComputation.getEditFilePsiTree()));
        /**********************************************************************************************/

        /*********************** Search from most relevant and move to less relevance *****************/

        /*********************** Search for matched declarations in containing function (Bodies) ********/

//        Optional<PsiElement> deepestMissingCodeElement = kotlinEditFilePsiTreeComputation.findDeepestMissingCodeElement();
//
//        if (deepestMissingCodeElement.isPresent()) {
//
//            Optional<KtNamedFunction> containingFunction = KotlinPsiTreeUtil.findFirstParentOfType(deepestMissingCodeElement.get(), KtNamedFunction.class);
//
//            if (containingFunction.isPresent()) {
//                //get body of similar functions
//                //System.out.println("Searching for function "+containingFunction.get().getName());
//                List<String> similarFunctions = List.of(KotlinPsiElementUtil.getName(containingFunction.get()));
//                addMatchingElementSignatures(similarFunctions, false, new KotlinDeclarationBodyFormatter(),0.9f, new KotlinDeclarationBodyFormatter(),0.85f);
//
//
//                //find types in function
//                List<String> containingFunctionEditFileReferences = KotlinPsiTreeReferencedElementsUtil.findReferencedTypes(
//                        kotlinEditFilePsiTreeComputation.getEditFilePsiTree()
//                );
////                List<String> containingFunctionEditFileReferences = KotlinPsiTreeResolvedReferencedElementsUtil.findReferencedTypes(
////                        containingFunction.get(), kotlinEditFilePsiTreeComputation.getBindingContext());
//
//                addMatchingElementSignatures(containingFunctionEditFileReferences, true, new KotlinDeclarationBodyFormatter(),1.0f, new KotlinDeclarationSignatureFormatter(),0.95f);
//            }
//        }


        KtFile editFile = kotlinEditFilePsiTreeComputation.getEditFilePsiTree();
        List<String> allEditFileReferences = KotlinPsiTreeReferencedElementsUtil.findReferencedTypes(
                editFile
        );
//        List<String> allEditFileReferences = KotlinPsiTreeResolvedReferencedElementsUtil.findReferencedTypes(
//                kotlinEditFilePsiTreeComputation.getEditFilePsiTree(), kotlinEditFilePsiTreeComputation.getBindingContext());

        addExplanation("Relevant Types", HtmlUtil.generateStringListHtmlTable(allEditFileReferences));

        addMatchingElementSignatures(editFile, allEditFileReferences, true, new KotlinDeclarationSignatureFormatter(),0.8f, new KotlinDeclarationSignatureFormatter(),0.75f);
    }

    private void addMatchingElementSignatures(KtFile editFile, List<String> editReferences, boolean publicReferencesOnly, PsiElementFormatter formatterForModifiedFiles, float weightForModifiedFiles, PsiElementFormatter formatterForAllFiles, float weightForAllFiles) {

        ResultWithExplanations<List<ContextEntry>> matchingPsiModifiedElements = kotlinRepoNamesToDeclarationsComputation.findReferencedElements(editFile, editReferences, publicReferencesOnly, true, formatterForModifiedFiles, ContextEntryWeight.of(weightForModifiedFiles));
        addContextEntryInNewGroup("Relevant code in Modified Files",matchingPsiModifiedElements.getResult());
        /*********************** Search for matched declarations in remaining files (Signatures) ********/

        ResultWithExplanations<List<ContextEntry>> matchingPsiElements = kotlinRepoNamesToDeclarationsComputation.findReferencedElements(editFile, editReferences, publicReferencesOnly, false, formatterForAllFiles, ContextEntryWeight.of(weightForAllFiles));
        addContextEntryInNewGroup("Relevant code in Other Files",matchingPsiElements.getResult());
    }






    private static String generateHtmlTable(Map<String, KtFile> classMap) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<table border='1'>");

        // Table header
        html.append("<tr>");
        html.append("<th>Class Name</th>");
        html.append("<th>File Path</th>");
        html.append("</tr>");

        // Sort entries alphabetically by key
        classMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    html.append("<tr>");
                    html.append("<td>").append(entry.getKey()).append("</td>");
                    html.append("<td>").append(entry.getValue().getVirtualFile().getPath()).append("</td>");
                    html.append("</tr>");
                });

        html.append("</table>");
        html.append("</body></html>");

        return html.toString();
    }


    public static String generatePsiElementTable(List<PsiElement> elements) {
        StringBuilder htmlTable = new StringBuilder();
        htmlTable.append("<table border='1'>\n")
                .append("  <thead>\n")
                .append("    <tr>\n")
                .append("      <th>Level</th>\n")
                .append("      <th>Element Type</th>n")
                .append("      <th>Text</th>\n")
                .append("    </tr>\n")
                .append("  </thead>\n")
                .append("  <tbody>\n");

        for (int i = 0; i < elements.size(); i++) {
            PsiElement element = elements.get(i);
            htmlTable.append("    <tr>\n")
                    .append("      <td>").append(i).append("</td>\n")
                    .append("      <td>").append(HtmlUtil.escapeHtml(element.getClass().getSimpleName())).append("</td>\n")
                    .append("      <td>").append(HtmlUtil.escapeHtml(element.getText())).append("</td>\n")
                    .append("    </tr>\n");
        }

        htmlTable.append("  </tbody>\n")
                .append("</table>");

        return htmlTable.toString();

    }


    @Override
    public void close() throws Exception {
        kotlinRepoNamesToDeclarationsComputation = null;
        kotlinEditFilePsiTreeComputation = null;
    }
}