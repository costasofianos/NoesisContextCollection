package org.noesis.codeanalysis.util.kotlin.psi;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement;
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl;

import java.util.Arrays;
import java.util.Set;

public class KotlinPsiTreeCompactHtmlUtil {
    private static final int MAX_TEXT_LENGTH = 50000;

    private static final Set<Class<? extends PsiElement>> DEFAULT_EXCLUDED_ELEMENTS = Set.of(
            //KtDotQualifiedExpression.class
            PsiWhiteSpaceImpl.class
    );

    public static String generatePsiTreeHtml(PsiElement rootElement) {
        return generatePsiTreeHtml(rootElement, DEFAULT_EXCLUDED_ELEMENTS);
    }

    public static String generatePsiTreeHtml(PsiElement rootElement, Set<Class<? extends PsiElement>> excludedElements) {
        StringBuilder htmlBuilder = new StringBuilder();
        // Add required JavaScript
        htmlBuilder.append("""
            <script>
            function toggleNode(element) {
                const ul = element.parentElement.querySelector('ul');
                if (ul) {
                    ul.style.display = ul.style.display === 'none' ? 'block' : 'none';
                    element.classList.toggle('collapsed');
                }
            }
            </script>
        """);
        htmlBuilder.append("<ul class=\"psi-tree\">");
        generateHtml(rootElement, htmlBuilder, excludedElements, null);
        htmlBuilder.append("</ul>");
        return htmlBuilder.toString();
    }

    private static void generateHtml(PsiElement element, StringBuilder htmlBuilder, 
                                   Set<Class<? extends PsiElement>> excludedElements, 
                                   String parentText) {
        String currentText = element.getText();
        boolean hideParentText = Arrays.stream(getFilteredChildren(element, excludedElements))
                                     .anyMatch(child -> child.getText().equals(currentText));
        
        PsiElement[] children = getFilteredChildren(element, excludedElements);
        boolean hasChildren = children.length > 0;
        
        htmlBuilder.append("<li>");
        if (hasChildren) {
            htmlBuilder.append("<div class=\"node-wrapper\" onclick=\"toggleNode(this)\">");
        }
        htmlBuilder.append(createNodeHtml(element, !hideParentText));
        if (hasChildren) {
            htmlBuilder.append("</div>");
        }

        if (hasChildren) {
            htmlBuilder.append("<ul>");
            for (PsiElement child : children) {
                generateHtml(child, htmlBuilder, excludedElements, currentText);
            }
            htmlBuilder.append("</ul>");
        }

        htmlBuilder.append("</li>");
    }

    private static String createNodeHtml(PsiElement element, boolean showText) {
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
        <div class="node%s"><span class="element-type">%s</span>%s%s</div>""",
        errorClass,
        elementType + (showText ? " " : ""),  // Added space after element type when there's text
        showText ? String.format("<span class=\"text\">%s</span>", text) : "",
        isError ? String.format("<span class=\"error-message\">%s</span>", 
                              escapeHtml(((PsiErrorElement)element).getErrorDescription())) : "");
    }

    /**
     * Retrieves filtered child elements from the given PSI element, including comments.
     * Uses {@code getNode().getChildren(null)} instead of {@code getChildren()} to access
     * the underlying AST (Abstract Syntax Tree) nodes, which ensures comments are included
     * in the result. Regular {@code getChildren()} does not return comment nodes.
     *
     * @param element The PSI element whose children should be retrieved
     * @param excludedElements Set of element types to exclude from the result
     * @return Array of filtered PSI elements including comments
     */

    private static PsiElement[] getFilteredChildren(PsiElement element, Set<Class<? extends PsiElement>> excludedElements) {
        return Arrays.stream(element.getNode().getChildren(null))  // get all nodes including comments
                .map(node -> node.getPsi())
                .filter(child -> !isExcluded(child, excludedElements))
                .toArray(PsiElement[]::new);
    }


    private static boolean isExcluded(PsiElement element, Set<Class<? extends PsiElement>> excludedElements) {

        // Make sure we're not excluding any comment types
        if (element.getClass().getSimpleName().contains("Comment")) {
            return false;
        }


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