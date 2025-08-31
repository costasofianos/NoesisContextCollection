package org.noesis.codeanalysis.util.kotlin.psi;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement;

import java.util.Arrays;
import java.util.Set;

public class KotlinPsiTreeHtmlUtil {
    private static final int MAX_TEXT_LENGTH = 50000;

    private static final Set<Class<? extends PsiElement>> DEFAULT_EXCLUDED_ELEMENTS = Set.of(
            //KtDotQualifiedExpression.class
    );

    public static String generatePsiTreeHtml(PsiElement rootElement) {
        return generatePsiTreeHtml(rootElement, DEFAULT_EXCLUDED_ELEMENTS);
    }

    public static String generatePsiTreeHtml(PsiElement rootElement, Set<Class<? extends PsiElement>> excludedElements) {
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<table>");
        generateHtml(rootElement, htmlBuilder, excludedElements);
        htmlBuilder.append("</table>");
        return htmlBuilder.toString();
    }

    private static void generateHtml(PsiElement element, StringBuilder htmlBuilder, Set<Class<? extends PsiElement>> excludedElements) {
        htmlBuilder.append("<tr><td>");
        htmlBuilder.append(createNodeHtml(element));

        PsiElement[] children = getFilteredChildren(element, excludedElements);
        if (children.length > 0) {
            htmlBuilder.append("<table>");
            for (PsiElement child : children) {
                generateHtml(child, htmlBuilder, excludedElements);
            }
            htmlBuilder.append("</table>");
        }

        htmlBuilder.append("</td></tr>");
    }

    private static String createNodeHtml(PsiElement element) {
        String elementType = element.getClass().getSimpleName();
        String text = truncateAndEscapeText(element.getText());

        boolean isError = element instanceof PsiErrorElement;
        if (isError) {
            PsiErrorElement errorElement = (PsiErrorElement) element;
//            System.out.printf("Found PSI error: %s at offset %d%n",
//                    errorElement.getErrorDescription(),
//                    errorElement.getTextOffset());
        }

        String errorClass = isError ? " error" : "";

        return String.format("""
        <div class="node%s">
            <span class="element-type">%s</span>
            <span class="text">%s</span>
            %s
        </div>""",
        errorClass,
        elementType,
        text,
        isError ? "<span class=\"error-message\">" +
                  escapeHtml(((PsiErrorElement)element).getErrorDescription()) +
                  "</span>" : "");
    }
    private static PsiElement[] getFilteredChildren(PsiElement element, Set<Class<? extends PsiElement>> excludedElements) {
        return Arrays.stream(element.getChildren())
                .filter(child -> !isExcluded(child, excludedElements))
                .toArray(PsiElement[]::new);
    }

    private static boolean isExcluded(PsiElement element, Set<Class<? extends PsiElement>> excludedElements) {
        return excludedElements.stream().anyMatch(clazz -> clazz.isInstance(element));
    }

    private static String truncateAndEscapeText(String text) {
        String truncated = text.length() > MAX_TEXT_LENGTH
            ? text.substring(0, MAX_TEXT_LENGTH) + "..."
            : text;
        return escapeHtml(truncated);
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
}