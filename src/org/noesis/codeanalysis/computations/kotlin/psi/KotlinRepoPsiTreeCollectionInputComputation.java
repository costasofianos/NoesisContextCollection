package org.noesis.codeanalysis.computations.kotlin.psi;

import org.jetbrains.kotlin.psi.KtFile;
import org.noesis.codeanalysis.dataobjects.input.ContextCollectionInput;
import org.noesis.codeanalysis.dataobjects.input.ContextCollectionInputJsonLine;
import org.noesis.codeanalysis.interfaces.Computation;

public class KotlinRepoPsiTreeCollectionInputComputation extends Computation {

    public KotlinRepoPsiTreeCollectionInputComputation(ContextCollectionInput contextCollectionInput) {
        super(contextCollectionInput);
    }

    public boolean isModified(KtFile ktFile) {
        String normalizedPath = getNormalisedPath(ktFile);
        ContextCollectionInputJsonLine jsonLine = getContextCollectionInput().getContextCollectionInputJsonLine();
        return jsonLine.modified().contains(normalizedPath);
    }

    private String getNormalisedPath(KtFile ktFile) {
        String fullPath = ktFile.getVirtualFile().getPath();
        String repoFolder = getContextCollectionInput().getRepoFolder().toString();
        String key = fullPath.startsWith(repoFolder) ?
                fullPath.substring(repoFolder.length()) :
                fullPath;
        return key.startsWith("/") ? key.substring(1) : key;
    }


    @Override
    public void close() throws Exception {

    }
}
