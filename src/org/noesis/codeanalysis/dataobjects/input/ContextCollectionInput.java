package org.noesis.codeanalysis.dataobjects.input;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.File;

public class ContextCollectionInput {

    private final ContextCollectionInputJsonLine contextCollectionInputJsonLine;

    private BatchContextCollectionInput batchContextCollectionInput;

    private String target;

    public ContextCollectionInput(BatchContextCollectionInput batchContextCollectionInput, ContextCollectionInputJsonLine contextCollectionInputJsonLine) throws JsonProcessingException {
        this.batchContextCollectionInput = batchContextCollectionInput;
        this.contextCollectionInputJsonLine = contextCollectionInputJsonLine;
    }

    public ContextCollectionInput(BatchContextCollectionInput batchContextCollectionInput, String jsonLine) throws JsonProcessingException {

        if (jsonLine == null) {
            throw new IllegalArgumentException("jsonLine cannot be null");
        }

        this.batchContextCollectionInput = batchContextCollectionInput;
        this.contextCollectionInputJsonLine = ContextCollectionInputJsonLine.fromJson(jsonLine);
        //System.out.println("[ContextCollectionInput] " +toHeaderString());
    }

    @Override
    public String toString() {
        return "ContextCollectionInput{" +
                "contextCollectionInputJsonLine=" + contextCollectionInputJsonLine +
                ", dataRepositoryFolderRoot=" + batchContextCollectionInput.getDataFolder() +
                ", repoFolder=" + getRepoFolder() +
                '}';
    }


    public String toHtml() {
        return "<table>" +
                "<tr>" +
                "<th>Data Repository Folder Root</th>" +
                "<th>Repo Folder</th>" +
                "</tr>" +
                "<tr>" +
                "<td>" + batchContextCollectionInput.getDataFolder() + "</td>" +
                "<td>" + getRepoFolder() + "</td>" +
                "</tr>" +
                "<tr>" +
                "<td colspan='2'><pre>" + contextCollectionInputJsonLine.toHtml() + "</pre></td>" +
                "</tr>" +
                "</table>";
    }



    public ContextCollectionInputJsonLine getContextCollectionInputJsonLine() {
        return contextCollectionInputJsonLine;
    }



    public File getRepoFolder() {
        return new File(batchContextCollectionInput.getRepositoriesFolder(), contextCollectionInputJsonLine.getRepoFolderString());
    }

    public String toHeaderString() {

        return "REPO FOLDER = "+getRepoFolder().toString()+"\n"+getContextCollectionInputJsonLine().toHeaderString()+"\n\n";
    }

    public BatchContextCollectionInput getContextCollectionInputBatch() {
        return batchContextCollectionInput;
    }
    public String getCollectionInputSummary() {
        return "REPO = "+getContextCollectionInputJsonLine().repo() + " | REVISION = "+getContextCollectionInputJsonLine().revision()+", PATH = "+getContextCollectionInputJsonLine().path();
    }

    public ContextCollectionInput clone()  {
        try {
            ContextCollectionInput clonedContextCollectionInput = new ContextCollectionInput(
                    this.batchContextCollectionInput.clone(),
                    this.contextCollectionInputJsonLine.clone()
            );
            clonedContextCollectionInput.setTarget(this.target);
            return clonedContextCollectionInput;

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to clone ContextCollectionInput", e);
        }
    }

    public void setTarget(String target) {
        //System.out.println("Setting Target to "+target);
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
