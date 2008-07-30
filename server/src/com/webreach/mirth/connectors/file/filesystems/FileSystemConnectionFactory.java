package com.webreach.mirth.connectors.file.filesystems;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.PoolableObjectFactory;

import com.webreach.mirth.connectors.file.FileConnector;

/** A factory to create instances of FileSystemConnection based on the
 *  endpoint and connector properties, and to adapt between them and the
 *  connection pool.
 *  
 * @author Erik Horstkotte
 */
public class FileSystemConnectionFactory implements PoolableObjectFactory {

	private static transient Log logger = LogFactory.getLog(FileSystemConnectionFactory.class);

	private String scheme;
	private String username;
	private String password;
	private String host;
	private int port;
	private boolean passive;

	/** Construct a FileSystemConnectionFactory from the endpoint URI and connector properties */
	public FileSystemConnectionFactory(String scheme, String username, String password, String host, int port, boolean passive) {

		this.scheme = scheme;
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = port;
		this.passive = passive;
	}

	/** Gets a pool key for connections on this endpoint
	 * 
	 * @param uri
	 * @param connector
	 * @return
	 */
	public static String getPoolKey(String scheme, String username, String password, String host, int port) {
		
		if (scheme.equals(FileConnector.SCHEME_FILE)) {
			return "file://";
		}
		else if (scheme.equals(FileConnector.SCHEME_FTP)) {
			return "ftp://" + username + ":" + password + "@" + host + ":" + port;
		}
		else if (scheme.equals(FileConnector.SCHEME_SFTP)) {
			return "sftp://" + username + ":" + password + "@" + host + ":" + port;
		}
		else if (scheme.equals(FileConnector.SCHEME_SMB)) {
		    return "smb://" + username + ":" + password + "@" + host + ":" + port;
		}
		else {
			logger.error("getPoolKey doesn't handle scheme " + scheme);
			return "default";
		}
	}

	public Object makeObject() throws Exception {
		
		if (scheme.equals(FileConnector.SCHEME_FILE)) {
			return new FileConnection();
		}
		else if (scheme.equals(FileConnector.SCHEME_FTP)) {
			return new FtpConnection(host, port, username, password, passive);
		}
		else if (scheme.equals(FileConnector.SCHEME_SFTP)) {
			return new SftpConnection(host, port, username, password);
		}
		else if (scheme.equals(FileConnector.SCHEME_SMB)) {
		    return new SmbFileConnection(host, username, password);
		}
		else {
			logger.error("makeObject doesn't handle scheme " + scheme);
			throw new IOException("Unimplemented or unrecognized scheme");
		}
	}

	public void destroyObject(Object arg0) throws Exception {

		FileSystemConnection connection = (FileSystemConnection) arg0;
		connection.destroy();
	}

	public void activateObject(Object arg0) throws Exception {

		FileSystemConnection connection = (FileSystemConnection) arg0;
		connection.activate();
	}

	public void passivateObject(Object arg0) throws Exception {

		FileSystemConnection connection = (FileSystemConnection) arg0;
		connection.passivate();
	}

	public boolean validateObject(Object arg0) {

		FileSystemConnection connection = (FileSystemConnection) arg0;
		return connection.isValid();
	}
}