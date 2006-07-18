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
import com.webreach.mirth.managers.types.MirthProperty;
import com.webreach.mirth.managers.types.MirthPropertyType;
import com.webreach.mirth.managers.types.mirth.Channel;
import com.webreach.mirth.managers.types.mirth.Endpoint;

public class EndpointController extends MirthController {
	public static final String[] ENDPOINT_DIRECTIONS = { "inbound", "outbound" };

	public void execute(String op, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String[] redirExclude = { "op", "state" };
		
		// if an execution was specified
		if (op != null) {
			if (op.equals("new")) {
				Endpoint newEndpoint = null;

				int numErrors = 0;

				// user input from the form
				String name = req.getParameter("name");
				String description = req.getParameter("description");
				String type = req.getParameter("type");

				// trim the endpoint name of any whitespace
				if (name != null) {
					name = name.trim();
				}
				
				// set the description to blank if it was not set
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
					searchList = cm.getEndpointList();
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
				
				for (int i = 0; i < searchList.size(); i++) {
					Endpoint check = (Endpoint) searchList.get(i);
					
					if (check.getName().toLowerCase().equals(name.toLowerCase())) {
						errorMessages.add(name + " already exists.");
						numErrors++;
					}
				}

				// create the endpoint
				try {
					newEndpoint = cm.createEndpoint();

					if (newEndpoint != null) {
						newEndpoint.setName(name);
						newEndpoint.setDescription(description);
						newEndpoint.setType(type);
						
						ArrayList<MirthProperty> properties = pm.getProperties("endpoint", type);
						ArrayList<String> values = new ArrayList<String>();

						for (int i = 0; i < properties.size(); i++) {
							values.add(req.getParameter(type + "_" + properties.get(i).getName()));
							cm.addMirthProperty(newEndpoint.getProperties().getProperty(), properties.get(i).getName(), values.get(i));
						}

						FormValidator fm = new FormValidator();
						
						if (!fm.validate(properties, values, errorMessages)) {
							numErrors++;
						}

						// If input is invalid, halt execution
						if (numErrors > 0) {
							res.sendRedirect("/endpoint/?state=new&" + parametersAsQuery(req, redirExclude));
							
							return;
						}

						// Add new endpoint
						cm.addEndpoint(newEndpoint);
					}
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				} catch (Exception e) {
					errorMessages.add(stackToString(e));
				}

				// Check if the endpoint was created.
				if (newEndpoint != null) {
					infoMessages.add("Added Endpoint: " + newEndpoint.getName());
					req.setAttribute("highlightId", new Integer(newEndpoint.getId()));

					String origin = req.getParameter("orig");
					if (origin != null && !origin.trim().equals("")) {
						res.sendRedirect(origin + newEndpoint.getId());
					}
				} else {
					errorMessages.add("Failed to create new endpoint: " + name);
				}
			} else if (op.equals("delete")) {
				// Execute delete command
				Endpoint deleteEndpoint = null;
				String name = "";
				int endpointId = 0;

				// Validate
				try {
					endpointId = Integer.parseInt(req.getParameter("id"));
				} catch (Exception e) {
					errorMessages.add("There was an error in trying to delete the requested Endpiont. Please try again.");
					return;
				}

				// Delete the Endpoint
				if (endpointId != 0) {
					try {
						deleteEndpoint = cm.getEndpoint(endpointId);
						if (deleteEndpoint != null) {
							name = deleteEndpoint.getName();
							cm.removeEndpoint(endpointId);
						}
					} catch (ManagerException e) {
						errorMessages.add(stackToString(e));
					}
				}

				if (deleteEndpoint != null) {
					infoMessages.add("Deleted Endpoint: " + name);
				} else {
					errorMessages.add("Endpoint has already been deleted.");
				}
			} else if (op.equals("edit")) {
				Endpoint editEndpoint = null;
				int numErrors = 0;

				int endpointId = Integer.parseInt(req.getParameter("id"));
				String name = req.getParameter("name").trim();

				// description not required
				String description = req.getParameter("description");
				if (description == null)
					description = "";

				String type = req.getParameter("type");

				// Validation of user input
				if (!isValidName(name)) {
					errorMessages.add("\"" + name + "\" is an invalid name. Please use only alphanumeric characters or the following symbols " + VALID_NAME_SYMBOLS);
					numErrors++;
				}
				// Test for duplicate names
				List searchList = null;
				try {
					searchList = cm.getEndpointList();
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
				for (int i = 0; i < searchList.size(); i++) {
					Endpoint check = (Endpoint) searchList.get(i);
					if (check.getName().toLowerCase().equals(name.toLowerCase())) {
						if (endpointId != check.getId()) {
							errorMessages.add(name + " already exists. ");
							numErrors++;
						}
					}
				}

				if (endpointId != 0) {
					try {
						editEndpoint = cm.createEndpoint();
						editEndpoint.setId(endpointId);

						if (editEndpoint != null) {
							if (name != null && type != null) {
								editEndpoint.setName(name);
								editEndpoint.setDescription(description);
								editEndpoint.setType(type);
								editEndpoint.setDescription(description);

								ArrayList<MirthProperty> properties = pm.getProperties("endpoint", type);
								ArrayList<String> values = new ArrayList<String>();

								for (int i = 0; i < properties.size(); i++) {
									values.add(req.getParameter(type + "_" + properties.get(i).getName()));
									cm.addMirthProperty(editEndpoint.getProperties().getProperty(), properties.get(i).getName(), values.get(i));
								}

								FormValidator fm = new FormValidator();
								if (!fm.validate(properties, values, errorMessages))
									numErrors++;

							}

							// If input is invalid, halt execution
							if (numErrors > 0) {
								res.sendRedirect("/endpoint/?state=edit&" + parametersAsQuery(req, redirExclude));
								return;
							}

							if (numErrors == 0) {
								cm.updateEndpoint(editEndpoint);
							}
								
						}

					} catch (ManagerException e) {
						errorMessages.add(stackToString(e));
					}
				}

				// User Feedback
				if (editEndpoint != null) {
					req.setAttribute("highlightId", new Integer(editEndpoint.getId()));
					infoMessages.add("Edited Endpoint: " + name);
				} else {
					errorMessages.add("The endpoint you are trying to edit no longer exists.");
				}
			}
		}
	}

	public String display(String state, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		ArrayList<MirthPropertyType> types = pm.getTypes("endpoint");
		
		if (state.equals("new")) {
			req.setAttribute("types", types);
			
			return ENDPOINT_FOLDER + "new.jsp";
		} else if (state.equals("edit")) {
			int endpointId = 0;
			
			try {
				endpointId = Integer.parseInt(req.getParameter("id"));
			} catch (Exception e) {
				
			}
			
			if (endpointId != 0) {
				try {
					Endpoint editEndpoint = null;
					editEndpoint = cm.getEndpoint(endpointId);
					ArrayList<MirthProperty> properties = pm.getProperties("endpoint", editEndpoint.getType());
					
					for (int i = 0; i < properties.size(); i++) {
						req.setAttribute(editEndpoint.getType() + "_" + properties.get(i).getName(), cm.getPropertyValue(editEndpoint.getProperties().getProperty(), properties.get(i).getName()));
					}

					req.setAttribute("types", types);
					req.setAttribute("endpoint", editEndpoint);

				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
				return ENDPOINT_FOLDER + "edit.jsp";
			} else {
				errorMessages.add("The request id " + req.getParameter("id") + " is invalid");
				return display("index", req, res);
			}
		} else if (state.equals("delete")) {
			int endpointId = Integer.parseInt(req.getParameter("id"));
			if (endpointId != 0) {
				try {
					req.setAttribute("endpoint", cm.getEndpoint(endpointId));
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
			}
			return ENDPOINT_FOLDER + "delete.jsp";
		} else if (state.equals("search")) {
			String searchParam = (String) req.getParameter("term");
			List searchList = null;
			
			try {
				searchList = cm.getEndpointList();
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}

			if (searchParam.equals("") || searchParam == null) {
				req.setAttribute("endpoints", searchList);
				errorMessages.add("The search term is not valid.");
				return ENDPOINT_FOLDER + "index.jsp";
			}

			ArrayList<Endpoint> finalList = new ArrayList<Endpoint>();

			int counter = 0;
			boolean match;

			for (int i = 0; i < searchList.size(); i++) {
				Endpoint check = (Endpoint) searchList.get(i);
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
			
			req.setAttribute("endpoints", finalList);
			infoMessages.add("Search Matched " + counter + " entries.");
			
			return ENDPOINT_FOLDER + "search.jsp";
		} else {
			try {
				List endpointList = cm.getEndpointList();
				List channelList = cm.getChannelList();
				String usedEndpoints = "";
				
				for (int i = 0; i < channelList.size(); i++) {
					Channel channel = (Channel) channelList.get(i);
					
					for (int j = 0; j < endpointList.size(); j++) {
						Endpoint endpoint = (Endpoint) endpointList.get(j);

						if (!usedEndpoints.contains(String.valueOf(endpoint.getId()))) {
							if (channel.getSourceEndpointId() == endpoint.getId() || channel.getDestinationEndpointId().contains(String.valueOf(endpoint.getId()))) {
								usedEndpoints += String.valueOf(endpoint.getId()) + "-";
							}
						}
					}
				}
				req.setAttribute("endpoints", endpointList);
				req.setAttribute("usedEndpoints", usedEndpoints);
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}
			
			return ENDPOINT_FOLDER + "index.jsp";
		}
	}

}
