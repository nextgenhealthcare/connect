package com.webreach.mirth.connectors.file.filesystems;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.mule.MuleException;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;

/** The FileSystemConnection class for files accessed via FTP.
 * 
 * @author Erik Horstkotte
 *
 */
public class FtpConnection implements FileSystemConnection {

	public class FtpFileInfo implements FileInfo {

		FTPFile theFile;
		
		public FtpFileInfo(FTPFile theFile) {
			this.theFile = theFile;
		}

		public long getLastModified() {
			return theFile.getTimestamp().getTimeInMillis();
		}

		public String getName() {
			return theFile.getName();
		}

		public long getSize() {
			return theFile.getSize();
		}

		public boolean isDirectory() {
			return theFile.isDirectory();
		}

		public boolean isFile() {
			return theFile.isFile();
		}

		public boolean isReadable() {
			return true;
			// return theFile.hasPermission(access, permission);
		}
		
	}
	
	private static transient Log logger = LogFactory.getLog(FtpConnection.class);

	/** The apache commons FTP client instance */
	private FTPClient client = null;

	public FtpConnection(String host, int port, String username, String password) throws Exception {
		
		client = new FTPClient();
		try {
			if (port > 0) {
				client.connect(host, port);
			} else {
				client.connect(host);
			}
			if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
				throw new IOException("Ftp error: " + client.getReplyCode());
			}
			if (!client.login(username, password)) {
				throw new IOException("Ftp error: " + client.getReplyCode());
			}
			if (!client.setFileType(FTP.BINARY_FILE_TYPE)) {
				throw new IOException("Ftp error");
			}
		} catch (Exception e) {
			if (client.isConnected()) {
				client.disconnect();
			}
			throw e;
		}
	}
	
	public List<FileInfo> listFiles(String fromDir, FilenameFilter filenameFilter)
		throws Exception
	{
		if (!client.changeWorkingDirectory(fromDir)) {
			throw new IOException("Ftp error: " + client.getReplyCode());
		}

		FTPFile[] files = client.listFiles();
		if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
			throw new IOException("Ftp error: " + client.getReplyCode());
		}
		
		if (files == null || files.length == 0) {
			return new ArrayList<FileInfo>();
		}
		
		List<FileInfo> v = new ArrayList<FileInfo>(files.length);
		for (int i = 0; i < files.length; i++) {
			if ((files[i] != null) && files[i].isFile()) {
				if (filenameFilter == null || filenameFilter.accept(null, files[i].getName())) {
					v.add(new FtpFileInfo(files[i]));
				}
			}
		}
		return v;
	}

	public InputStream readFile(String file, String fromDir)
		throws Exception
	{
		if (!client.changeWorkingDirectory(fromDir)) {
			throw new IOException("Ftp error: " + client.getReplyCode());
		}

		return client.retrieveFileStream(file);
	}

	public boolean canAppend() {

		return false;
	}
	
	public OutputStream writeFile(String file, String toDir, boolean append)
		throws Exception
	{
		cdmake(toDir);
		return client.storeFileStream(file);
	}

	public void delete(String file, String fromDir) throws Exception {
		
		if (!client.changeWorkingDirectory(fromDir)) {
			throw new IOException("Ftp error: " + client.getReplyCode());
		}

		boolean deleteSucceeded = client.deleteFile(file);
		if (!deleteSucceeded) {
			throw new IOException("Ftp error: " + client.getReplyCode());
		}
	}

	private void cdmake(String dir) throws Exception {
		
		if (!client.changeWorkingDirectory(dir)) {
			if (!client.makeDirectory(dir)) {
				String tempDir = dir;
				if (tempDir.startsWith("/")) {
					tempDir = tempDir.substring(1);
				}

				String[] dirs = tempDir.split("/");

				if (dirs.length > 0) {
					for (int i = 0; i < dirs.length; i++) {
						if (!client.changeWorkingDirectory(dirs[i])) {
							logger.debug("Making directory: " + dirs[i]);
							if (!client.makeDirectory(dirs[i])) {
								throw new Exception("Unable to make destination directory: " + dirs[i]);
							}
							if (!client.changeWorkingDirectory(dirs[i])) {
								throw new Exception("Unable to change to destination directory: " + dirs[i]);
							}
						}
					}
				}
			}
		}
	}

	public void move(String fromName, String fromDir, String toName, String toDir) throws Exception {
		
		cdmake(toDir);

		try {
			client.deleteFile(toName);
		} catch (Exception e) {
			logger.info("Unable to delete destination file");
		}

		if (!client.changeWorkingDirectory(fromDir)) {
			throw new Exception("Unable to change to directory: " + fromDir.substring(1) + "/");
		}

		boolean renameSucceeded = client.rename(fromName.replaceAll("//", "/"), (toDir + "/" + toName).replaceAll("//", "/"));
		if (!renameSucceeded) {
			throw new IOException("Ftp error: " + client.getReplyCode());
		}
	}

	public boolean isConnected() {
		
		if (client != null) {
			return client.isConnected();
		}
		else {
			return false;
		}
	}

	// **************************************************
	// Lifecycle methods
	
	public void activate() {
		client.setReaderThread(true);
	}

	public void passivate() {
		client.setReaderThread(false);
	}

	public void destroy() {
		try{
			client.logout();
			client.disconnect();
		} catch (Exception e){
			logger.debug(e);
		}
	}

	public boolean isValid() {
		try {
			client.sendNoOp();
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
