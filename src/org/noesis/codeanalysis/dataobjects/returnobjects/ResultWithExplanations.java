package org.noesis.codeanalysis.dataobjects.returnobjects;

import java.util.HashMap;

public class ResultWithExplanations<T> {
    private final HashMap<String, String> explanations;
    private final T result;

    public ResultWithExplanations(T result) {
        this.result = result;
        this.explanations = new HashMap<>();
    }

    public ResultWithExplanations(T result, HashMap<String, String> explanations) {
        this.result = result;
        this.explanations = explanations != null ? explanations : new HashMap<>();
    }

    public HashMap<String, String> getExplanations() {
        return explanations;
    }

    public T getResult() {
        return result;
    }

    public void addExplanation(String key, String explanation) {
        explanations.put(key, explanation);
    }



}