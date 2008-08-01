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
 * an ant task to add a mirth user
 * 
 * @author andrzej@coalese.com
 */

public class UserAddTask extends AbstractMirthTask {
	protected String userid = "";
	protected String pswd = "";
	protected String firstName = "";
	protected String lastName = "";
	protected String organization = "";
	protected String email = "";

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

	/**
	 * @param firstName
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	/**
	 * @param lastName
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	/**
	 * @param organization
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * @param email
	 */
	public void setEmail(String email) {
		this.email = email;
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
				commandUserAdd();
				disconnectClient();
			} else {
				throw (new BuildException("Userid no specified"));
			}
		} catch (ClientException e) {
			throw (new BuildException("Mirth client exception caught: " + e.getMessage(), e));
		}
	}

	private void commandUserAdd() throws ClientException, BuildException {
		User newUser = new User();

		newUser.setUsername(userid);
		newUser.setFirstName(firstName);
		newUser.setLastName(lastName);
		newUser.setOrganization(organization);
		newUser.setEmail(email);

		List<User> users = client.getUser(null);

		for (Iterator<User> iter = users.iterator(); iter.hasNext();) {
			User luser = iter.next();
			if (luser.getUsername().equalsIgnoreCase(userid)) {
				throw (new BuildException("Unable to add user: userid already in use: " + userid));
			}
		}

		try {
			client.updateUser(newUser, pswd);
			System.out.println("User \"" + userid + "\" added successfully.");
		} catch (Exception e) {
			throw (new BuildException("Unable to add user \"" + userid + "\": " + e));
		}
	}

}
