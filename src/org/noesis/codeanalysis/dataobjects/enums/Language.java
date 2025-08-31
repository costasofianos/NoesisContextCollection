package org.noesis.codeanalysis.dataobjects.enums;

public enum Language {
    python,
    kotlin;

    public static Language fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Language value cannot be null");
        }
        try {
            return Language.valueOf(value.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid language value: " + value);
        }
    }
}
