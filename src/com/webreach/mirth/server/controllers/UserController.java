package com.webreach.mirth.server.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.webreach.mirth.model.User;
import com.webreach.mirth.server.core.util.DatabaseConnection;
import com.webreach.mirth.server.core.util.DatabaseUtil;

public class UserController {
	private Logger logger = Logger.getLogger(UserController.class);
	private DatabaseConnection dbConnection;

	/**
	 * Returns a List containing the user with the specified <code>id</code>.
	 * If the <code>id</code> is <code>null</code>, all users are returned.
	 * 
	 * @param userId
	 * @return
	 * @throws ControllerException
	 */
	public List<User> getUsers(Integer userId) throws ControllerException {
		logger.debug("retrieving user list: " + userId);

		ResultSet result = null;

		try {
			dbConnection = new DatabaseConnection();
			StringBuilder query = new StringBuilder();
			query.append("SELECT ID, USERNAME, PASSWORD FROM USERS");

			if (userId != null) {
				query.append(" WHERE ID = " + userId.toString());
			}

			query.append(";");
			result = dbConnection.query(query.toString());
			return getUserList(result);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}

	/**
	 * Returns a List of User objects given a ResultSet.
	 * 
	 * @param result
	 * @return
	 * @throws SQLException
	 */
	private List<User> getUserList(ResultSet result) throws SQLException {
		ArrayList<User> users = new ArrayList<User>();

		while (result.next()) {
			User user = new User();
			user.setId(result.getInt("ID"));
			user.setUsername(result.getString("USERNAME"));
			user.setPassword(result.getString("PASSWORD"));
			users.add(user);
		}

		return users;
	}

	/**
	 * Updates the specified user.
	 * 
	 * @param user
	 * @throws ControllerException
	 */
	public void updateUser(User user) throws ControllerException {
		logger.debug("updating user: " + user.toString());

		try {
			dbConnection = new DatabaseConnection();
			StringBuffer statement = new StringBuffer();

			if (getUsers(user.getId()).isEmpty()) {
				statement.append("INSERT INTO USERS (ID, USERNAME, PASSWORD) VALUES(");
				statement.append("'" + user.getId() + "',");
				statement.append("'" + user.getUsername() + "',");
				statement.append("'" + user.getPassword() + "');");
			} else {
				statement.append("UPDATE USERS SET ");
				statement.append("USERNAME = '" + user.getUsername() + "',");
				statement.append("PASSWORD = '" + user.getPassword() + "'");
				statement.append(" WHERE ID = " + user.getId() + ";");
			}

			dbConnection.update(statement.toString());
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}

	/**
	 * Removes the user with the specified id.
	 * 
	 * @param userId
	 * @throws ControllerException
	 */
	public void removeUser(int userId) throws ControllerException {
		logger.debug("removing user: " + userId);

		try {
			dbConnection = new DatabaseConnection();
			StringBuffer statement = new StringBuffer();
			statement.append("DELETE FROM USERS");
			statement.append(" WHERE ID = " + userId + ";");
			dbConnection.update(statement.toString());
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}

	/**
	 * Returns an id given a valid username and password, -1 otherwise.
	 * 
	 * @param username
	 * @param password
	 * @return
	 * @throws ControllerException
	 */
	public int authenticateUser(String username, String password) throws ControllerException {
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
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}

}
