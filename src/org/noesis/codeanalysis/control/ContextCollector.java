package org.noesis.codeanalysis.control;

import org.noesis.codeanalysis.collectionstrategies.general.ContextCollectionStrategy;
import org.noesis.codeanalysis.computations.general.cache.ComputationFactory;
import org.noesis.codeanalysis.dataobjects.input.BatchContextCollectionInput;
import org.noesis.codeanalysis.dataobjects.input.ContextCollectionInput;
import org.noesis.codeanalysis.dataobjects.output.CollectedContext;
import org.noesis.codeanalysis.dataobjects.output.ContextEntrySetFromStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ContextCollector implements AutoCloseable {



    //    private final ComputationCache computationCache;
    private ContextCollectionInput contextCollectionInput;
    private List<List<ContextCollectionStrategy>> contextCollectionStrategies;
    ComputationFactory computationFactory;
    boolean submitLocally;
    BatchContextCollectionInput batchContextCollectionInput;


    public ContextCollector(ContextCollectionInput contextCollectionInput,
                            List<List<Class<? extends ContextCollectionStrategy>>> contextCollectionStrategyClasses,
                            BatchContextCollectionInput batchContextCollectionInput,
                            boolean submitLocally
                            ) {
        this.submitLocally = submitLocally;
        if (contextCollectionInput == null) {
            throw new IllegalArgumentException("ContextCollectionInput cannot be null or empty");
        }
        if (contextCollectionStrategyClasses == null) {
            throw new IllegalArgumentException("Strategy class cannot be null");
        }

        this.contextCollectionInput = contextCollectionInput;
        computationFactory = new ComputationFactory(getContextCollectionInput());
        this.contextCollectionStrategies = contextCollectionStrategyClasses.stream()
                .map(strategyClassList -> createStrategies(this, strategyClassList, computationFactory))
                .toList();
    }

    public CollectedContext collectResults() {

        for (List<ContextCollectionStrategy> strategies : contextCollectionStrategies) {
            try {
                List<ContextEntrySetFromStrategy> contextEntriesFromStrategies = new ArrayList<ContextEntrySetFromStrategy>();

                strategies
                        .forEach(contextCollectionStrategy -> contextEntriesFromStrategies.add(contextCollectionStrategy.collectContext())
                        );

                CollectedContext collectedContext = new CollectedContext(contextEntriesFromStrategies, this, batchContextCollectionInput, submitLocally);

//                if (new Random().nextInt(3) == 0) {
//                    throw new RuntimeException("Random failure (20% chance)");
//                }

                boolean hasAnyEntries = contextEntriesFromStrategies.stream()
                        .filter(Objects::nonNull)
                        .map(ContextEntrySetFromStrategy::contextEntryGroups) // replace with your accessor if different
                        .filter(Objects::nonNull)
                        .flatMap(List::stream)
                        .filter(Objects::nonNull)
                        .anyMatch(group ->
                                group.contextEntries() != null              // replace with your accessor if different
                                        && !group.contextEntries().isEmpty()
                        );


                if (hasAnyEntries) {
                    return collectedContext;
                }
                System.out.println("Collection Results from Strategy List " +
                        strategies.stream()
                                .map(s -> s == null ? "<null>" : s.getClass().getSimpleName())
                                .collect(java.util.stream.Collectors.joining(", ", "[", "]")) +
                        " was empty, trying next Strategy List");
            } catch (Exception e) {
                System.out.println("[WARNING] Exception found during context collection. No context found");
                e.printStackTrace();
            }
        }

        CollectedContext collectedContext = new CollectedContext(new ArrayList<ContextEntrySetFromStrategy>(), this, batchContextCollectionInput, submitLocally);
        return collectedContext;
    }


    private List<ContextCollectionStrategy> createStrategies(
            ContextCollector contextCollector,
            List<Class<? extends ContextCollectionStrategy>> contextCollectionStrategyClasses,
            ComputationFactory computationFactory) {
        List<ContextCollectionStrategy> strategyList = new ArrayList<>();

        for (Class<? extends ContextCollectionStrategy> contextCollectionStrategyClass : contextCollectionStrategyClasses) {
            try {
                ContextCollectionStrategy strategy = contextCollectionStrategyClass
                        .getDeclaredConstructor(ContextCollector.class, ComputationFactory.class)
                        .newInstance(contextCollector, computationFactory);
                strategyList.add(strategy);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                        "Failed to create strategy instance for input: " + contextCollectionInput.toHeaderString(), e);
            }
        }

        return Collections.unmodifiableList(strategyList);
    }

//    public List<ContextCollectionStrategy> getContextCollectionStrategies() {
//        return contextCollectionStrategies;
//    }

    public ContextCollectionInput getContextCollectionInput() {
        return contextCollectionInput;
    }

    public ComputationFactory getComputationFactory() {
        return computationFactory;
    }

    public void setContextCollectionInput(ContextCollectionInput contextCollectionInput) {
        this.contextCollectionInput = contextCollectionInput;
    }

    @Override
    public void close() throws Exception {
        getComputationFactory().close();
        computationFactory = null;
        contextCollectionStrategies = null;
        contextCollectionInput = null;
    }
}