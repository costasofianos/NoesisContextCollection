package org.noesis.codeanalysis.util.html;

import java.util.*;
import java.util.stream.Collectors;

public class HtmlUtil {

    public static String generateStringListHtmlTable(Collection<String> strings) {
        StringBuilder html = new StringBuilder();
        html.append("<table border=\"1\">")
                .append("<tr>")
                .append("<th>Text</th>")
                .append("</tr>");

        strings
                //.sorted()
                .forEach(text -> {
                    html.append("<tr>")
                            .append("<td>").append(HtmlUtil.escapeHtml(text)).append("</td>")
                            .append("</tr>");
                });

        html.append("</table>");
        return html.toString();
    }

    public static String generateStringListsMapHtmlTable(Map<String, List<String>> map) {
        StringBuilder html = new StringBuilder();
        html.append("<table border=\"1\">")
                .append("<tr>")
                .append("<th>File</th>")
                .append("<th>Methods</th>")
                .append("</tr>");

        map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    html.append("<tr>")
                            .append("<td>").append(HtmlUtil.escapeHtml(entry.getKey())).append("</td>")
                            .append("<td>")
                            .append(entry.getValue().stream()
                                    .sorted()
                                    .map(HtmlUtil::escapeHtml)
                                    .collect(Collectors.joining("<br>")))
                            .append("</td>")
                            .append("</tr>");
                });

        html.append("</table>");
        return html.toString();
    }

    public static String generateSimpleHtmlTable(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "<p>No data available</p>";
        }

        StringBuilder html = new StringBuilder();
        html.append("<table border='1' style='border-collapse: collapse; width: 100%;'>\n");

        // Header row
        html.append("<tr style='background-color: #f2f2f2;'>\n")
                .append("<th style='padding: 8px; text-align: left;'>Key</th>\n")
                .append("<th style='padding: 8px; text-align: left;'>Value</th>\n")
                .append("</tr>\n");

        // Data rows
        map.forEach((key, value) -> {
            html.append("<tr>\n")
                    .append(String.format("<td style='padding: 8px;'>%s</td>\n", escapeHtml(key)))
                    .append(String.format("<td style='padding: 8px;'>%s</td>\n", escapeHtml(value)))
                    .append("</tr>\n");
        });

        html.append("</table>");
        return html.toString();
    }


    /**
     * Generates an HTML table from a map where values are lists of maps containing multiple columns of data.
     * @param map Map with type names as keys and lists of detail maps as values
     * @return Formatted HTML table string
     */
    public static String generateMapWithMultipleColumnsHtmlTable(Map<String, List<Map<String, String>>> map) {
        if (map == null || map.isEmpty()) {
            return "<p>No data available</p>";
        }

        StringBuilder html = new StringBuilder();
        html.append("<table border='1' style='border-collapse: collapse; width: 100%;'>\n");

        // Get column headers from the first entry's map keys (assuming all maps have same structure)
        Set<String> columnHeaders = map.values().stream()
                .flatMap(List::stream)
                .findFirst()
                .map(Map::keySet)
                .orElse(Collections.emptySet());

        // Generate header row
        html.append("<tr style='background-color: #f2f2f2;'>\n");
        html.append("<th style='padding: 8px; text-align: left;'>Type</th>\n");
        for (String header : columnHeaders) {
            html.append(String.format("<th style='padding: 8px; text-align: left;'>%s</th>\n", header));
        }
        html.append("</tr>\n");

        // Generate data rows
        map.forEach((type, detailsList) -> {
            if (!detailsList.isEmpty()) {
                // First row of each type includes rowspan
                html.append("<tr>\n");
                html.append(String.format("<td rowspan='%d' style='padding: 8px; vertical-align: top;'>%s</td>\n",
                        detailsList.size(), escapeHtml(type)));
                appendDetailCells(html, detailsList.get(0), columnHeaders);
                html.append("</tr>\n");

                // Remaining rows for this type
                for (int i = 1; i < detailsList.size(); i++) {
                    html.append("<tr>\n");
                    appendDetailCells(html, detailsList.get(i), columnHeaders);
                    html.append("</tr>\n");
                }
            }
        });

        html.append("</table>");
        return html.toString();
    }

    private static void appendDetailCells(StringBuilder html, Map<String, String> details, Set<String> columnHeaders) {
        for (String header : columnHeaders) {
            String value = details.getOrDefault(header, "");
            html.append(String.format("<td style='padding: 8px;'>%s</td>\n", escapeHtml(value)));
        }
    }





    public static String escapeHtml(String text) {

        if (text == null) {
            return "null";
        }

        return text .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }



}
