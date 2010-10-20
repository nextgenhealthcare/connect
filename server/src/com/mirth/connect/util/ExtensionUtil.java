/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.mirth.connect.client.core.VersionMismatchException;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.ExtensionLibrary;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;
import com.mirth.connect.server.controllers.ExtensionController.ExtensionType;
import com.mirth.connect.server.util.UUIDGenerator;

public class ExtensionUtil {

    public static Map<String, ? extends MetaData> loadExtensionMetaData(final ExtensionType extensionType) throws ControllerException {
        Map<String, MetaData> extensionMap = new HashMap<String, MetaData>();
        List<File> extensionFiles = loadExtensionFiles(extensionType);
        ObjectXMLSerializer serializer = new ObjectXMLSerializer(new Class[] { MetaData.class, PluginMetaData.class, ConnectorMetaData.class, ExtensionLibrary.class });

        try {
            for (File extensionFile : extensionFiles) {
                MetaData extensionMetadata = (MetaData) serializer.fromXML(FileUtils.readFileToString(extensionFile));
                extensionMap.put(extensionMetadata.getName(), extensionMetadata);
            }
        } catch (IOException ioe) {
            throw new ControllerException(ioe);
        }

        return extensionMap;
    }

    public static void saveExtensionMetaData(Map<String, ? extends MetaData> metaData) throws ControllerException {
        ObjectXMLSerializer serializer = new ObjectXMLSerializer(new Class[] { MetaData.class, PluginMetaData.class, ConnectorMetaData.class, ExtensionLibrary.class });

        try {
            for (Entry<String, ? extends MetaData> entry : metaData.entrySet()) {
                MetaData extensionMetaData = entry.getValue();
                String fileName = ExtensionType.PLUGIN.getFileNames()[0];

                if (extensionMetaData instanceof ConnectorMetaData) {
                    if (((ConnectorMetaData) extensionMetaData).getType().equals(ConnectorMetaData.Type.SOURCE)) {
                        fileName = ExtensionType.SOURCE.getFileNames()[0];
                    } else {
                        fileName = ExtensionType.DESTINATION.getFileNames()[0];
                    }
                }

                File metaDataFile = new File(ExtensionController.getExtensionsPath() + extensionMetaData.getPath() + File.pathSeparator + fileName);
                FileUtils.writeStringToFile(metaDataFile, serializer.toXML(metaData.get(entry.getKey())));
            }
        } catch (IOException ioe) {
            throw new ControllerException(ioe);
        }
    }

    public static List<String> loadClientLibraries(List<MetaData> extensionMetaData) {
        List<String> extensionLibraries = new ArrayList<String>();

        for (MetaData metaData : extensionMetaData) {
            for (ExtensionLibrary library : metaData.getLibraries()) {
                if (library.getType().equals(ExtensionLibrary.Type.CLIENT) || library.getType().equals(ExtensionLibrary.Type.SHARED)) {
                    extensionLibraries.add(metaData.getPath() + "/" + library.getPath());
                }
            }
        }

        return extensionLibraries;
    }

    /**
     * Find all extension files of a certain type and return them.
     * 
     * @param extensionType
     * @return List of extension MetaData xml files.
     */
    private static List<File> loadExtensionFiles(final ExtensionType extensionType) {

        FileFilter extensionFileFilter = new FileFilter() {
            public boolean accept(File file) {
                boolean accept = false;

                if (file.isDirectory()) {
                    return false;
                }

                for (String fileName : extensionType.getFileNames()) {
                    if (file.getName().equalsIgnoreCase(fileName)) {
                        accept = true;
                    }
                }
                return (accept);
            }
        };

        FileFilter directoryFilter = new FileFilter() {
            public boolean accept(File file) {
                return (file.isDirectory());
            }
        };

        List<File> extensionFiles = new ArrayList<File>();
        File extensionsPath = new File(ExtensionController.getExtensionsPath());
        File[] directories = extensionsPath.listFiles(directoryFilter);

        for (File directory : directories) {
            File[] singleExtensionFiles = directory.listFiles(extensionFileFilter);
            extensionFiles.addAll(Arrays.asList(singleExtensionFiles));

        }

        return extensionFiles;
    }

    public static void installExtension(FileItem fileItem) throws ControllerException {
        // update this to use regular expression to get the client and shared
        // libraries
        String uniqueId = UUIDGenerator.getUUID();
        // append installer temp
        String location = ExtensionController.getExtensionsPath() + "install_temp" + System.getProperty("file.separator");
        File locationFile = new File(location);
        if (!locationFile.exists()) {
            locationFile.mkdir();
        }

        File file = null;
        ZipFile zipFile = null;
        try {
            file = File.createTempFile(uniqueId, ".zip", locationFile);
            String zipFileLocation = file.getAbsolutePath();
            fileItem.write(file);

            zipFile = new ZipFile(zipFileLocation);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            // First check to see if Mirth is compatible with this extension.
            String mirthVersion = ControllerFactory.getFactory().createConfigurationController().getServerVersion();

            // If there is no build version, just use the patch version
            if (mirthVersion.split("\\.").length == 4) {
                mirthVersion = mirthVersion.substring(0, mirthVersion.lastIndexOf('.'));
            }

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String entryName = entry.getName();
                String plugin = ExtensionController.ExtensionType.PLUGIN.getFileNames()[0];
                String destination = ExtensionController.ExtensionType.DESTINATION.getFileNames()[0];
                String source = ExtensionController.ExtensionType.SOURCE.getFileNames()[0];

                if (entryName.endsWith(plugin) || entryName.endsWith(destination) || entryName.endsWith(source)) {
                    ObjectXMLSerializer serializer = new ObjectXMLSerializer(new Class[] { MetaData.class, PluginMetaData.class, ConnectorMetaData.class, ExtensionLibrary.class });
                    MetaData extensionMetaData = (MetaData) serializer.fromXML(IOUtils.toString(zipFile.getInputStream(entry)));

                    String[] mirthVersions = extensionMetaData.getMirthVersion().split(",");
                    boolean compatible = false;

                    for (int i = 0; i < mirthVersions.length; i++) {
                        if (mirthVersions[i].trim().equals(mirthVersion)) {
                            compatible = true;
                        }
                    }

                    if (!compatible) {
                        throw new VersionMismatchException("Extension MetaData \"" + entry.getName() + "\" is not compatible with Mirth version " + mirthVersion + ".");
                    }
                }
            }

            // Reset the entries and extract
            entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                if (entry.isDirectory()) {
                    // Assume directories are stored parents first then
                    // children.

                    // This is not robust, just for demonstration purposes.
                    (new File(location + entry.getName())).mkdir();
                    continue;
                }

                IOUtils.copy(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(new File(location + entry.getName()))));
            }
        } catch (Exception e) {
            throw new ControllerException(e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (Exception e) {
                    throw new ControllerException(e);
                }
            }
            if (file != null) {
                try {
                    file.delete();
                } catch (Exception e) {
                    throw new ControllerException(e);
                }
            }
        }
    }
}
