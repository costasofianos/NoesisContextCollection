package org.noesis.codeanalysis.util.kotlin.general;

public class KotlinSourceCodeUtil {

    public static String generateCommentBanner(String header) {
        final int BANNER_WIDTH = 80;  // Standard width for A4 paper with typical font
        final String FILL_CHAR = "*";

        // Center the header text
        String centeredHeader = header.trim();
        int headerLength = centeredHeader.length();
        int sideStars = (BANNER_WIDTH - headerLength - 2) / 2; // -2 for the opening and closing '/'

        StringBuilder banner = new StringBuilder();

        // Top line
        banner.append('/').append(FILL_CHAR.repeat(BANNER_WIDTH - 2)).append("/\n");

        // Header line
        banner.append('/').append(FILL_CHAR.repeat(sideStars));
        banner.append(centeredHeader);
        banner.append(FILL_CHAR.repeat(BANNER_WIDTH - sideStars - headerLength - 2));
        banner.append("/\n");

        // Bottom line
        banner.append('/').append(FILL_CHAR.repeat(BANNER_WIDTH - 2)).append('/');

        return banner.toString();
    }

    public static String getRelativePathFromFullyQualifiedName(String fullyQualifiedName) {

        int lastDotIndex = fullyQualifiedName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            return "/" + fullyQualifiedName.substring(0, lastDotIndex).replace(".", "/")
                    + fullyQualifiedName.substring(lastDotIndex);
        }
        return "/" + fullyQualifiedName;
    }
}