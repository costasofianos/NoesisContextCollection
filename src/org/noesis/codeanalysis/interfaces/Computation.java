package org.noesis.codeanalysis.interfaces;

import org.noesis.codeanalysis.dataobjects.input.ContextCollectionInput;

public abstract class Computation implements AutoCloseable{

    ContextCollectionInput contextCollectionInput;

    public Computation(ContextCollectionInput contextCollectionInput) {
        this.contextCollectionInput = contextCollectionInput;
    }

    public ContextCollectionInput getContextCollectionInput() {
        return contextCollectionInput;
    }



}