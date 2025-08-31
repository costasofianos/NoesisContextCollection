package org.noesis.codeanalysis.dataobjects.output;

import org.jetbrains.annotations.NotNull;

public record ContextEntryWeight(double value) {

    public ContextEntryWeight {
        if (Double.isNaN(value) || value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("Value must be between 0.0 and 1.0 inclusive.");
        }
    }

    public static ContextEntryWeight of(double value) {
        return new ContextEntryWeight(value);
    }

    @NotNull
    @Override
    public String toString() {
        return String.format("%.4f", value);
    }

}
