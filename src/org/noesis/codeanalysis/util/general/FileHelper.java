package org.noesis.codeanalysis.util.general;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHelper {

    public static String addToPath(File baseFile, String... additionalPaths) {
        if (baseFile == null) {
            return "";
        }
        return addToPath(baseFile.getAbsolutePath(), additionalPaths);
    }


    public static String addToPath(String basePath, String... additionalPaths) {
        if (basePath == null || basePath.isEmpty()) {
            return "";
        }

        Path result = Paths.get(basePath);
        for (String path : additionalPaths) {
            if (path != null && !path.isEmpty()) {
                result = result.resolve(path);
            }
        }

        return result.normalize().toString();
    }

}
