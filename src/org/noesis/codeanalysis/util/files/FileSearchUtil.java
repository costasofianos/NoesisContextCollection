package org.noesis.codeanalysis.util.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// FileSearchUtil.java
public class FileSearchUtil {
    public static List<File> findFilesByNamePattern(File rootFolder, String fileNamePattern) {
        List<File> matchingFiles = new ArrayList<>();
        findFilesByNamePattern(rootFolder, fileNamePattern, matchingFiles);
        return matchingFiles;
    }

    private static void findFilesByNamePattern(File folder, String fileNamePattern, List<File> matchingFiles) {
        if (!folder.isDirectory()) {
            return;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                findFilesByNamePattern(file, fileNamePattern, matchingFiles);
            } else if (file.getName().toLowerCase().matches(fileNamePattern.toLowerCase())) {
                matchingFiles.add(file);
            }
        }
    }
}