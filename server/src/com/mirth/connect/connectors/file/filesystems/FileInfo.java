/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filesystems;

import java.io.IOException;

public interface FileInfo {

	/** Gets the name of the file relative to the folder searched */
	public String getName();
	
	/** Gets the absolute pathname of the file */
	public String getAbsolutePath();
	
	/** Gets the canonical pathname string of the file */
    public String getCanonicalPath() throws IOException;
	
	/** Gets the absolute pathname of the directory holding the file */
	public String getParent();
	
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