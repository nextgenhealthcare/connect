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
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.controllers;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.webreach.mirth.model.User;
import com.webreach.mirth.server.util.SqlConfig;
import com.webreach.mirth.util.Encrypter;

public class UserController {
	private Logger logger = Logger.getLogger(this.getClass());
	private SqlMapClient sqlMap = SqlConfig.getSqlMapInstance();
	private ConfigurationController configurationController = new ConfigurationController();
	
	public List<User> getUser(User user) throws ControllerException {
		logger.debug("getting user: " + user);
		String originalPassword = new String();
		if (user != null){
			originalPassword = user.getPassword();
			Encrypter encrypter = new Encrypter(configurationController.getEncryptionKey());
			user.setPassword(encrypter.encrypt(originalPassword));
		}
		
		try {
			return sqlMap.queryForList("getUser", user);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}finally{
			if (user != null){
				user.setPassword(originalPassword);
			}
		}
	}

	public void updateUser(User user) throws ControllerException {
		String originalPassword = user.getPassword();
		try {
			Encrypter encrypter = new Encrypter(configurationController.getEncryptionKey());
			user.setPassword(encrypter.encrypt(originalPassword));
			if (user.getId() == null) {
				logger.debug("adding user: " + user);
				sqlMap.insert("insertUser", user);
			} else {
				logger.debug("updating user: " + user);
				sqlMap.update("updateUser", user);
			}
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			user.setPassword(originalPassword);
		}
	}

	public void removeUser(User user) throws ControllerException {
		logger.debug("removing user: " + user);

		try {
			sqlMap.delete("deleteUser", user);
		} catch (SQLException e) {
			throw new ControllerException(e);
		}
	}
}
