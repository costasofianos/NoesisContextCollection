package org.noesis.codeanalysis.dataobjects.output;

import java.io.File;


public record ContextEntry (
    File sourceFile,
    String text,
    ContextEntryWeight contextEntryWeight,
    String explanation
) {

}








