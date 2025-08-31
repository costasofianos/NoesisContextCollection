package org.noesis.codeanalysis.util.kotlin.psi;

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys;
import org.jetbrains.kotlin.cli.common.config.ContentRootsKt;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.com.intellij.openapi.Disposable;
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer;
import org.jetbrains.kotlin.config.*;

import java.io.File;

public class KotlinEnvironmentUtil implements AutoCloseable {

    Disposable disposable = Disposer.newDisposable();

    public KotlinCoreEnvironment createEnvironment(File sourceDir)  {
        // 1. Setup configuration
        CompilerConfiguration configuration = new CompilerConfiguration();
        configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.Companion.getNONE());;
        configuration.put(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS,
                LanguageVersionSettingsImpl.DEFAULT);

        LanguageVersionSettings languageVersionSettings = new LanguageVersionSettingsImpl(
                LanguageVersion.LATEST_STABLE,
                ApiVersion.LATEST_STABLE
        );
        configuration.put(JVMConfigurationKeys.JVM_TARGET, JvmTarget.JVM_21);
        configuration.put(CommonConfigurationKeys.MODULE_NAME, "<my-module-name>");
        // Set the platform


        addKtFilesAsRoots(configuration, sourceDir);

// 2. Create the environment

        return KotlinCoreEnvironment.createForProduction(
                disposable,
                configuration,
                EnvironmentConfigFiles.JVM_CONFIG_FILES
        );

    }

    private static void addKtFilesAsRoots(CompilerConfiguration configuration, File dir) {
        if (!dir.exists() || !dir.isDirectory()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                addKtFilesAsRoots(configuration, file); // Recurse
            } else if (file.getName().endsWith(".kt")) {
                ContentRootsKt.addKotlinSourceRoot(configuration, file.getAbsolutePath());
            }
        }
    }

    @Override
    public void close() throws Exception {
        Disposer.dispose(disposable);
        disposable = null;
    }
}
