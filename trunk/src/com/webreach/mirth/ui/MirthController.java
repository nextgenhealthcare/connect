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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.webreach.mirth.managers.ChangeManager;
import com.webreach.mirth.managers.ConfigUtil;
import com.webreach.mirth.managers.ConfigurationManager;
import com.webreach.mirth.managers.LogManager;
import com.webreach.mirth.managers.ManagerException;
import com.webreach.mirth.managers.MessageManager;
import com.webreach.mirth.managers.PropertyFormUtil;
import com.webreach.mirth.managers.PropertyManager;
import com.webreach.mirth.managers.StatusManager;
import com.webreach.mirth.managers.types.MirthPropertyType;
import com.webreach.mirth.managers.types.mirth.Filter;
import com.webreach.mirth.managers.types.mirth.User;

public abstract class MirthController extends HttpServlet {
	public static final String WEBAPPS_FOLDER = File.separator + "jetty" + File.separator + "webapps" + File.separator + "root";
	public static final String JSP_FOLDER = "/" + "_jsp" + "/";
	public static final String ENDPOINT_FOLDER = JSP_FOLDER + "endpoint" + "/";
	public static final String FILTER_FOLDER = JSP_FOLDER + "filter" + "/";
	public static final String WIZARD_FOLDER = JSP_FOLDER + "wizard" + "/";
	public static final String ADMIN_FOLDER = JSP_FOLDER + "admin" + "/";
	public static final String CHANNEL_FOLDER = JSP_FOLDER + "channel" + "/";
	public static final String MAIN_FOLDER = JSP_FOLDER + "main" + "/";
	public static final String TRANSFORMER_FOLDER = JSP_FOLDER + "transformer" + "/";
	public static final String USER_SESS_ID = "userSessId";
	public static final String VALID_NAME_SYMBOLS = "'_- =+(),.";

	protected ArrayList<String> errorMessages = new ArrayList<String>();
	protected ArrayList<String> infoMessages = new ArrayList<String>();

	protected ConfigurationManager cm = ConfigurationManager.getInstance();
	protected PropertyManager pm = PropertyManager.getInstance();
	protected StatusManager sm = StatusManager.getInstance();
	protected ChangeManager chm = ChangeManager.getInstance();
	protected MessageManager mm = MessageManager.getInstance();
	protected LogManager lm = LogManager.getInstance();

	MirthController() {
		cm = ConfigurationManager.getInstance();
		pm = PropertyManager.getInstance();
		sm = StatusManager.getInstance();
		chm = ChangeManager.getInstance();
		mm = MessageManager.getInstance();
		lm = LogManager.getInstance();
		
		try {
			cm.initialize();
			pm.initialize();
			sm.initialize();
//			sm.deployChannels();
			chm.initialize();
			mm.initialize();
			lm.initialize();
			
			generateForms();
		} catch (ManagerException e) {
			errorMessages.add(stackToString(e));
		}
	}

