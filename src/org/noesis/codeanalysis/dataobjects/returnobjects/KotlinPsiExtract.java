package org.noesis.codeanalysis.dataobjects.returnobjects;

import org.jetbrains.kotlin.com.intellij.openapi.project.Project;
import org.jetbrains.kotlin.com.intellij.psi.PsiManager;
import org.jetbrains.kotlin.psi.KtFile;

import java.util.List;

public record KotlinPsiExtract(List<KtFile> ktFiles, PsiManager psiManager, Project project) {
}
