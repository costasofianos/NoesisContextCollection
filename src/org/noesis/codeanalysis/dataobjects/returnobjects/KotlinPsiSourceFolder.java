package org.noesis.codeanalysis.dataobjects.returnobjects;

import org.jetbrains.kotlin.psi.KtFile;
import java.util.List;
import java.util.Objects;

public record KotlinPsiSourceFolder(String relativePath, List<KtFile> psiTree) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KotlinPsiSourceFolder that = (KotlinPsiSourceFolder) o;
        return Objects.equals(relativePath, that.relativePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relativePath);
    }
}

