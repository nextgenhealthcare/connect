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
import javax.servlet.http.HttpSession;

import com.webreach.mirth.managers.ManagerException;
import com.webreach.mirth.managers.types.mirth.Channel;
import com.webreach.mirth.managers.types.mirth.Endpoint;
import com.webreach.mirth.managers.types.mirth.Filter;
import com.webreach.mirth.managers.types.mirth.Transformer;

public class ChannelWizardController extends MirthController {
	public static final String[] HL7_VERSIONS = { "All", "2.1", "2.2", "2.3", "2.4", "2.5", "3.0" };
	public static int channelId = 0;
	public static int sourceEndpoint = 0;
	public static int destinationEndpoint = 0;
	public static int filterId = 0;
	public static int transformerId = 0;
	public static String name = "";
	public static String hl7 = "";
	public static String deployed = "";
	public static String inEndpoint = "";
	public static String outEndpoint = "";
	public static String filter = "";
	public static String transformer = "";
	public static Channel newChannel = null;

	public void execute(String op, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String[] redirExclude = { "op", "state", "next", "previous", "preview" };

		// Setup a wizard channel
		HttpSession sess = req.getSession(true);
		Channel channel = (Channel) sess.getAttribute("wizardChannel");
		Integer lastState = (Integer) sess.getAttribute("wizardLastState");
		if (channel == null || (op != null && op.equals("new"))) {
			try {
				channel = cm.createChannel();
				sess.setAttribute("wizardChannel", channel);
				sess.setAttribute("wizardLastState", new Integer(0));
			} catch (ManagerException e) {
				errorMessages.add("Error accessing database.");
				return;
			}
		}
		if (lastState == null) {
			lastState = new Integer(0);
			sess.setAttribute("wizardLastState", lastState);
		}

		if (op != null) {
			int numErrors = 0;

			if (op.equals("basics")) {
				// Get user input
				String name = req.getParameter("name");
				String hl7 = req.getParameter("hl7");
				String deployed = req.getParameter("deployed");
				String direction = req.getParameter("direction");

				if (name != null)
					name = name.trim();

				// Validation
				if (!isValidName(name)) {
					errorMessages.add("\"" + name + "\" is an invalid name. Please use only alphanumeric characters or the following symbols " + VALID_NAME_SYMBOLS);
					numErrors++;
				}

				// Test for duplicate names
				List searchList = null;
				try {
					searchList = cm.getChannelList();
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
				for (int i = 0; i < searchList.size(); i++) {
					Channel check = (Channel) searchList.get(i);
					if (check.getName().toLowerCase().equals(name.toLowerCase())) {
						errorMessages.add(name + " already exists. ");
						numErrors++;
					}
				}

				if (!isArrayContain(HL7_VERSIONS, hl7)) {
					errorMessages.add("Invalid HL7 version");
					numErrors++;
				}

				if (direction == null) {
					errorMessages.add("Direction is invalid");
					numErrors++;
				}

				// If input is invalid, halt execution
				if (numErrors > 0) {
					res.sendRedirect("/wizard/?state=basics&" + parametersAsQuery(req, redirExclude));
					return;
				}

				channel.setName(name);
				channel.setEncoding(hl7);
				channel.setOutbound(direction.equals("outbound"));

				if (deployed != null && deployed.equals("1")) {
					channel.setEnabled(true);
				} else {
					channel.setEnabled(false);
				}
			} else if (op.equals("inbound")) {
				int sourceEndpoint = 0;

				// User input
				String inEndpoint = req.getParameter("inEndpoint");

				// Validation
				try {
					sourceEndpoint = Integer.parseInt(inEndpoint);
				} catch (Exception e) {
					errorMessages.add("The requested Source Endpoint is invalid. Please try again.");
					numErrors++;
				}

				// If input is invalid, halt execution
				if (numErrors > 0) {
					res.sendRedirect("/wizard/?state=inbound&" + parametersAsQuery(req, redirExclude));
					return;
				}

				// Add endpoint
				channel.setSourceEndpointId(sourceEndpoint);
			} else if (op.equals("filters")) {
				int filterIds[] = new int[0];

				// User input
				String filters[] = req.getParameterValues("filters");

				// Validation
				if (filters == null) {
					errorMessages.add("The requested filter does not exist. Please try again.");
					numErrors++;
				} else {
					try {
						if (filters.length > 0) {
							filterIds = new int[filters.length];
							for (int i = 0; i < filters.length; i++) {
								filterIds[i] = Integer.parseInt(filters[i]);
							}
						}
					} catch (Exception e) {
						errorMessages.add("Filter is invalid.");
						numErrors++;
					}
				}

				// If input is invalid, halt execution
				if (numErrors > 0) {
					res.sendRedirect("/wizard/?state=filters&" + parametersAsQuery(req, redirExclude));
					return;
				}

				// Add filter
				channel.setFilterId(idsToString(filterIds));
			} else if (op.equals("transformers")) {
				int transformerIds[] = { 0 };

				// User input
				String transformers[] = req.getParameterValues("transformers");

				// Validation
				if (transformers == null) {
					errorMessages.add("The requested transformer does not exist. Please try agian.");
					numErrors++;
				} else {
					try {
						if (transformers.length > 0) {
							transformerIds = new int[transformers.length];
							for (int i = 0; i < transformers.length; i++) {
								transformerIds[i] = Integer.parseInt(transformers[i]);
							}
						}
					} catch (NumberFormatException e) {
						errorMessages.add("Transformer is invalid.");
						numErrors++;
					}
				}

				// If input is invalid, halt execution
				if (numErrors > 0) {
					res.sendRedirect("/wizard/?state=transformers&" + parametersAsQuery(req, redirExclude));
					return;
				}

				// Add transformer
				channel.setTransformerId(idsToString(transformerIds));
			} else if (op.equals("outbound")) {
				int destinationEndpoints[] = { 0 };

				// User input
				String outEndpoints[] = req.getParameterValues("outEndpoints");

				if (outEndpoints == null) {
					errorMessages.add("The requested endpoint does not exist.");
					numErrors++;
				} else {
					try {
						if (outEndpoints.length > 0) {
							destinationEndpoints = new int[outEndpoints.length];
							for (int i = 0; i < outEndpoints.length; i++) {
								destinationEndpoints[i] = Integer.parseInt(outEndpoints[i]);
							}
						}
					} catch (Exception e) {
						errorMessages.add("Endpoint is invalid.");
						numErrors++;
					}
				}

				// If input is invalid, halt execution
				if (numErrors > 0) {
					res.sendRedirect("/wizard/?state=outbound&" + parametersAsQuery(req, redirExclude));
					return;
				}

				// Add outbound
				channel.setDestinationEndpointId(idsToString(destinationEndpoints));
			} else if (op.equals("preview")) {
				try {
					cm.addChannel(channel);

					sess.setAttribute("wizardChannel", null);
					res.sendRedirect("/channel/?state=index&id=" + channel.getId());
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
			}
		}
	}

	public String display(String state, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		boolean goNext = req.getParameter("next") != null;
		boolean goPrev = req.getParameter("previous") != null;
		boolean goPreview = req.getParameter("preview") != null;

		if (goPreview) {
			return displayPage("preview", req, res);
		} else {
			if (state.equals("inbound")) {
				if (goNext) {
					return displayPage("filters", req, res);
				} else if (goPrev) {
					return displayPage("basics", req, res);
				} else {
					return displayPage("inbound", req, res);
				}
			} else if (state.equals("filters")) {
				if (goNext) {
					return displayPage("transformers", req, res);
				} else if (goPrev) {
					return displayPage("inbound", req, res);
				} else {
					return displayPage("filters", req, res);
				}
			} else if (state.equals("transformers")) {
				if (goNext) {
					return displayPage("outbound", req, res);
				} else if (goPrev) {
					return displayPage("filters", req, res);
				} else {
					return displayPage("transformers", req, res);
				}
			} else if (state.equals("outbound")) {
				if (goNext) {
					return displayPage("preview", req, res);
				} else if (goPrev) {
					return displayPage("transformers", req, res);
				} else {
					return displayPage("outbound", req, res);
				}
			} else if (state.equals("preview")) {
				if (goNext) {
					return displayPage("preview", req, res);
				} else if (goPrev) {
					return displayPage("outbound", req, res);
				} else {
					return displayPage("preview", req, res);
				}
			} else {
				if (goNext) {
					return displayPage("inbound", req, res);
				} else {
					return displayPage("basics", req, res);
				}
			}
		}
	}

	private String displayPage(String state, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpSession sess = req.getSession(true);
		Channel channel = (Channel) sess.getAttribute("wizardChannel");
		req.setAttribute("channel", channel);

		// Update lastState
		Integer lastState = (Integer) sess.getAttribute("wizardLastState");
		ArrayList<String> states = new ArrayList<String>();
		states.add("basics");
		states.add("inbound");
		states.add("filters");
		states.add("transformers");
		states.add("outbound");
		states.add("preview");

		Integer currentState = new Integer(states.indexOf(state));

		if (lastState.compareTo(currentState) < 0) {
			lastState = currentState;
			sess.setAttribute("wizardLastState", lastState);
		}
		req.setAttribute("lastState", lastState);

		if (state.equals("basics")) {
			req.setAttribute("hl7Versions", HL7_VERSIONS);
			return WIZARD_FOLDER + "basics.jsp";
		} else if (state.equals("inbound")) {
			try {
				req.setAttribute("endpoints", cm.getEndpointList());
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}
			return WIZARD_FOLDER + "inbound.jsp";
		} else if (state.equals("filters")) {
			String newFilter = req.getParameter("newFilter");
			int newFilterId = 0;
			if (newFilter != null) {
				try {
					newFilterId = Integer.parseInt(newFilter);
					channel.setFilterId((channel.getFilterId() + " " + newFilterId).trim());
				} catch (Exception e) {
				}
			}
			int[] filterIds = stringToIds(channel.getFilterId());

			try {
				ArrayList<Filter> remainingFilters = new ArrayList<Filter>(cm.getFilterList());
				ArrayList<Filter> selectedFilters = new ArrayList<Filter>();
				Filter currentFilter = null;

				for (int i = 0; i < filterIds.length; i++) {
					currentFilter = cm.getFilter(filterIds[i]);
					if (currentFilter != null) {
						selectedFilters.add(currentFilter);
						remainingFilters.remove(currentFilter);
					}
				}

				req.setAttribute("selectedFilters", selectedFilters);
				req.setAttribute("unselectedFilters", remainingFilters);

				req.setAttribute("filters", cm.getFilterList());
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}
			return WIZARD_FOLDER + "filters.jsp";
		} else if (state.equals("transformers")) {
			String newTransformer = req.getParameter("newTransformer");
			int newTransformerId = 0;
			if (newTransformer != null) {
				try {
					newTransformerId = Integer.parseInt(newTransformer);
					channel.setTransformerId((channel.getTransformerId() + " " + newTransformerId).trim());
				} catch (Exception e) {
				}
			}
			int[] transformerIds = stringToIds(channel.getTransformerId());

			try {
				ArrayList<Transformer> remainingTransformers = new ArrayList<Transformer>(cm.getTransformerList());
				ArrayList<Transformer> selectedTransformers = new ArrayList<Transformer>();
				Transformer currentTransformer = null;

				for (int i = 0; i < transformerIds.length; i++) {
					currentTransformer = cm.getTransformer(transformerIds[i]);
					if (currentTransformer != null) {
						selectedTransformers.add(currentTransformer);
						remainingTransformers.remove(currentTransformer);
					}
				}

				req.setAttribute("selectedTransformers", selectedTransformers);
				req.setAttribute("unselectedTransformers", remainingTransformers);
				req.setAttribute("transformers", cm.getTransformerList());
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}
			return WIZARD_FOLDER + "transformers.jsp";
		} else if (state.equals("outbound")) {
			
			int[] outboundEndpointIds = stringToIds(channel.getDestinationEndpointId());

			try {
				ArrayList<Endpoint> unseletecEndpoints = new ArrayList<Endpoint>(cm.getEndpointList());
				ArrayList<Endpoint> selectedEndpoints = new ArrayList<Endpoint>();
				Endpoint currentEndpoint = null;

				for (int i = 0; i < outboundEndpointIds.length; i++) {
					currentEndpoint = cm.getEndpoint(outboundEndpointIds[i]);
					
					if (currentEndpoint != null) {
						selectedEndpoints.add(currentEndpoint);
						unseletecEndpoints.remove(currentEndpoint);
					}
				}

				req.setAttribute("selectedEndpoints", selectedEndpoints);
				req.setAttribute("unselectedEndpoints", unseletecEndpoints);
				req.setAttribute("endpoints", cm.getEndpointList());
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}
			return WIZARD_FOLDER + "outbound.jsp";
		} else if (state.equals("preview")) {
			int[] filterIds = stringToIds(channel.getFilterId());
			int[] transformerIds = stringToIds(channel.getTransformerId());
			int[] outboundEndpointIds = stringToIds(channel.getDestinationEndpointId());
			
			try {
				ArrayList<Transformer> selectedTransformers = new ArrayList<Transformer>();
				ArrayList<Filter> selectedFilters = new ArrayList<Filter>();
				ArrayList<Endpoint> selectedEndpoints = new ArrayList<Endpoint>();

				Filter currentFilter = null;
				Transformer currentTransformer = null;
				Endpoint currentEndpoint = null;

				for (int i = 0; i < filterIds.length; i++) {
					currentFilter = cm.getFilter(filterIds[i]);
					if (currentFilter != null) {
						selectedFilters.add(currentFilter);
					}
				}

				for (int i = 0; i < transformerIds.length; i++) {
					currentTransformer = cm.getTransformer(transformerIds[i]);
					if (currentTransformer != null) {
						selectedTransformers.add(currentTransformer);
					}
				}

				for (int i = 0; i < outboundEndpointIds.length; i++) {
					currentEndpoint = cm.getEndpoint(outboundEndpointIds[i]);
					if (currentEndpoint != null) {
						selectedEndpoints.add(currentEndpoint);
					}
				}

				Endpoint inboundEndpoint = cm.getEndpoint(channel.getSourceEndpointId());

				req.setAttribute("inboundEndpoint", inboundEndpoint);
				req.setAttribute("selectedTransformers", selectedTransformers);
				req.setAttribute("selectedFilters", selectedFilters);
				req.setAttribute("selectedEndpoints", selectedEndpoints);
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}
			return WIZARD_FOLDER + "preview.jsp";
		} else {
			return WIZARD_FOLDER + "edit.jsp";
		}

	}

}
