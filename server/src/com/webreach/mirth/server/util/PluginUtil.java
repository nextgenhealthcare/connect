package com.webreach.mirth.server.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.webreach.mirth.model.MetaData;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.ControllerException;

public class PluginUtil
{
    public static Map<String, ? extends MetaData> loadPluginMetaData (String location) throws ControllerException
    {
        FileFilter fileFilter = new FileFilter()
        {
            public boolean accept(File file)
            {
                return (!file.isDirectory() && file.getName().endsWith(".xml"));
            }
        };
        
        Map<String, MetaData> pluginsMap = new HashMap<String, MetaData>();
        File path = new File(location);
        File[] pluginFiles = path.listFiles(fileFilter);
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();

        try
        {
            for (int i = 0; i < pluginFiles.length; i++)
            {
                File pluginFile = pluginFiles[i];
                String xml = FileUtil.read(pluginFile.getAbsolutePath());
                MetaData pluginMetadata = (MetaData) serializer.fromXML(xml);
                pluginsMap.put(pluginMetadata.getName(), pluginMetadata);
            }
        }
        catch (IOException ioe)
        {
            throw new ControllerException(ioe);
        }

        return pluginsMap;
    }

    public static List<String> loadPluginLibraries(String location) throws ControllerException
    {
        // update this to use regular expression to get the client and shared
        // libraries
        FileFilter libraryFilter = new FileFilter()
        {
            public boolean accept(File file)
            {
                return (!file.isDirectory() && (file.getName().contains("-client.jar") || file.getName().contains("-shared.jar")));
            }
        };

        List<String> pluginLibs = new ArrayList<String>();
        File path = new File(location);
        File[] pluginFiles = path.listFiles(libraryFilter);

        for (int i = 0; i < pluginFiles.length; i++)
        {
            File pluginFile = pluginFiles[i];
            pluginLibs.add(pluginFile.getName());
        }

        return pluginLibs;
    }
}
