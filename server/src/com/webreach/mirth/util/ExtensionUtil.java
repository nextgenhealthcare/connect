package com.webreach.mirth.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.fileupload.FileItem;

import com.webreach.mirth.model.ConnectorMetaData;
import com.webreach.mirth.model.MetaData;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.tools.ClassPathResource;
import com.webreach.mirth.server.util.FileUtil;
import com.webreach.mirth.server.util.UUIDGenerator;

public class ExtensionUtil {
	private static final String ARCHIVE_METADATA_XML = "archive-metadata.xml";
	private static final String PLUGIN_LOCATION = ClassPathResource.getResourceURI("plugins").getPath() + System.getProperty("file.separator");
	private static String CONNECTORS_LOCATION = ClassPathResource.getResourceURI("connectors").getPath() + System.getProperty("file.separator");
	
	public static Map<String, ? extends MetaData> loadExtensionMetaData(String location) throws ControllerException {
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return (!file.isDirectory() && file.getName().endsWith(".xml"));
			}
		};

		Map<String, MetaData> extensionMap = new HashMap<String, MetaData>();
		File path = new File(location);
		File[] extensionFiles = path.listFiles(fileFilter);
		ObjectXMLSerializer serializer = new ObjectXMLSerializer(new Class[] { PluginMetaData.class, ConnectorMetaData.class });

		try {
			for (int i = 0; i < extensionFiles.length; i++) {
				File extensionFile = extensionFiles[i];
				String xml = FileUtil.read(extensionFile.getAbsolutePath());
				MetaData extensionMetadata = (MetaData) serializer.fromXML(xml);
				extensionMap.put(extensionMetadata.getName(), extensionMetadata);
			}
		} catch (IOException ioe) {
			throw new ControllerException(ioe);
		}

		return extensionMap;
	}

	public static void saveExtensionMetaData(Map<String, ? extends MetaData> metaData, String location) throws ControllerException {
		ObjectXMLSerializer serializer = new ObjectXMLSerializer(new Class[] { PluginMetaData.class, ConnectorMetaData.class });

		try {
			Iterator i = metaData.entrySet().iterator();
			while (i.hasNext()) {
				Entry entry = (Entry) i.next();
				String name = ((MetaData)entry.getValue()).getName();
				FileUtil.write(location + name + ".xml", false, serializer.toXML(metaData.get(entry.getKey())));

			}
		} catch (IOException ioe) {
			throw new ControllerException(ioe);
		}

	}

	public static List<String> loadExtensionLibraries(String location) throws ControllerException {
		// update this to use regular expression to get the client and shared
		// libraries
		FileFilter libraryFilter = new FileFilter() {
			public boolean accept(File file) {
				return (!file.isDirectory() && (file.getName().contains("-client.jar") || file.getName().contains("-shared.jar")));
			}
		};

		List<String> extensionLibs = new ArrayList<String>();
		File path = new File(location);
		File[] extensionFiles = path.listFiles(libraryFilter);

		for (int i = 0; i < extensionFiles.length; i++) {
			File extensionFile = extensionFiles[i];
			extensionLibs.add(extensionFile.getName());
		}

		return extensionLibs;
	}

	public static void installExtension(String location, FileItem fileItem) throws ControllerException {
		// update this to use regular expression to get the client and shared
		// libraries
		String uniqueId = UUIDGenerator.getUUID();
		//append installer temp
		location = location + "install_temp" + System.getProperty("file.separator");
		ZipFile zipFile = null;
		try {
			File file = File.createTempFile(uniqueId, ".zip");
			String zipFileLocation = file.getAbsolutePath();
			fileItem.write(file);
			
			zipFile = new ZipFile(zipFileLocation);
			Enumeration entries = zipFile.entries();
			File locationFile = new File(location);
			if (!locationFile.exists()){
				locationFile.mkdir();
			}
			
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				
				if (entry.isDirectory()) {
					// Assume directories are stored parents first then
					// children.

					// This is not robust, just for demonstration purposes.
					(new File(location + entry.getName())).mkdir();
					continue;
				}else if (entry.getName().equals(ARCHIVE_METADATA_XML)){
					//Ignore the archive metadata
					continue;
				}

				copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(new File(location + entry.getName()))));
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
		}
	}

	public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}
}
