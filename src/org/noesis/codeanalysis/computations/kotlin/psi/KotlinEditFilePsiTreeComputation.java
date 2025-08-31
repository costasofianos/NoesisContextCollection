package org.noesis.codeanalysis.computations.kotlin.psi;


import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.com.intellij.openapi.project.Project;
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory;
import org.jetbrains.kotlin.com.intellij.psi.PsiManager;
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.psi.KtFile;
import org.noesis.codeanalysis.dataobjects.input.ContextCollectionInput;
import org.noesis.codeanalysis.dataobjects.returnobjects.KotlinPsiSourceFolder;
import org.noesis.codeanalysis.interfaces.Computation;
import org.noesis.codeanalysis.util.constants.KotlinContextCollectorConstants;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiTreeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KotlinEditFilePsiTreeComputation extends Computation {

    private  KtFile editFilePsiTree;
    public record EditFileResult(List<KtFile> sourceFiles, KtFile editedFile) {}
    KotlinRepoPsiTreeComputation kotlinRepoPsiTreeComputation;
    private  List<KtFile> editedPsiTree;
    private  KotlinCoreEnvironment kotlinCoreEnvironment;

    private  List<KotlinPsiSourceFolder> sourceFolders;
    private  KotlinPsiSourceFolder editFileSourceFolder;




    public KotlinEditFilePsiTreeComputation(ContextCollectionInput contextCollectionInput, KotlinRepoPsiTreeComputation kotlinRepoPsiTreeComputation) {
        super(contextCollectionInput);
        this.kotlinRepoPsiTreeComputation = kotlinRepoPsiTreeComputation;

        List<KtFile> originalPsiTree = kotlinRepoPsiTreeComputation.getOriginalPsiTree();
        PsiManager psiManager = kotlinRepoPsiTreeComputation.getPsiManager();
        Project project = kotlinRepoPsiTreeComputation.getProject();

        kotlinCoreEnvironment = kotlinRepoPsiTreeComputation.getKotlinCoreEnvironment();

        EditFileResult editFileResult = replaceEditFile(originalPsiTree, psiManager, project, 
        getContextCollectionInput().getContextCollectionInputJsonLine().path());

        editedPsiTree = editFileResult.sourceFiles();
        editFilePsiTree = editFileResult.editedFile();

        // Group files by source root
        String repoFolderPath = getContextCollectionInput().getRepoFolder().getAbsolutePath();
        sourceFolders = groupFilesBySourceRoot(editedPsiTree, repoFolderPath);
        editFileSourceFolder = findEditFileSourceFolder();

        //System.out.println("Edit File Source Folder: " + editFileSourceFolder.relativePath() + " (" + editFileSourceFolder.psiTree().size() + " files)");

//        System.out.println("Source Folders and Kt File counts (Repo Folder = "+repoFolderPath+") :");
//        sourceFolders.forEach(folder ->
//                System.out.println(folder.relativePath() + ": " + folder.psiTree().size())
//        );


    }


    private List<KotlinPsiSourceFolder> groupFilesBySourceRoot(List<KtFile> ktFiles, String repoFolderPath) {
        return ktFiles.stream()
                .collect(Collectors.groupingBy(
                        ktFile -> getRelativePath(ktFile, repoFolderPath),
                        Collectors.toList()))
                .entrySet().stream()
                .map(entry -> new KotlinPsiSourceFolder(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private String getRelativePath(KtFile ktFile, String repoFolderPath) {
        File ktFileOnDisk = KotlinPsiElementUtil.getContainingFile(ktFile);
        String fullPath = ktFileOnDisk.getAbsolutePath();
        String packagePath = ktFile.getPackageFqName().asString().replace('.', '/');

        // Remove repo path prefix
        String pathWithoutRepo = fullPath.startsWith(repoFolderPath)
                ? fullPath.substring(repoFolderPath.length()).replaceAll("^/", "")
                : fullPath;

        // Find and remove package path suffix
        if (!packagePath.isEmpty()) {
            int packageIndex = pathWithoutRepo.lastIndexOf(packagePath);
            if (packageIndex > 0) {
                String result = pathWithoutRepo.substring(0, packageIndex).replaceAll("/$", "");
                //System.out.println("Relative path for file: " + fullPath + " -> " + result);
                return result;
            }
        }

        //System.out.println("Relative path for file: " + fullPath + " -> " + pathWithoutRepo);
        return pathWithoutRepo;
    }




    private KotlinPsiSourceFolder findEditFileSourceFolder() {
        if (editFilePsiTree == null) {
            throw new RuntimeException("Edit file KtFile is null");
        }

        File editFile = KotlinPsiElementUtil.getContainingFile(editFilePsiTree);
        String editFilePath = editFile.getAbsolutePath();

        return sourceFolders.stream()
                .filter(sourceFolder -> sourceFolder.psiTree().stream()
                        .anyMatch(file -> {
                            File containingFile = KotlinPsiElementUtil.getContainingFile(file);
                            boolean matches = containingFile.getAbsolutePath().equals(editFilePath);
                            return matches;
                        }))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No matching source folder found for file: " + editFilePath));
    }



    private EditFileResult replaceEditFile(List<KtFile> sourceKtFiles, PsiManager psiManager, Project project, String editFilePathString) {
        String editText = getKotlinEditText();

        // First find the file to replace
        KtFile originalFile = sourceKtFiles.stream()
                .filter(file -> {
                    VirtualFile virtualFile = file.getVirtualFile();
                    return virtualFile != null && virtualFile.getPath().endsWith(editFilePathString);
                })
                .findFirst()
                .orElse(null);

        if (originalFile == null) {
            return new EditFileResult(sourceKtFiles, null);
        }

        VirtualFile originalVirtualFile = originalFile.getVirtualFile();
        String originalPath = originalVirtualFile.getPath();

        // Create a new LightVirtualFile with properties from original
        LightVirtualFile lightVirtualFile = new LightVirtualFile(
                originalPath,  // Use full path instead of just name
                editText
        );
        lightVirtualFile.setCharset(originalVirtualFile.getCharset());

        // Create edited file using the light virtual file
        KtFile editedFile = (KtFile) PsiFileFactory.getInstance(project)
                .createFileFromText(
                        originalPath,  // Use full path instead of just name
                        KotlinLanguage.INSTANCE,
                        lightVirtualFile.getContent(),
                        true,
                        false
                );

        // Create the new list by comparing full paths
        List<KtFile> updatedFiles = sourceKtFiles.stream()
                .map(file -> {
                    VirtualFile virtualFile = file.getVirtualFile();
                    return (virtualFile != null && virtualFile.getPath().equals(originalPath))
                            ? editedFile
                            : file;
                })
                .collect(Collectors.toList());

        return new EditFileResult(updatedFiles, editedFile);
    }


//    private EditFileResult replaceEditFile(List<KtFile> sourceKtFiles, PsiManager psiManager, Project project, String editFilePathString) {
//
////        System.out.println("Available source Kotlin files:");
////        sourceKtFiles.forEach(file -> System.out.println("- " + file.getVirtualFile().getPath()));
////        System.out.println("\nLooking for file: " + editFilePathString + "\n");
//
//        String editText = getKotlinEditText();
//
//        // First find the file to replace
//        KtFile originalFile = sourceKtFiles.stream()
//                .filter(file -> {
//                    VirtualFile virtualFile = file.getVirtualFile();
//                    return virtualFile != null && virtualFile.getPath().endsWith(editFilePathString);
//                })
//                .findFirst()
//                .orElse(null);
//
//        if (originalFile == null) {
//            //System.out.println("Could not find file to replace: " + editFilePathString);
//            return new EditFileResult(sourceKtFiles, null);
//        }
//
//        VirtualFile originalVirtualFile = originalFile.getVirtualFile();
//        String originalPath = originalVirtualFile.getPath();
//
//        // Create a new LightVirtualFile with properties from original
//        LightVirtualFile lightVirtualFile = new LightVirtualFile(
//                originalFile.getName(),
//                editText
//        );
//
//        lightVirtualFile.setCharset(originalVirtualFile.getCharset());
//
//        // Create edited file using the light virtual file
//        KtFile editedFile = (KtFile) PsiFileFactory.getInstance(project)
//                .createFileFromText(
//                        lightVirtualFile.getName(),
//                        KotlinLanguage.INSTANCE,
//                        lightVirtualFile.getContent(),
//                        true,  // physical
//                        false  // eventSystemEnabled
//                );
//
//
//        // Create the new list by comparing full paths
//        List<KtFile> updatedFiles = sourceKtFiles.stream()
//            .map(file -> {
//                VirtualFile virtualFile = file.getVirtualFile();
//                return (virtualFile != null && virtualFile.getPath().equals(originalPath))
//                    ? editedFile
//                    : file;
//            })
//            .collect(Collectors.toList());
//
//        return new EditFileResult(updatedFiles, editedFile);
//    }

    public List<PsiElement> findParentsOfMissingCode() {
        Optional<PsiElement> missingCodeElement = findDeepestMissingCodeElement();
        //System.out.println("Deepest Element [" + missingCodeElement.map(PsiElement::getText).orElse("none")+"]");
        return missingCodeElement.map(KotlinPsiTreeUtil::findParentElements)
                .orElse(new ArrayList<>()); // Fixed: return empty Map instead of ArrayList
    }

    public Optional<PsiElement> findDeepestMissingCodeElement() {
        return KotlinPsiTreeUtil.findDeepestElementWithText(editFilePsiTree, KotlinContextCollectorConstants.MISSING_CODE_COMMENT);
    }

    public String getKotlinEditText() {
        return getContextCollectionInput().getContextCollectionInputJsonLine().prefix()+"\n" + KotlinContextCollectorConstants.MISSING_CODE_COMMENT+"\n"+getContextCollectionInput().getContextCollectionInputJsonLine().suffix();
    }

    public KtFile getEditFilePsiTree() {
        return editFilePsiTree;
    }

    public List<KtFile> getSourceFolderPsiTreeContainingEditedFile() {
        return getEditFileSourceFolder().psiTree();
    }

    public KotlinCoreEnvironment getKotlinCoreEnvironment() {
        return kotlinCoreEnvironment;
    }

    public KotlinPsiSourceFolder getEditFileSourceFolder() {
        return editFileSourceFolder;
    }

    // New method to get other source folders
    public List<KotlinPsiSourceFolder> getOtherSourceFolders() {
        return sourceFolders.stream()
                .filter(folder -> !folder.equals(editFileSourceFolder))
                .collect(Collectors.toList());
    }

    @Override
    public void close() throws Exception {
        editFilePsiTree = null;
        kotlinRepoPsiTreeComputation = null;
        editedPsiTree = null;
        kotlinCoreEnvironment = null;
        sourceFolders = null;
        editFileSourceFolder = null;
    }


}