	public abstract void execute(String op, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException;

	public abstract String display(String state, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException;

	public User getUserLogin(HttpSession sess) {
		String userSessId = (String) sess.getAttribute(USER_SESS_ID);
		User user = null;
		int uid;

		if (userSessId != null) {
			try {
				uid = Integer.parseInt(userSessId);
				return (User) cm.getUser(uid);
			} catch (NumberFormatException nfe) {
				return null;
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}
		}

		return null;
	}

	public boolean isLoggedIn(HttpSession sess) {
		String userSessId = (String) sess.getAttribute(USER_SESS_ID);
		int uid;

		if (userSessId != null) {
			try {
				uid = Integer.parseInt(userSessId);
			} catch (NumberFormatException nfe) {
				return false;
			}

			return true;
		}

		return false;
	}

	public boolean login(HttpSession sess, String username, String password) {
		try {
			List<User> userList = cm.getUserList();
			Iterator<User> userIter = (Iterator<User>) userList.iterator();
			User user;

			while (userIter.hasNext()) {
				user = (User) userIter.next();
				if (user.getLogin().equals(username) && ConfigUtil.decryptPassword(user.getPassword()).equals(password)) {
					sess.setAttribute(USER_SESS_ID, Integer.toString(user.getId()));
					return true;
				}
			}

			// If no users exist, use default username and password
			if (userList.size() == 0 && username.equals("admin") && password.equals("admin")) {
				sess.setAttribute(USER_SESS_ID, "0");
				return true;
			}
		} catch (ManagerException e) {
		}
		return false;
	}

	public void logout(HttpSession sess) {
		String userSessId = (String) sess.getAttribute(USER_SESS_ID);
		if (userSessId != null) {
			sess.removeAttribute(USER_SESS_ID);
		}
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpSession sess = req.getSession(true);
		sess.setMaxInactiveInterval(-1);
		ServletContext app = getServletContext();
		RequestDispatcher dispatcher;

		errorMessages = (ArrayList<String>) sess.getAttribute("errorMessages");
		infoMessages = (ArrayList<String>) sess.getAttribute("infoMessages");

		if (errorMessages == null) {
			errorMessages = new ArrayList<String>();
			sess.setAttribute("errorMessages", errorMessages);
		}

		if (infoMessages == null) {
			infoMessages = new ArrayList<String>();
			sess.setAttribute("infoMessages", infoMessages);
		}

		// boolean loggedIn = isLoggedIn( sess );

		String op, state;
		String jsp;

		op = req.getParameter("op");

		// Execute operation if available
		if (op != null && !op.trim().equals("")) {
			if (op.equals("login")) {
				if (login(sess, req.getParameter("username"), req.getParameter("password"))) {
					infoMessages.add("Login successful!");
				} else {
					errorMessages.add("Login failed! ");
					if (req.getParameter("uri") != null) {
						String redirTail = "";
						String username = req.getParameter("username");
						if (username != null) {
							redirTail = "&username=" + URLEncoder.encode(username, "UTF-8");
						}
						res.sendRedirect("/main/?state=login&uri=" + req.getParameter("uri") + redirTail);
					} else {
						res.sendRedirect("/main/?state=login");
					}
					return;
				}
			} else if (isLoggedIn(sess)) {
				if (op.equals("logout")) {
					logout(sess);
					infoMessages.add("Logged out");
				} else {
					execute(op, req, res);
				}
			} else {
				errorMessages.add("You are not logged in");
				res.sendRedirect("/main/?state=login&uri=" + req.getRequestURI());
				return;
			}
		}

		// Set default state if needed
		state = req.getParameter("state");
		if (state == null || state.trim().equals("")) {
			state = "index";
		}

		if (isLoggedIn(sess) || state.equals("login")) {
			// Set the JSP to be loaded
			jsp = display(state, req, res);
			dispatcher = app.getRequestDispatcher(jsp);

			// Set global attributes
			req.setAttribute("user", getUserLogin(sess));
			req.setAttribute("errorMessages", errorMessages);
			req.setAttribute("infoMessages", infoMessages);
			req.setAttribute("cm", cm);
			req.setAttribute("cmVersion", cm.getVersion());
			req.setAttribute("cmBuildNumber", cm.getBuildNumber());
			req.setAttribute("newline", "\n");

			// Forward JSP
			dispatcher.forward(req, res);

			errorMessages.clear();
			infoMessages.clear();
		} else {
			String uri = req.getRequestURI();
			if (req.getQueryString() != null) {
				uri += "?" + req.getQueryString();
			}
			res.sendRedirect("/main/?state=login&uri=" + URLEncoder.encode(uri, "UTF-8"));
		}
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// Force POST and GET to act the same...

		doGet(req, res);
	}

	static public String stackToString(Exception e) {
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			System.err.println(sw.toString());
			return sw.toString();
			// return "<b>" + e.toString() + "</b><br />" +
			// sw.toString().replaceAll( "\n", "<br />" );
		} catch (Exception en) {
			return "[[BadStackString]]";
		}
	}

	protected static boolean isValidName(String name) {
		if (name == null || name.equals("")) {
			return false;
		}

		char[] cName = name.toCharArray();

		for (int i = 0; i < cName.length; i++) {
			if (!Character.isLetterOrDigit(cName[i]) && !Character.isWhitespace(cName[i]) && VALID_NAME_SYMBOLS.indexOf(cName[i]) == -1) {
				return false;
			}
		}
		return true;
	}

	protected static String parametersAsQuery(HttpServletRequest req, String[] exclude) {
		Enumeration<String> pNames = req.getParameterNames();
		int item = 0;
		String currentName = "";
		String query = "";
		String subVals[] = null;
		boolean isExcluded = false;

		try {
			while (pNames.hasMoreElements()) {
				currentName = (String) pNames.nextElement();
				isExcluded = false;

				for (int i = 0; i < exclude.length && !isExcluded; i++) {
					if (exclude[i].equals(currentName)) {
						isExcluded = true;
					}
				}

				if (!isExcluded) {
					subVals = req.getParameterValues(currentName);
					for (int i = 0; i < subVals.length; i++) {
						if (item != 0) {
							query += "&";
						}
						query += URLEncoder.encode(currentName, "UTF-8") + "=" + URLEncoder.encode(subVals[i], "UTF-8");
						item++;
					}
				}
			}
		} catch (UnsupportedEncodingException uee) {
		}
		return query;
	}

	protected static boolean isArrayContain(String[] list, String item) {
		if (list == null || item == null)
			return false;
		for (int i = 0; i < list.length; i++) {
			if (list[i].equals(item)) {
				return true;
			}
		}
		return false;
	}

	public static String idsToString(int[] ids) {
		StringBuffer sb = new StringBuffer();

		for (int x = 0; x < (ids.length - 1); x++) {
			sb.append(Integer.toString(ids[x]));
			sb.append(" ");
		}
		sb.append(ids[ids.length - 1]);

		return (sb.toString());
	}

	public static int[] stringToIds(String ids) {
		if (ids == null)
			return new int[0];
		String[] splitIds = ids.split(" ");
		int[] retval = new int[splitIds.length];
		for (int x = 0; x < splitIds.length; x++) {
			try {
				retval[x] = Integer.parseInt(splitIds[x]);
			} catch (Exception e) {
				retval[x] = 0;
			}
		}

		return retval;
	}

	public static Hashtable<Integer, Filter> filterListToHash(List<Filter> filterList) {
		Hashtable<Integer, Filter> hash = new Hashtable<Integer, Filter>();
		Iterator<Filter> filterIter = (Iterator<Filter>) filterList.iterator();
		Filter filter;
		while (filterIter.hasNext()) {
			filter = (Filter) filterIter.next();

			hash.put(new Integer(filter.getId()), (Filter) filter);
		}

		return hash;

	}

	private void generateForms() {
		String forms = "";
		ArrayList<MirthPropertyType> types = pm.getTypes("endpoint");
		File outputFile;
		File basedir = new File(".");
		FileWriter out = null;
		boolean defaultType = true;

		for (int i = 0; i < types.size(); i++) {
			forms += PropertyFormUtil.getForm("endpoint", types.get(i).getName(), defaultType);
			defaultType = false;
		}

		try {
			outputFile = new File(basedir.getCanonicalPath() + WEBAPPS_FOLDER + ENDPOINT_FOLDER + File.separator + "form.jsp");
			out = new FileWriter(outputFile);
			out.write(forms);
			out.close();
		} catch (IOException e) {
			errorMessages.add(stackToString(e));
		}
	}

}
