package org.noesis.codeanalysis.dataobjects.output;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.psi.KtFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElementsIndex {
    Map<String, List<PsiElement>> nameToElementsMap = new HashMap<>();
    Map<KtFile, List<PsiElement>> fileToElementsMap = new HashMap<>();
}
