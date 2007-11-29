/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth-Ant.
 *
 * The Initial Developer of the Original Code is
 * Coalese Corporation.
 * Portions created by the Initial Developer are Copyright (C) 2007
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Andrzej Taramina <andrzej@coalese.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.ant;

import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.model.User;

/**
 * an ant task to chane password for a mirth user
 * 
 * @author andrzej@coalese.com
 */

public class UserChangePasswordTask extends AbstractMirthTask {
	protected String userid = "";
	protected String pswd = "";

	/**
	 * @param pswd
	 */
	public void setPswd(String pswd) {
		this.pswd = pswd;
	}

	/**
	 * @param userid
	 */
	public void setUserid(String userid) {
		this.userid = userid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */

	public void executeTask() throws BuildException {
		try {
			if (userid.length() > 0) {
				connectClient();
				commandUserChangePassword();
				disconnectClient();
			} else {
				throw (new BuildException("Userid no specified"));
			}
		} catch (ClientException e) {
			throw (new BuildException("Mirth client exception caught: " + e.getMessage(), e));
		}
	}

	private void commandUserChangePassword() throws ClientException {
		List<User> users = client.getUser(null);

		for (Iterator<User> iter = users.iterator(); iter.hasNext();) {
			User u = iter.next();
			if (u.getId().toString().equalsIgnoreCase(userid) || u.getUsername().equalsIgnoreCase(userid)) {
				client.updateUser(u, pswd);
				System.out.println("User \"" + u.getUsername() + "\" password changed.");
				break;
			}
		}
	}

}
