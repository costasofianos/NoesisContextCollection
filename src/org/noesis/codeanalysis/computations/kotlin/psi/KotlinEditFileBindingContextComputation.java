package org.noesis.codeanalysis.computations.kotlin.psi;

import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.cli.jvm.compiler.JvmPackagePartProvider;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.cli.jvm.compiler.NoScopeRecordCliBindingTrace;
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.kotlin.com.intellij.openapi.project.Project;
import org.jetbrains.kotlin.config.*;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.noesis.codeanalysis.dataobjects.input.ContextCollectionInput;
import org.noesis.codeanalysis.interfaces.Computation;

import java.util.List;
import java.util.stream.Collectors;

public class KotlinEditFileBindingContextComputation extends Computation {

    BindingContext bindingContext;

    public KotlinEditFileBindingContextComputation(ContextCollectionInput contextCollectionInput, KotlinEditFilePsiTreeComputation kotlinEditFilePsiTreeComputation) {
        super(contextCollectionInput);

        bindingContext = createBindingContext(
                kotlinEditFilePsiTreeComputation.getSourceFolderPsiTreeContainingEditedFile(),
                kotlinEditFilePsiTreeComputation.getKotlinCoreEnvironment());
    }

    private BindingContext createBindingContext(List<KtFile> sourceFiles, KotlinCoreEnvironment kotlinCoreEnvironment) {
        CompilerConfiguration configuration = kotlinCoreEnvironment.getConfiguration();
        Project project = kotlinCoreEnvironment.getProject();

        sourceFiles.forEach(file -> {
            if (file == null) {
                System.out.println("Found null KtFile");
            } else if (file.getVirtualFile() == null) {
                System.out.println("File has null VirtualFile: " + file.getName());
            }
        });

        List<KtFile> validFiles = sourceFiles.stream()
                .filter(f -> f != null && f.getVirtualFile() != null)
                .collect(Collectors.toList());

        System.out.println("Valid files count: " + validFiles.size() + " out of " + sourceFiles.size() + " total files");

        LanguageVersionSettingsImpl languageVersionSettings = new LanguageVersionSettingsImpl(
                LanguageVersion.LATEST_STABLE,  // or specific version like LanguageVersion.KOTLIN_1_9
                ApiVersion.createByLanguageVersion(LanguageVersion.LATEST_STABLE)
        );

        configuration.put(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS, languageVersionSettings);


        AnalysisResult analysisResult = TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                project,
                validFiles,
                new NoScopeRecordCliBindingTrace(),
                configuration,
                inputScope -> new JvmPackagePartProvider(languageVersionSettings, inputScope)



// packagePartProvider
        );

        return analysisResult.getBindingContext();
    }


    public BindingContext getBindingContext() {
        return bindingContext;
    }

    @Override
    public void close() throws Exception {
        bindingContext = null;
    }
}
