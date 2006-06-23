package com.webreach.mirth.server.controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.truemesh.squiggle.MatchCriteria;
import com.truemesh.squiggle.SelectQuery;
import com.truemesh.squiggle.Table;
import com.webreach.mirth.model.User;
import com.webreach.mirth.server.util.DatabaseConnection;
import com.webreach.mirth.server.util.DatabaseConnectionFactory;
import com.webreach.mirth.server.util.DatabaseUtil;

public class UserController {
	private Logger logger = Logger.getLogger(UserController.class);

	/**
	 * Returns a List containing the User with the specified <code>userId</code>.
	 * If the <code>userId</code> is <code>null</code>, all users are
	 * returned.
	 * 
	 * @param userId
	 *            the ID of the User to be returned.
	 * @return a List containing the User with the specified <code>userId</code>,
	 *         a List containing all users otherwise.
	 * @throws ControllerException
	 */
	public List<User> getUsers(Integer userId) throws ControllerException {
		logger.debug("retrieving user list: user id = " + userId);

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();

			Table users = new Table("users");
			SelectQuery select = new SelectQuery(users);

			select.addColumn(users, "id");
			select.addColumn(users, "username");
			select.addColumn(users, "password");

			if (userId != null) {
				select.addCriteria(new MatchCriteria(users, "id", MatchCriteria.EQUALS, userId.toString()));
			}

			result = dbConnection.executeQuery(select.toString());
			return getUserList(result);
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			DatabaseUtil.close(result);
			dbConnection.close();
		}
	}

	/**
	 * Converts a ResultSet to a List of User objects.
	 * 
	 * @param result
	 *            the ResultSet to be converted.
	 * @return a List of User objects.
	 * @throws SQLException
	 */
	private List<User> getUserList(ResultSet result) throws SQLException {
		ArrayList<User> users = new ArrayList<User>();

		while (result.next()) {
			User user = new User();
			user.setId(result.getInt("id"));
			user.setUsername(result.getString("username"));
			user.setPassword(result.getString("password"));
			users.add(user);
		}

		return users;
	}

	/**
	 * If a User with the specified User's ID already exists, the User will be
	 * updated. Otherwise, the User will be added.
	 * 
	 * @param user
	 *            User to be updated.
	 * @throws ControllerException
	 */
	public void updateUser(User user) throws ControllerException {
		logger.debug("updating user: " + user.toString());

		DatabaseConnection dbConnection = null;
		
		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
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

			dbConnection.executeUpdate(statement.toString());
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}

	/**
	 * Removes the user with the specified ID.
	 * 
	 * @param userId
	 *            ID of the User to be removed.
	 * @throws ControllerException
	 */
	public void removeUser(int userId) throws ControllerException {
		logger.debug("removing user: " + userId);

		DatabaseConnection dbConnection = null;
		
		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			StringBuffer statement = new StringBuffer();
			statement.append("DELETE FROM USERS");
			statement.append(" WHERE ID = " + userId + ";");
			dbConnection.executeUpdate(statement.toString());
		} catch (SQLException e) {
			throw new ControllerException(e);
		} finally {
			dbConnection.close();
		}
	}

	/**
	 * Returns a User ID given a valid username and password, -1 otherwise.
	 * 
	 * @param username
	 *            the username of the User to be authenticated.
	 * @param password
	 *            the password of the User to be authenticated.
	 * @return a User ID given a valid username and password, -1 otherwise.
	 * @throws ControllerException
	 */
	public int authenticateUser(String username, String password) throws ControllerException {
		logger.debug("authenticating user: " + username);

		DatabaseConnection dbConnection = null;
		ResultSet result = null;

		try {
			dbConnection = DatabaseConnectionFactory.createDatabaseConnection();
			
			Table users = new Table("users");
			SelectQuery select = new SelectQuery(users);
			select.addColumn(users, "id");
			select.addCriteria(new MatchCriteria(users, "username", MatchCriteria.EQUALS, username));
			select.addCriteria(new MatchCriteria(users, "password", MatchCriteria.EQUALS, password));
			
			result = dbConnection.executeQuery(select.toString());

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
