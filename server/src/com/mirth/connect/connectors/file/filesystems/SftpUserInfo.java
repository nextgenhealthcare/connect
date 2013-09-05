/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.file.filesystems;

import com.jcraft.jsch.UserInfo;

public class SftpUserInfo implements UserInfo {
	private String password;
	
	public SftpUserInfo(String password) {
		this.password = password;
	}
	
	public String getPassphrase() {
		return null;
	}

	public String getPassword() {
		return password;
	}

	public boolean promptPassword(String password) {
		return true;
	}

	public boolean promptPassphrase(String passphrase) {
		return false;
	}

	public boolean promptYesNo(String str) {
		return true;
	}

	public void showMessage(String message) {

	}

}