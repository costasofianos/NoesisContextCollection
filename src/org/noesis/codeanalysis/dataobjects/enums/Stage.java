package org.noesis.codeanalysis.dataobjects.enums;

public enum Stage {
    LocalStage("local"),
    PracticeStage("practice"),
    PublicStage("public"),
    PrivateStage("private");


    private final String value;

    Stage(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Stage fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Language value cannot be null");
        }
        try {
            for (Stage stage : Stage.values()) {
                if (stage.getValue().equalsIgnoreCase(value)) {
                    return stage;
                }
            }
            throw new IllegalArgumentException("Invalid stage value: " + value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid stage value: " + value);
        }
    }
}