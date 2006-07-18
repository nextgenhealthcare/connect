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
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.managers.ManagerException;
import com.webreach.mirth.managers.types.mirth.Channel;
import com.webreach.mirth.managers.types.mirth.Filter;
import com.webreach.mirth.managers.types.mirth.Script;

public class FilterController extends MirthController {

	/*
	 * public static final String[] SCRIPT_TYPES = {
	 * ConfigurationManager.SCRIPT_ECMA, ConfigurationManager.SCRIPT_PYTHON };
	 */
	public static final String[] SCRIPT_TYPES = { "JavaScript", "Java", "Python" };

	public void execute(String op, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String[] redirExclude = { "op", "state" };
		int numErrors = 0;

		// If an execution was specified
		if (op != null) {
			if (op.equals("new")) {
				Filter newFilter = null;
				Script newScript = null;

				// user input
				String name = req.getParameter("name");
				String description = req.getParameter("description");
				String type = req.getParameter("script");
				String script = req.getParameter("scriptString");

				if (name != null) {
					name = name.trim();
				}

				if (description == null) {
					description = "";
				}

				// validation of user input
				if (!isValidName(name)) {
					errorMessages.add("\"" + name + "\" is an invalid name. Please use only alphanumeric characters or the following symbols " + VALID_NAME_SYMBOLS);
					numErrors++;
				}

				// test for duplicate names
				List searchList = null;
				
				try {
					searchList = cm.getFilterList();
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
				
				for (int i = 0; i < searchList.size(); i++) {
					Filter check = (Filter) searchList.get(i);
					if (check.getName().toLowerCase().equals(name.toLowerCase())) {
						errorMessages.add(name + " already exists. ");
						numErrors++;
					}
				}

				if (script == null || script.equals("")) {
					errorMessages.add("A script must be set.");
					numErrors++;
				}

				// If input is invalid, halt execution
				if (numErrors > 0) {
					res.sendRedirect("/filter/?state=new&" + parametersAsQuery(req, redirExclude));
					return;
				}

				try {

					newScript = cm.createScript();
					newScript.setType(type);
					newScript.setValue(script);

					newFilter = cm.createFilter();
					newFilter.setName(name);
					newFilter.setDescription(description);
					newFilter.setScript(newScript);

					// add new Filter
					cm.addFilter(newFilter);
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}

				if (newFilter != null) {
					infoMessages.add("Added Filter: " + newFilter.getName());
					req.setAttribute("highlightId", new Integer(newFilter.getId()));
					String origin = req.getParameter("orig");
					
					if (origin != null && !origin.trim().equals("")) {
						res.sendRedirect(origin + newFilter.getId());
					}
				} else {
					errorMessages.add("Failed to create new Filter: " + name);
				}
			} else if (op.equals("delete")) {
				// execute delete command
				Filter deleteFilter = null;
				String name = "";

				int filterId = 0;
				
				try {
					filterId = Integer.parseInt(req.getParameter("id"));
				} catch (Exception e) {
					errorMessages.add("There was an error in trying to delete the requested Filter. Please try again.");
					return;
				}

				if (filterId != 0) {
					try {
						deleteFilter = cm.getFilter(filterId);
						
						if (deleteFilter != null) {
							name = deleteFilter.getName();
							cm.removeFilter(filterId);
						}
					} catch (ManagerException e) {
						errorMessages.add(stackToString(e));
					}
				}

				if (deleteFilter != null) {
					infoMessages.add("Deleted Filter: " + name);
				} else {
					errorMessages.add("Filter has already been deleted.");
				}
			} else if (op.equals("edit")) {
				Filter editFilter = null;

				String id = req.getParameter("id");
				String name = req.getParameter("name").trim();
				String description = req.getParameter("description");
				String type = req.getParameter("script");
				String script = req.getParameter("scriptString");

				int filterId = 0;

				if (name != null)
					name = name.trim();
				if (description == null)
					description = "";

				try {
					filterId = Integer.parseInt(id);
				} catch (Exception e) {
					errorMessages.add("There was an error trying to edit the requested Filter. Please try again.");
					return;
				}

				// validation of user input
				if (!isValidName(name)) {
					errorMessages.add("\"" + name + "\" is an invalid name. Please use only alphanumeric characters or the following symbols " + VALID_NAME_SYMBOLS);
					numErrors++;
				}

				// test for duplicate names
				List searchList = null;
				
				try {
					searchList = cm.getFilterList();
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
				
				for (int i = 0; i < searchList.size(); i++) {
					Filter check = (Filter) searchList.get(i);
					if (check.getName().toLowerCase().equals(name.toLowerCase())) {
						if (filterId != check.getId()) {
							errorMessages.add(name + " already exists. ");
							numErrors++;
						}
					}
				}

				if (script == null || script.equals("")) {
					errorMessages.add("A script must be set.");
					numErrors++;
				}

				// if input is invalid, halt execution
				if (numErrors > 0) {
					res.sendRedirect("/filter/?state=edit&" + parametersAsQuery(req, redirExclude));
					return;
				}

				if (filterId != 0) {
					try {
						editFilter = cm.getFilter(filterId);
						Script editScript = cm.createScript();
						editScript.setType(type);
						editScript.setValue(script);

						if (editFilter != null) {
							if (name != null) {
								editFilter.setName(name);
								editFilter.setDescription(description);
								editFilter.setScript(editScript);
							}
							cm.updateFilter(editFilter);
						}

					} catch (ManagerException e) {
						errorMessages.add(stackToString(e));
					}
				}

				// user feedback
				if (editFilter != null) {
					req.setAttribute("highlightId", new Integer(editFilter.getId()));
					infoMessages.add("Edited Filter: " + name);
				} else {
					errorMessages.add("The Filter you are trying to edit no longer exists.");
				}
			}
		}
	}

	public String display(String state, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		req.setAttribute("scripts", SCRIPT_TYPES);
		if (state.equals("new")) {
			return FILTER_FOLDER + "new.jsp";
		} else if (state.equals("edit")) {
			Filter editFilter = null;
			int filterId = 0;

			try {
				filterId = Integer.parseInt(req.getParameter("id"));
			} catch (Exception e) {
			}

			if (filterId != 0) {
				try {
					Filter filter = cm.getFilter(filterId);
					req.setAttribute("filter", filter);
					if (filter.getScript().getType().equals("JavaScript"))
						req.setAttribute("javascriptValue", filter.getScript().getValue());
					else if (filter.getScript().getType().equals("python"))
						req.setAttribute("pythonValue", filter.getScript().getValue());
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
				
				return FILTER_FOLDER + "edit.jsp";
			} else {
				errorMessages.add("The request id " + req.getParameter("id") + " is invalid");
				return "/error.jsp";
			}
		} else if (state.equals("delete")) {
			int filterId = Integer.parseInt(req.getParameter("id"));
			if (filterId != 0) {
				try {
					req.setAttribute("filter", cm.getFilter(filterId));
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
			}
			return FILTER_FOLDER + "delete.jsp";
		} else if (state.equals("search")) {
			String searchParam = (String) req.getParameter("term");
			List searchList = null;
			try {
				searchList = cm.getFilterList();
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}

			if (searchParam == null || searchParam.equals("")) {
				req.setAttribute("filters", searchList);
				errorMessages.add("The search term is not valid.");
				return FILTER_FOLDER + "index.jsp";
			}

			ArrayList<Filter> finalList = new ArrayList<Filter>();

			int counter = 0;
			boolean match;

			for (int i = 0; i < searchList.size(); i++) {
				Filter check = (Filter) searchList.get(i);
				match = false;
				for (int j = 0; searchParam.length() + j <= check.getName().length(); j++) {
					if (check.getName().substring(j, j + searchParam.length()).toLowerCase().equals(searchParam.toLowerCase()))
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

			req.setAttribute("filters", finalList);
			infoMessages.add("Search Matched " + counter + " entries.");

			return FILTER_FOLDER + "search.jsp";
		} else {
			try {
				List filterList = cm.getFilterList();
				List channelList = cm.getChannelList();
				String usedFilters = "";
				for (int i = 0; i < channelList.size(); i++) {
					Channel channel = (Channel) channelList.get(i);
					for (int j = 0; j < filterList.size(); j++) {
						Filter filter = (Filter) filterList.get(j);
						if (!usedFilters.contains(String.valueOf(filter.getId()))) {
							if (channel.getFilterId().contains(String.valueOf(filter.getId()))) {
								usedFilters += String.valueOf(filter.getId()) + "-";
							}
						}
					}
				}
				req.setAttribute("filters", cm.getFilterList());
				req.setAttribute("usedFilters", usedFilters);
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}
			return FILTER_FOLDER + "index.jsp";
		}
	}

}
