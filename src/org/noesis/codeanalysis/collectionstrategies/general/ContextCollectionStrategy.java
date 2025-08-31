package org.noesis.codeanalysis.collectionstrategies.general;

import org.noesis.codeanalysis.computations.general.cache.ComputationFactory;
import org.noesis.codeanalysis.control.ContextCollector;
import org.noesis.codeanalysis.dataobjects.output.ContextEntry;
import org.noesis.codeanalysis.dataobjects.output.ContextEntryGroup;
import org.noesis.codeanalysis.dataobjects.output.ContextEntrySetFromStrategy;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementContextEntryUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class ContextCollectionStrategy implements AutoCloseable {

    private final ContextCollector contextCollector;
    private final ComputationFactory computationFactory;
    private ArrayList<ContextEntryGroup> contextEntryGroups;
    private Map<String, String> explanations;

    public ContextCollectionStrategy(ContextCollector contextCollector, ComputationFactory computationFactory) {
        this.contextCollector = contextCollector;
        this.computationFactory = computationFactory;
    }

    public ContextEntrySetFromStrategy collectContext() {
        contextEntryGroups = new ArrayList<>();
        explanations = new LinkedHashMap<>();
        populateCollectionStrategyResults();
        

        KotlinPsiElementContextEntryUtil.createContextEntryGroupsHtmlTable(contextEntryGroups);
        
        return new ContextEntrySetFromStrategy(this, contextEntryGroups, explanations);
    }

    public abstract void populateCollectionStrategyResults();


    public ComputationFactory getComputationCache() {
        return computationFactory;
    }

    public ContextCollector getContextCollector() {
        return contextCollector;
    }

    public void addContextEntryGroup(ContextEntryGroup group) {
        if (contextEntryGroups == null) {
            contextEntryGroups = new ArrayList<>();
        }
        contextEntryGroups.add(group);
    }

    public void addContextEntryInNewGroup(String text, List<ContextEntry> contextEntries) {
        if (contextEntryGroups == null) {
            contextEntryGroups = new ArrayList<>();
        }
        contextEntryGroups.add(new ContextEntryGroup(text, contextEntries));
    }

    public void addContextEntryGroups(List<ContextEntryGroup> groups) {
        if (groups == null) {
            return;
        }

        if (this.contextEntryGroups == null) {
            this.contextEntryGroups = new ArrayList<>();
        }
        this.contextEntryGroups.addAll(groups);
    }

    public void addExplanation(String key, String explanation) {
        if (explanations == null) {
            explanations = new LinkedHashMap<>();
        }
        explanations.put(key, explanation);
    }

    public void addExplanations(Map<String, String> newExplanations) {
        if (newExplanations != null) {
            explanations.putAll(newExplanations);
        }
    }

    public ComputationFactory getComputationFactory() {
        return computationFactory;
    }

    public int getTotalContextEntriesCount() {
        return getContextEntryGroups().stream()
                .mapToInt(group -> group.contextEntries().size())
                .sum();
    }
    
    

    public ArrayList<ContextEntryGroup> getContextEntryGroups() {
        return contextEntryGroups;
    }

    public double getMaxWeight() {
        return getContextEntryGroups().stream()
                .flatMap(group -> group.contextEntries().stream())
                .map(entry -> entry.contextEntryWeight().value())
                .max(Double::compare)
                .orElse(0.0);
    }

}