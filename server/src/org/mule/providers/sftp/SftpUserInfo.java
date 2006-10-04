package org.mule.providers.sftp;

import com.jcraft.jsch.UserInfo;

public class SftpUserInfo implements UserInfo {
	private String password;
	
	public SftpUserInfo(String passphrase, String password) {
		this.password = password;
	}
	
	public String getPassphrase() {
		return null;
	}

	public String getPassword() {
		return password;
	}

	public boolean promptPassword(String password) {
		return false;
	}

	public boolean promptPassphrase(String passphrase) {
		return false;
	}

	public boolean promptYesNo(String str) {
		return false;
	}

	public void showMessage(String message) {

	}

}
