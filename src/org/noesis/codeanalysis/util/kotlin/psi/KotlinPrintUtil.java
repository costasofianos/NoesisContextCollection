package org.noesis.codeanalysis.util.kotlin.psi;

import org.jetbrains.kotlin.psi.KtFile;

public class KotlinPrintUtil {

    public static String prettyPrint (KtFile ktFile){
        // Create a copy to avoid modifying the original
        KtFile copy = (KtFile) ktFile.copy();

        return copy.getText();
    }
}

