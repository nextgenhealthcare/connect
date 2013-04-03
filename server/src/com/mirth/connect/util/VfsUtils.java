package com.mirth.connect.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class VfsUtils {
    private static Map<String, String> uriExtensionMap;

    static {
        /*
         * Map of file extensions to Apache VFS file-system types (see
         * http://commons.apache.org/proper/commons-vfs/filesystems.html). The import dialog will
         * detect files with these extensions and send the appropriate uri format to the
         * MessageWriterVfs class.
         */
        uriExtensionMap = new HashMap<String, String>();
        uriExtensionMap.put("zip", "zip://");
        uriExtensionMap.put("tar.gz", "tgz://");
        uriExtensionMap.put("tar.bz2", "tbz2://");
        uriExtensionMap.put("tar", "tar://");
    }

    /**
     * Automatically prepend the appropriate URI prefix to the given path.
     */
    public static String pathToUri(String path) {
        for (Entry<String, String> entry : uriExtensionMap.entrySet()) {
            String ext = entry.getKey();
            String prefix = entry.getValue();

            // if the path has this extension and does not have the corresponding uri prefix, then return the path with the uri prefix prepended
            if (path.length() >= ext.length() && path.substring(path.length() - ext.length()).toLowerCase().equals(ext) && (path.length() < prefix.length() || !path.substring(0, prefix.length()).equals(prefix))) {
                return prefix + path;
            }
        }

        return path;
    }
}
