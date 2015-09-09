package com.mirth.connect.userutil;

/**
 * Provides JSON utility methods.
 */
public class JsonUtil {
    private JsonUtil() {}

    /**
     * Formats an JSON string with indented markup.
     * 
     * @param input
     *            The JSON string to format.
     * @return The formatted JSON string.
     */
    public static String prettyPrint(String input) {
        return com.mirth.connect.util.MirthJsonUtil.prettyPrint(input);
    }
}
