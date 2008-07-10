package com.webreach.mirth.connectors.file.filesystems;

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