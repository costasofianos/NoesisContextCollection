package org.noesis.codeanalysis.util.html;

public class HtmlResources {

    public static String wrapInHtmlPage(String content) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Context Collection</title>
                    <style>
                %s
                    </style>
                </head>
                <body>
                %s
                </body>
                </html>
                """.formatted(getAllStyles(), content);
    }


    public static String getAllStyles() {
        return getTableStyle() + getTreeStyle();
    }

    public static String getTableStyle() {
        return """
                table {
                    border-collapse: separate;
                    border-spacing: 0;
                    border-radius: 8px;
                    overflow: hidden;
                    width: 100%;
                    margin: 20px 0;
                    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
                    border: 1px solid #e2e8f0;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                }
                
                th {
                    background-color: #f8fafc;
                    color: #1a202c;
                    font-weight: 600;
                    padding: 12px 16px;
                    text-align: left;
                    border-bottom: 2px solid #e2e8f0;
                }
                
                td {
                    padding: 12px 16px;
                    border-bottom: 1px solid #e2e8f0;
                    color: #4a5568;
                }
                
                tr:last-child td {
                    border-bottom: none;
                }
                
                pre {
                    background-color: #f7fafc;
                    padding: 12px;
                    border-radius: 6px;
                    margin: 0;
                    font-family: Monaco, "Courier New", monospace;
                    font-size: 14px;
                    white-space: pre-wrap;
                    word-wrap: break-word;
                }
                """;
    }

    private static String getTreeStyle() {
        return """
                    .tree {
                        font-family: monospace;
                    }
                    .tree ul {
                        list-style-type: none;
                        padding-left: 20px;
                    }
                    .node {
                        padding: 2px;
                        border-radius: 3px;
                        margin: 2px 0;
                    }
                    .node:hover {
                        background-color: #f0f0f0;
                    }
                    .element-type {
                        color: #2196F3;
                        font-weight: bold;
                    }
                    .text {
                        color: #666;
                    }
                    .node.error {
                        background-color: #ffebee;
                        border: 1px solid #ef5350;
                    }
                
                    .error-message {
                        color: #d32f2f;
                        font-style: italic;
                        display: block;
                        margin-top: 4px;
                    }
                
                """;

    }
}