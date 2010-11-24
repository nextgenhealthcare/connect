/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filesystems;

import java.io.ByteArrayInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.UserInfo;
import com.mirth.connect.connectors.file.filters.RegexFilenameFilter;

public class SftpConnection implements FileSystemConnection {

	public class SftpFileInfo implements FileInfo {

		String thePath;
		ChannelSftp.LsEntry theFile;

		public SftpFileInfo(String path, ChannelSftp.LsEntry theFile) {
			this.thePath = path;
			this.theFile = theFile;
		}

		public long getLastModified() {
			return theFile.getAttrs().getMTime();
		}

		public String getName() {
			return theFile.getFilename();
		}

		/** Gets the absolute pathname of the file */
		public String getAbsolutePath() {
			
			return getParent() + "/" + getName();
		}
		
		/** Gets the absolute pathname of the directory holding the file */
		public String getParent() {
			
			return this.thePath;
		}
		
		public long getSize() {
			return theFile.getAttrs().getSize();
		}

		public boolean isDirectory() {
			return theFile.getAttrs().isDir();
		}

		public boolean isFile() {
			SftpATTRS attrs = theFile.getAttrs();
			return !attrs.isDir() && !attrs.isLink();
		}

		public boolean isReadable() {
			return true;
			// return (theFile.getAttrs().getPermissions() & MASK) != 0;
		}
	}
	
	private static transient Log logger = LogFactory.getLog(SftpConnection.class);

	/** The JSch SFTP client instance */
	private ChannelSftp client = null;
	private String lastDir = null;

	public SftpConnection(String host, int port, String username, String password, int timeout) throws Exception {
		
		JSch jsch = new JSch();
		client = new ChannelSftp();

		try {
			Session session = null;

			if (port > 0) {
				session = jsch.getSession(username, host, port);
			} else {
				session = jsch.getSession(username, host);
			}
			
			session.setTimeout(timeout);

			UserInfo userInfo = new SftpUserInfo(password);
			session.setUserInfo(userInfo);
			session.connect(timeout);

			Channel channel = session.openChannel("sftp");
			channel.connect();
			client = (ChannelSftp) channel;
		}
		catch (Exception e) {
			
			if (client.isConnected()) {
				client.disconnect();
			}

			throw e;
		}
	}

	public List<FileInfo> listFiles(String fromDir, String filenamePattern, boolean isRegex) throws Exception
	{
	    lastDir = fromDir;
        FilenameFilter filenameFilter;
        
        if (isRegex) {
            filenameFilter = new RegexFilenameFilter(filenamePattern);    
        } else {
            filenameFilter = new WildcardFileFilter(filenamePattern);
        }
	    
		client.cd(fromDir);
		Vector<ChannelSftp.LsEntry> entries = client.ls(".");
		List<FileInfo> files = new ArrayList<FileInfo>(entries.size());

		for (Iterator<ChannelSftp.LsEntry> iter = entries.iterator(); iter.hasNext();) {
			ChannelSftp.LsEntry entry = iter.next();

			if (!entry.getAttrs().isDir() && !entry.getAttrs().isLink()) {
				if ((filenameFilter == null) || (filenameFilter.accept(null, entry.getFilename()))) {
					files.add(new SftpFileInfo(fromDir, entry));
				}
			}
		}

		return files;
	}

	public boolean canRead(String readDir) {
	    try {
	        lastDir = readDir;
	        client.cd(readDir);
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	public boolean canWrite(String writeDir) {
        try {
            lastDir = writeDir;
            client.cd(writeDir);
            return true;
        } catch (Exception e) {
            return false;
        }
	}
	
	public InputStream readFile(String file, String fromDir) throws Exception {
	    lastDir = fromDir;
		client.cd(fromDir);
		return client.get(file);
	}

	/** Must be called after readFile when reading is complete */
	public void closeReadFile() throws Exception {
		// nothing
	}

	public boolean canAppend() {

		return true;
	}
	
	public void writeFile(String file, String toDir, boolean append, byte[] message) throws Exception
	{
	    lastDir = toDir;
		cdmake(toDir);
		int mode = 0;
		if (append)
			mode = ChannelSftp.APPEND;
		else
			mode = ChannelSftp.OVERWRITE;
		client.put(new ByteArrayInputStream(message), file, mode);
	}

	public void delete(String file, String fromDir, boolean mayNotExist) throws Exception
	{
		client.cd(fromDir);
		try {
			client.rm(file);
		}
		catch (Exception e) {
			if (!mayNotExist) {
				throw e;
			}
		}
	}

	private void cdmake(String dir) throws Exception {
		
		try {
			client.cd(dir);
		}
		catch (Exception e) {
			String toDir = dir;
			if (toDir.startsWith("/")) {
				toDir = toDir.substring(1);
				client.cd("/");
			}
			String[] dirs = toDir.split("/");
			if (dirs.length > 0) {
				for (int i = 0; i < dirs.length; i++) {
					try {
						client.cd(dirs[i]);
					} catch (Exception ex) {
						logger.debug("Making directory: " + dirs[i]);
						client.mkdir(dirs[i]);
						client.cd(dirs[i]);
					}
				}
			}
		}
	}

	public void move(String fromName, String fromDir, String toName, String toDir)
		throws Exception
	{
		client.cd(fromDir); // start in the read directory
		// Create any missing directories in the toDir path
		cdmake(toDir);

		try {
			client.rm(toName);
		}
		catch (Exception e) {
			logger.info("Unable to delete destination file");
		}

		client.cd(fromDir); // move to the read directory
		client.rename(fromName.replaceAll("//", "/"), (toDir + "/" + toName).replaceAll("//", "/"));
	}

	public boolean isConnected() {
		return client.isConnected();
	}

	// **************************************************
	// Lifecycle methods
	
	public void activate() {
		// Nothing
	}

	public void passivate() {
		// Nothing
	}

	public void destroy() {
		client.quit();
		client.disconnect();
	}

	public boolean isValid() {
	    if (lastDir == null) {
	        return client.isConnected();
	    } else {
	        return client.isConnected() && canRead(lastDir);
	    }
	}
}