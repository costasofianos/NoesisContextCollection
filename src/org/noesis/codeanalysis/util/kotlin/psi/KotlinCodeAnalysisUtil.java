package org.noesis.codeanalysis.util.kotlin.psi;

import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler;
import org.jetbrains.kotlin.resolve.BindingContext;

public class KotlinCodeAnalysisUtil {

    public static BindingContext analyzeAll(KotlinCoreEnvironment environment) {
        AnalysisResult result = KotlinToJVMBytecodeCompiler.INSTANCE.analyze(environment);
        return result.getBindingContext();
    }


}
