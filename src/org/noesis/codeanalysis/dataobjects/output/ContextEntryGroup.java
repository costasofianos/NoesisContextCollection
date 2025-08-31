package org.noesis.codeanalysis.dataobjects.output;

import java.util.List;

public record ContextEntryGroup(String text, List<ContextEntry> contextEntries) {
}
