/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.ant;

import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.model.User;

/**
 * an ant task to list all mirth users
 * 
 * @author andrzej@coalese.com
 */

public class UserListTask extends AbstractMirthTask {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */

	public void executeTask() throws BuildException {
		try {
			connectClient();
			commandUserList();
			disconnectClient();
		} catch (ClientException e) {
			throw (new BuildException("Mirth client exception caught: " + e.getMessage(), e));
		}
	}

	private void commandUserList() throws ClientException {
		List<User> users = client.getUser(null);

		System.out.println("ID\tUser\t\tFirst Name\t\tLast Name\t\tOrganization\t\t\tEmail");

		for (Iterator<User> iter = users.iterator(); iter.hasNext();) {
			User user = iter.next();
			System.out.println(user.getId() + "\t\t" + user.getUsername() + "\t\t" + user.getFirstName() + "\t\t" + user.getLastName() + "\t\t" + user.getOrganization() + "\t\t\t" + user.getEmail());
		}
	}

}
