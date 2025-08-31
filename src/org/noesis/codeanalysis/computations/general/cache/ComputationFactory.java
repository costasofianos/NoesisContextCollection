package org.noesis.codeanalysis.computations.general.cache;

import org.noesis.codeanalysis.dataobjects.input.ContextCollectionInput;
import org.noesis.codeanalysis.interfaces.Computation;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComputationFactory implements AutoCloseable {

    ContextCollectionInput contextCollectionInput;

    public ComputationFactory(ContextCollectionInput contextCollectionInput) {
        this.contextCollectionInput = contextCollectionInput;
    }


    private List<Computation> computations = new ArrayList<Computation>();

    public List<Computation> getComputations() {
        return computations;
    }

    public void setComputations(List<Computation> computations) {
        this.computations = computations;
    }


    @SuppressWarnings("unchecked")
    public <T extends Computation> T getInstance(Class computationClass) {
        //System.out.println("Getting Instance of " + computationClass.getSimpleName());

        Optional<Computation> existingComputation = getExistingComputation(computationClass);
        if (existingComputation.isPresent()) {
            return (T) existingComputation.get();
        }

        try {
            Constructor<?>[] constructors = computationClass.getDeclaredConstructors();

            if (constructors.length != 1) {
                throw new IllegalStateException(
                        "Computation classes must have exactly one constructor: " +
                                computationClass.getSimpleName()
                );
            }

            Constructor<?> constructor = constructors[0];
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            
            if (parameterTypes.length == 0) {
                T computation = (T) constructor.newInstance();
                return computation;
            }

            // Analyze and store parameter types
            List<Class<?>> constructorParamTypes = new ArrayList<>();
            boolean hasContextCollectionInput = false;

            for (Class<?> paramType : parameterTypes) {
                if (ContextCollectionInput.class.isAssignableFrom(paramType)) {
                    if (hasContextCollectionInput) {
                        throw new IllegalStateException(
                                "Multiple ContextCollectionInput parameters are not allowed in class: " +
                                        computationClass.getSimpleName()
                        );
                    }
                    hasContextCollectionInput = true;
                    constructorParamTypes.add(paramType);
                } else if (Computation.class.isAssignableFrom(paramType)) {
                    constructorParamTypes.add(paramType);
                } else {
                    throw new IllegalStateException(
                            "Parameter type " + paramType.getSimpleName() +
                                    " must be either ContextCollectionInput or extend Computation in class: " +
                                    computationClass.getSimpleName()
                    );
                }
            }

           // System.out.println("["+computationClass.getName()+"] Constructor has " +
           //         constructorParamTypes.size() + " parameters" +
           //         (hasContextCollectionInput ? " (including ContextCollectionInput)" : ""));
//            constructorParamTypes.forEach(type ->
//                    System.out.println("["+computationClass.getName()+"] Parameter type: " +
//                            type.getSimpleName() +
//                            (ContextCollectionInput.class.isAssignableFrom(type) ? " (ContextCollectionInput)" : " (Computation)"))
//            );

            // Create constructor arguments based on parameter types
            Object[] constructorArgs = new Object[constructorParamTypes.size()];
            for (int i = 0; i < constructorParamTypes.size(); i++) {
                Class<?> paramType = constructorParamTypes.get(i);
                if (ContextCollectionInput.class.isAssignableFrom(paramType)) {
                    constructorArgs[i] = contextCollectionInput;
                } else {
                    Computation computationParameter = getInstance((Class<? extends Computation>) paramType);
                    addComputation(computationParameter);
                    constructorArgs[i] = computationParameter;
                }
            }

            constructor.setAccessible(true);
            return (T) constructor.newInstance(constructorArgs);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of Computation ["+computationClass.getName()+"]", e);
        }
    }


    private <T extends Computation> Optional<T> getExistingComputation(Class<T> computationClass) {
        return computations.stream()
                .filter(computation -> computation.getClass().equals(computationClass))
                .map(computation -> (T) computation)
                .findFirst();
    }

    public void addComputation(Computation computation) {
        if (computation != null) {
            // Add to computations list if not already present
            if (!computations.contains(computation)) {
                computations.add(computation);
            }
        }
    }

    public ContextCollectionInput getContextCollectionInput() {
        return contextCollectionInput;
    }


    @Override
    public void close() throws Exception {
        if (computations != null) {
            for (Computation computation : computations) {
                if (computation != null) {
                    computation.close();
                }
            }
            computations.clear();
            computations = null;
        }
    }

}