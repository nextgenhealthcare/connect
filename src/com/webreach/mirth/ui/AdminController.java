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


package com.webreach.mirth.ui;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.managers.ConfigUtil;
import com.webreach.mirth.managers.ManagerException;
import com.webreach.mirth.managers.types.mirth.User;

public class AdminController extends MirthController {

	public void execute(String op, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// If an execution was specified
		String[] redirExclude = { "op", "state" };
		int debugMsg = 0;
		int numErrors = 0;
		String sect = req.getParameter("sect");
		
		if (op != null && sect != null) {
			// User operations
			if (sect.equals("users")) {
				if (op.equals("new")) {
					User newUser = null;
					String username = req.getParameter("username");
					if (username != null) {
						username = username.trim();
					} else {
						username = "";
					}

					String password = req.getParameter("password");
					String passwordCheck = req.getParameter("password_check");
					String description = req.getParameter("description");

					// Validation of user input
					if (!isValidName(username)) {
						errorMessages.add("\"" + username + "\" is an invalid name. Please use only alphanumeric characters or the following symbols " + VALID_NAME_SYMBOLS);
						numErrors++;
					}

					// Test for duplicate names
					List searchList = null;
					try {
						searchList = cm.getUserList();
					} catch (ManagerException e) {
						errorMessages.add(stackToString(e));
					}
					for (int i = 0; i < searchList.size(); i++) {
						User check = (User) searchList.get(i);
						if (check.getLogin().toLowerCase().equals(username.toLowerCase())) {
							errorMessages.add(username + " already exists. ");
							numErrors++;
						}
					}

					// If input is invalid, halt execution
					if (numErrors > 0) {
						res.sendRedirect("/admin/?state=users" + parametersAsQuery(req, redirExclude));
						return;
					}

					try {
						newUser = cm.createUser();
						if (username != null && password != null && passwordCheck != null && !username.equals("") && !password.equals("") && password.equals(passwordCheck)) {
							newUser.setLogin(username);
							newUser.setPassword(ConfigUtil.encryptPassword(password));
							if (description != null) {
								newUser.setDescription(description);
							}
						} else {
							String redirTail = "";
							if (description != null) {
								redirTail = "&description=" + URLEncoder.encode(description, "UTF-8");
							}
							errorMessages.add("Invalid username or password");
							res.sendRedirect("/admin/?state=new&sect=users&username=" + username + redirTail);
							return;
						}

						cm.addUser(newUser);
					} catch (ManagerException e) {
						errorMessages.add(stackToString(e));
					}

					if (newUser != null) {
						req.setAttribute("highlightId", new Integer(newUser.getId()));
						infoMessages.add("Added User: " + username);
					} else {
						errorMessages.add("Could not create new user: " + username);
					}
				} else if (op.equals("edit")) {
					User editUser = null;

					int userId = Integer.parseInt(req.getParameter("id"));
					String password = req.getParameter("password");
					String passwordCheck = req.getParameter("password_check");
					String description = req.getParameter("description");

					if (!cm.getUser(userId).getLogin().equals("admin")) {
						try {
							editUser = cm.getUser(userId);

							if (password != null && passwordCheck != null) {
								if (!password.equals(passwordCheck)) {
									String redirTail = "";
									errorMessages.add("Password does not match.");
									if (description != null) {
										redirTail = "&description=" + URLEncoder.encode(description, "UTF-8");
									}
									res.sendRedirect("/admin/?state=edit&sect=users&id=" + userId + redirTail);
									return;
								} else {
									if (!password.equals("")) {
										editUser.setPassword(ConfigUtil.encryptPassword(password));
									}
								}
							}
							if (description != null) {
								editUser.setDescription(description);
							}
							cm.updateUser(editUser);
						} catch (ManagerException e) {
							errorMessages.add(stackToString(e));
						}
					} else {
						errorMessages.add("Cannot edit admin account.");
					}

					if (editUser == null) {
						errorMessages.add("User could not be found in the database.");
					} else {
						infoMessages.add("Your changes were applied to user " + editUser.getLogin());
					}
				} else if (op.equals("delete")) {
					User deleteUser = null;
					int userId = Integer.parseInt(req.getParameter("id"));
					String login = "";

					if (!cm.getUser(userId).getLogin().equals("admin")) {
						try {
							deleteUser = cm.getUser(userId);
							if (deleteUser != null) {
								login = deleteUser.getLogin();
								cm.removeUser(userId);
							}
						} catch (ManagerException e) {
							errorMessages.add(stackToString(e));
						}
					} else {
						errorMessages.add("Cannot delete admin account.");
					}

					if (deleteUser != null) {
						infoMessages.add("Deleted user: " + login);
					} else {
						errorMessages.add("The user has already been deleted");
					}
				}
			}
		}
	}

	public String display(String state, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String sect = req.getParameter("sect");
		if (isSection(sect, "users") || state.equals("users")) {
			if (state.equals("new")) {
				return ADMIN_FOLDER + "users_new.jsp";
			} else if (state.equals("edit")) {
				int userId = Integer.parseInt(req.getParameter("id"));
				User editUser = null;
				try {
					editUser = cm.getUser(userId);
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}

				if (editUser != null) {
					req.setAttribute("editUser", editUser);
					return ADMIN_FOLDER + "users_edit.jsp";
				} else {
					errorMessages.add("The user you are trying to edit has been deleted or has not yet been created.");
					return display("index", req, res);
				}
			} else if (state.equals("search")) {
				String searchParam = (String) req.getParameter("term");
				List searchList = null;
				try {
					searchList = cm.getUserList();
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}

				if (searchParam == null || searchParam.equals("")) {
					req.setAttribute("users", searchList);
					errorMessages.add("The search term is not valid.");
					return ADMIN_FOLDER + "users.jsp";
				}

				ArrayList<User> finalList = new ArrayList<User>();

				int counter = 0;
				boolean match;

				for (int i = 0; i < searchList.size(); i++) {
					User check = (User) searchList.get(i);
					match = false;
					for (int j = 0; searchParam.length() + j <= check.getLogin().length(); j++) {
						if (check.getLogin().substring(j, j + searchParam.length()).toLowerCase().equals(searchParam.toLowerCase()))
							match = true;
					}
					if (match) {
						try {
							finalList.add(counter, check);
						} catch (Exception e) {
							errorMessages.add(stackToString(e));
						}
						counter++;
					}
				}

				req.setAttribute("users", finalList);
				infoMessages.add("Search Matched " + counter + " entries.");

				return ADMIN_FOLDER + "users_search.jsp";
			} else {
				try {
					req.setAttribute("users", cm.getUserList());
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
				return ADMIN_FOLDER + "users.jsp";
			}
		} else if (state.equals("xml")) {
			return ADMIN_FOLDER + "xml.jsp";
		} else if (state.equals("settings")) {
			return ADMIN_FOLDER + "settings.jsp";
		} else {
			return ADMIN_FOLDER + "monitor.jsp";
		}
	}

	private boolean isSection(String sect, String section) {
		return ((sect != null)
				&& (section != null)
				&& (sect.equals(section)));
	}
}
