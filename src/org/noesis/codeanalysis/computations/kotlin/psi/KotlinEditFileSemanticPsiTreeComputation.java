package org.noesis.codeanalysis.computations.kotlin.psi;

import org.jetbrains.kotlin.psi.KtFile;
import org.noesis.codeanalysis.dataobjects.input.ContextCollectionInput;
import org.noesis.codeanalysis.dataobjects.returnobjects.KotlinPsiSourceFolder;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeRoot;
import org.noesis.codeanalysis.interfaces.Computation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KotlinEditFileSemanticPsiTreeComputation extends Computation {

    private KotlinSemanticPsiTreeRoot kotlinSemanticPsiTreeRootContainingEditFile;
    private Map<String, KotlinSemanticPsiTreeRoot> otherSourceFolderRoots;

    public KotlinEditFileSemanticPsiTreeComputation(ContextCollectionInput contextCollectionInput, 
                                                   KotlinEditFilePsiTreeComputation kotlinEditFilePsiTreeComputation) {
        super(contextCollectionInput);
        
        // Create semantic tree for the source folder containing edited file
        List<KtFile> psiTreeContainingEditedFile = kotlinEditFilePsiTreeComputation.getSourceFolderPsiTreeContainingEditedFile();
        //System.out.println("Found " + psiTreeContainingEditedFile.size() + " files in the edited file source folder");
        kotlinSemanticPsiTreeRootContainingEditFile = new KotlinSemanticPsiTreeRoot(psiTreeContainingEditedFile);

        // Create semantic trees for other source folders
        otherSourceFolderRoots = new HashMap<>();
        List<KotlinPsiSourceFolder> otherSourceFolders = kotlinEditFilePsiTreeComputation.getOtherSourceFolders();
        
//        for (KotlinPsiSourceFolder sourceFolder : otherSourceFolders) {
//            String relativePath = sourceFolder.relativePath();
//            List<KtFile> folderFiles = sourceFolder.psiTree();
//            KotlinSemanticPsiTreeRoot root = new KotlinSemanticPsiTreeRoot(folderFiles);
//            otherSourceFolderRoots.put(relativePath, root);
//
////            System.out.println("Created semantic tree for source folder '" + relativePath +
////                             "' containing " + folderFiles.size() + " files");
//        }
    }

    public KotlinSemanticPsiTreeRoot getKotlinSemanticPsiTreeRootContainingEditFile() {
        return kotlinSemanticPsiTreeRootContainingEditFile;
    }

    public KotlinSemanticPsiTreeRoot getKotlinSemanticPsiTreeRoot() {
        return kotlinSemanticPsiTreeRootContainingEditFile;
    }

    public Map<String, KotlinSemanticPsiTreeRoot> getOtherSourceFolderRoots() {
        return otherSourceFolderRoots;
    }

    @Override
    public void close() throws Exception {
        kotlinSemanticPsiTreeRootContainingEditFile = null;
        otherSourceFolderRoots = null;
    }
}