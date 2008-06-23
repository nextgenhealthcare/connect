package com.webreach.mirth.connectors.file.filesystems;

public interface FileInfo {

	/** Gets the name of the file relative to the folder searched */
	public String getName();
	
	/** Gets the size of the file in bytes */
	public long getSize();
	
	/** Gets the date and time the file was last modified, in milliseconds since the epoch */
	public long getLastModified();
	
	/** Tests if the file is a directory */
	public boolean isDirectory();
	
	/** Tests if the file is a plain file */
	public boolean isFile();

	/** Tests if the file exists and is readable */
	public boolean isReadable();
}
