package org.noesis.codeanalysis.util.kotlin.psi;

import org.noesis.codeanalysis.util.html.HtmlUtil;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeNode;

import java.util.List;

public class KotlinSemanticPsiTreeCompactHtmlUtil {
    public static String generateHtml(KotlinSemanticPsiTreeNode rootNode) {
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
        generateNodeHtml(rootNode, htmlBuilder);
        htmlBuilder.append("</ul>");
        return htmlBuilder.toString();
    }

private static void generateNodeHtml(KotlinSemanticPsiTreeNode node, StringBuilder htmlBuilder) {
    List<? extends KotlinSemanticPsiTreeChildNode<?>> children = node.getChildren();
    boolean hasChildren = !children.isEmpty();

    htmlBuilder.append("<li>");
    // Always add the wrapper since we always have at least metadata
    htmlBuilder.append("<div class=\"node-wrapper collapsed\" onclick=\"toggleNode(this)\">");

    // Create node HTML with class name in bold and type name
    htmlBuilder.append(String.format("""
        <div class="node"><span class="element-type"><strong>%s</strong></span><span class="text"> %s</span></div>""",
            node.getClass().getSimpleName(),
            HtmlUtil.escapeHtml(node.getTypeName())
    ));

    htmlBuilder.append("</div>");

    htmlBuilder.append("<ul style=\"display: none;\">");
    // Add metadata nodes
    htmlBuilder.append(String.format("""
        <li><div class="node"><span class="element-type">Visibility:</span><span class="text"> %s</span></div></li>
        <li><div class="node"><span class="element-type">PublicContext:</span><span class="text"> %s</span></div></li>
        <li><div class="node"><span class="element-type">SignatureReferencedTypes:</span><span class="text"> %s</span></div></li>
        <li><div class="node"><span class="element-type">BodyReferencedTypes:</span><span class="text"> %s</span></div></li>""",
            node.getElementVisibility().toString(),
            HtmlUtil.escapeHtml(node.getPublicContext()),
            HtmlUtil.escapeHtml(String.join(", ", node.getSignatureReferencedNames())),
            HtmlUtil.escapeHtml(String.join(", ", node.getBodyReferences()))
    ));

    if (hasChildren) {
        for (KotlinSemanticPsiTreeChildNode<?> child : children) {
            generateNodeHtml(child, htmlBuilder);
        }
    }
    htmlBuilder.append("</ul>");

    htmlBuilder.append("</li>");
}

}