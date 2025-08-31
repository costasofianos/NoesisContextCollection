package org.noesis.codeanalysis.dataobjects.output;

import org.noesis.codeanalysis.collectionstrategies.general.ContextCollectionStrategy;

import java.util.ArrayList;
import java.util.Map;

public record ContextEntrySetFromStrategy(
        ContextCollectionStrategy contextCollectionStrategy,
        ArrayList<ContextEntryGroup> contextEntryGroups,
        Map<String, String> explanations
) {
    public String toHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<table>\n")
                .append("<tr><th>").append(contextCollectionStrategy.getClass().getName()).append("</th></tr>\n");

        // Display file results
        html.append("<tr><th colspan=\"1\">Collection Results</th></tr>\n");

        // Display each context entry group with its header
        for (ContextEntryGroup group : contextEntryGroups) {
            // Add group header
            html.append("<tr><td class=\"group-header\">").append(group.text()).append("</td></tr>\n");

            // Display entries in the group
            html.append("<tr><td><table class=\"inner-table\">\n");
//            for (ContextEntry entry : group.contextEntries()) {
//                html.append("<tr><td class=\"file-name\">")
//                        .append(entry.sourceFile().getName())
//                        .append("</td><td class=\"context-line\"><pre>")
//                        .append(entry.text())
//                        .append("</pre></td></tr>\n");
//            }
            html.append("</table></td></tr>\n");
        }

        // Display explanations
        html.append("<tr><th colspan=\"1\">Explanations</th></tr>\n");
        for (Map.Entry<String, String> entry : explanations.entrySet()) {
            html.append("<tr><td><table class=\"inner-table\">\n")
                    .append("<tr><td class=\"explanation-key\">")
                    .append(entry.getKey())
                    .append("</td></tr>\n")
                    .append("<tr><td class=\"explanation-value\"><pre>")
                    .append(entry.getValue())
                    .append("</pre></td></tr>\n")
                    .append("</table></td></tr>\n");
        }

        html.append("</table>");
        return html.toString();
    }

}