package org.noesis.codeanalysis.util.kotlin.psi;

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.com.intellij.openapi.project.Project;
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer;
import org.jetbrains.kotlin.com.intellij.psi.PsiManager;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtPsiFactory;
import org.noesis.codeanalysis.dataobjects.returnobjects.KotlinPsiExtract;

import java.util.ArrayList;

public class KotlinPsiExtractor {

    public static KotlinPsiExtract getPsiTree(KotlinCoreEnvironment kotlinCoreEnvironment) {

        // 3. Get the PSI manager and source files
        Project project = kotlinCoreEnvironment.getProject();
        PsiManager psiManager = PsiManager.getInstance(project);

        return new KotlinPsiExtract(new ArrayList<>(kotlinCoreEnvironment.getSourceFiles()), psiManager,  project);

    }


    /**
     * Parse Kotlin source files from a directory into AST representation
     * @param sourceDir Directory containing Kotlin source files
     * @return List of KtFile objects representing the AST
     */
//    public static List<KtFile> parseDirectory(File sourceDir, KotlinCoreEnvironment kotlinCoreEnvironment) {
//        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
//            throw new IllegalArgumentException("Source directory does not exist or is not a directory");
//        }
//
//
//        // Get PSI manager
//        PsiManager psiManager = PsiManager.getInstance(kotlinCoreEnvironment.getProject());
//
//        // List to store parsed files
//        List<KtFile> ktFiles = new ArrayList<>();
//
//        // Process all Kotlin files in the directory
//        return processDirectory(sourceDir, psiManager);
//    }
//
//    private static List<KtFile> processDirectory(File directory, PsiManager psiManager) {
//       // System.out.println("########Processing Directory : " + directory.getPath());
//
//        List<KtFile> ktFiles = new ArrayList<>();
//
//        File[] files = directory.listFiles();
//        if (files == null) return ktFiles;
//
//        for (File file : files) {
//            if (file.isDirectory()) {
//                // Add results from subdirectory instead of returning them
//                ktFiles.addAll(processDirectory(file, psiManager));
//            } else if (file.getName().endsWith(".kt")) {
//                //System.out.println("########Parsing file: " + file.getAbsolutePath());
//                KtFile ktFile = (KtFile) psiManager.findFile(
//                        new org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalFileSystem()
//                                .findFileByIoFile(file));
//                if (ktFile != null) {
//                    ktFiles.add(ktFile);
//                }
//            }
//        }
//
//        return ktFiles;
//    }


    /**
     * Parse Kotlin source code string into AST representation
     * @param sourceCode String containing Kotlin source code
     * @param fileName Name to use for the virtual file (e.g., "Example.kt")
     * @return KtFile object representing the AST
     */

    public static KtFile parseString(String sourceCode, String fileName) {
        // Create compiler configuration
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.Companion.getNONE());

        // Create Kotlin environment
        KotlinCoreEnvironment environment = KotlinCoreEnvironment.createForProduction(
                Disposer.newDisposable(),
                configuration,
                EnvironmentConfigFiles.JVM_CONFIG_FILES
        );


        // Create KtFile from the virtual file
        return new KtPsiFactory(environment.getProject())
                .createFile(fileName, sourceCode);

    }


}