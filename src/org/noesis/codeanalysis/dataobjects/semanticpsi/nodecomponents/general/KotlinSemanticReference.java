package org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class KotlinSemanticReference {
    private final String referenceName;
    private final PsiElement psiElement;
    private final KotlinSemanticPart<?> containingPart;

    public KotlinSemanticReference(String referenceName, PsiElement psiElement, KotlinSemanticPart<?> containingPart) {
        this.referenceName = referenceName;
        this.psiElement = psiElement;
        this.containingPart = containingPart;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public KotlinSemanticPart<?> getContainingPart() {
        return containingPart;
    }

    public Map<String, String> toExplnationMap() {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("REFERENCE NAME", referenceName);
        properties.put("PSI ELEMENT TYPE", psiElement.getClass().getSimpleName());
        properties.put("CONTAINING SEMANTIC NODE NAME", containingPart.getContainingNode().getTypeName());
        properties.put("CONTAINING SEMANTIC NODE TYPE", containingPart.getContainingNode().getClass().getSimpleName());
        properties.put("CONTAINING PART TYPE", containingPart.getClass().getSimpleName());
        return properties;
    }

    @Override
    public String toString() {
        return toExplnationMap().entrySet().stream()
                .map(entry -> "\n" + entry.getKey() + "\n" + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KotlinSemanticReference that = (KotlinSemanticReference) o;
        return Objects.equals(referenceName, that.referenceName) &&
                Objects.equals(psiElement.getText(), that.psiElement.getText());
    }

    @Override
    public int hashCode() {
        String psiText = psiElement != null ? psiElement.getText() : "";
        return Objects.hash(referenceName, psiText);
    }






}