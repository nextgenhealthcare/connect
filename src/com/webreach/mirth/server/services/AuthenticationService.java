package com.webreach.mirth.server.services;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;

public class AuthenticationService {
	private Logger logger = Logger.getLogger(AuthenticationService.class);
	private DatabaseConnection dbConnection;

	public int authenticateUser(String username, String password) throws ServiceException {
		logger.debug("authenticating user: " + username);

		ResultSet result = null;

		try {
			dbConnection = new DatabaseConnection();
			StringBuffer query = new StringBuffer();
			query.append("SELECT ID FROM USERS WHERE USERNAME = '" + username + "' AND PASSWORD = '" + password + "';");
			result = dbConnection.query(query.toString());

			while (result.next()) {
				return result.getInt("ID");
			}
			
			return -1;
		} catch (SQLException e) {
			throw new ServiceException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}

}
