package org.noesis.codeanalysis.dataobjects.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.noesis.codeanalysis.util.html.HtmlUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.util.List;


public record ContextCollectionTargetJsonLine (
        String id,
        String repo,
        String revision,
        String path,
        List<String> modified,
        String prefix,
        String suffix,
        String archive,
        String target

) {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static ContextCollectionTargetJsonLine fromJson(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, ContextCollectionTargetJsonLine.class);
    }

    public static ContextCollectionTargetJsonLine fromJsonFile(File file) throws IOException {
        return objectMapper.readValue(file, ContextCollectionTargetJsonLine.class);
    }
    
    public String toJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }
private String formatField(String heading, Object value) {
    String capitalizedHeading = heading.substring(0, 1).toUpperCase() + heading.substring(1);
    String formattedValue = value instanceof List<?> list 
        ? String.join("\n", list.stream().map(Object::toString).toList()) 
        : String.valueOf(value);
    return capitalizedHeading + "\n" + formattedValue + "\n\n";
}

    public String prettyPrint() {
        StringBuilder result = new StringBuilder();
        for (RecordComponent component : this.getClass().getRecordComponents()) {
            try {
                Object value = component.getAccessor().invoke(this);
                result.append(formatField(component.getName(), value));
            } catch (ReflectiveOperationException e) {
                result.append(formatField(component.getName(), "Error accessing value"));
            }
        }
        return result.toString();
    }

    public String toHtml() {
        StringBuilder tableRows = new StringBuilder();

        for (RecordComponent component : this.getClass().getRecordComponents()) {
            // Add header row
            tableRows.append("<tr><th>")
                    .append(component.getName())
                    .append("</th></tr>\n");

            // Add value row
            tableRows.append("<tr><td><pre>");
            try {
                Object value = component.getAccessor().invoke(this);
                tableRows.append(value == null ? "" : HtmlUtil.escapeHtml(String.valueOf(value)));
            } catch (ReflectiveOperationException e) {
                tableRows.append("Error accessing value");
            }
            tableRows.append("</pre></td></tr>\n");
        }

        return String.format("""
            <table class='context-collection-table'>
                %s
            </table>
            """, tableRows);
    }

    public String toHeaderString() {
        return String.format("ID: %s | REPO: %s | REV: %s | PATH: %s | TARGET: %s",
                id,
                repo,
                revision,
                path,
                target
        );
    }




    public String getRepoFolderString() {
        return  repo.replace("/", "__") + "-" + revision;
    }

    public String getEditText() {
        return prefix()+"  "+suffix();
    }


    public String getFileName() {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public ContextCollectionTargetJsonLine clone() {
        return new ContextCollectionTargetJsonLine(
                this.id,
                this.repo,
                this.revision,
                this.path,
                List.copyOf(this.modified),  // Create a new immutable copy of the list
                this.prefix,
                this.suffix,
                this.archive,
                this.target

        );
    }



}