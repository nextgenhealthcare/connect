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
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.managers.ManagerException;
import com.webreach.mirth.managers.types.mirth.Channel;
import com.webreach.mirth.managers.types.mirth.Endpoint;
import com.webreach.mirth.managers.types.mirth.Filter;
import com.webreach.mirth.managers.types.mirth.Transformer;

public class ChannelController extends MirthController {
	public static final String[] HL7_VERSIONS = { "All", "2.1", "2.2", "2.3", "2.4", "2.5", "3.0" };

	public void execute(String op, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String[] redirExclude = { "op", "state" };

		// If an execution was specified
		if (op != null) {
			if (op.equals("new") || op.equals("clone")) {
				Channel newChannel = null;

				int sourceEndpoint = 0;
				int destinationEndpoints[] = { 0 };
				int filterIds[] = { 0 };
				int transformerIds[] = { 0 };

				int numErrors = 0;

				// User input
				String name = req.getParameter("name");
				String hl7 = req.getParameter("hl7");
				String deployed = req.getParameter("deployed");
				String description = req.getParameter("description");
				String direction = req.getParameter("direction");
				String inEndpoint = req.getParameter("inEndpoint");
				String outEndpoints[] = req.getParameterValues("outEndpoints");
				String filters[] = req.getParameterValues("filters");
				String transformers[] = req.getParameterValues("transformers");

				if (name != null)
					name = name.trim();

				// Validation of user input
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

				try {
					sourceEndpoint = Integer.parseInt(inEndpoint);
				} catch (Exception e) {
					errorMessages.add("The requested Source Endpoint is invalid.");
					numErrors++;
				}

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
				
				if (filters == null) {
					errorMessages.add("The requested filter does not exist.");
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

				if (transformers == null) {
					errorMessages.add("The requested transformer does not exist.");
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

				for (int i = 0; i < destinationEndpoints.length; i++) {
					if (destinationEndpoints[i] == sourceEndpoint) {
						errorMessages.add("Inbound and Outbound Endpoints cannot be the same.");
						numErrors++;
						break;
					}
				}

				// If input is invalid, halt execution
				if (numErrors > 0) {
					res.sendRedirect("/channel/?state=new&" + parametersAsQuery(req, redirExclude));
					return;
				}

				// Create the Channel
				try {
					newChannel = cm.createChannel();

					// If the channel exists:
					if (newChannel != null) {
						newChannel.setName(name);
						newChannel.setEncoding(hl7);
						newChannel.setDescription(description);
						newChannel.setOutbound(direction.equals("outbound"));
						newChannel.setSourceEndpointId(sourceEndpoint);
						newChannel.setDestinationEndpointId(idsToString(destinationEndpoints));
						newChannel.setFilterId(idsToString(filterIds));
						newChannel.setTransformerId(idsToString(transformerIds));

						if (deployed != null && deployed.equals("1")) {
							newChannel.setEnabled(true);
						} else {
							newChannel.setEnabled(false);
						}

						cm.addChannel(newChannel);
						// sm.setIsChannelRunning(newChannel.getId(), false);
					}
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}

				// Check if the Channel is created.
				if (newChannel != null) {
					req.setAttribute("highlightId", new Integer(newChannel.getId()));
					infoMessages.add("Added Channel: " + name);
				} else {
					errorMessages.add("A database error has occured; could not create the new Channel: " + name);
				}
			} else if (op.equals("delete")) {
				Channel deleteChannel = null;
				String name = "";
				int channelId = 0;

				// Validate
				try {
					channelId = Integer.parseInt(req.getParameter("id"));
				} catch (Exception e) {
					errorMessages.add("There was an error in trying to delete the requested Channel.");
					return;
				}

				// Delete the Channel
				if (channelId != 0) {
					try {
						deleteChannel = cm.getChannel(channelId);
						if (deleteChannel != null) {
							name = deleteChannel.getName();
							cm.removeChannel(channelId);
						}
					} catch (ManagerException e) {
						errorMessages.add(stackToString(e));
					}
				}

				// Notify the user if the action is complete
				if (deleteChannel != null) {
					infoMessages.add("Deleted Channel: " + name);
				} else {
					errorMessages.add("Channel has already been deleted.");
				}
			} else if (op.equals("edit")) {
				Channel editChannel = null;

				int channelId = 0;
				int sourceEndpoint = 0;
				int destinationEndpoints[] = { 0 };
				int filterIds[] = { 0 };
				int transformerIds[] = { 0 };

				int numErrors = 0;

				// User input
				String id = req.getParameter("id");
				String name = req.getParameter("name");
				String hl7 = req.getParameter("hl7");
				String description = req.getParameter("description");
				String deployed = req.getParameter("deployed");
				String direction = req.getParameter("direction");
				String inEndpoint = req.getParameter("inEndpoint");
				String outEndpoints[] = req.getParameterValues("outEndpoints");
				String filters[] = req.getParameterValues("filters");
				String transformers[] = req.getParameterValues("transformers");

				if (name != null)
					name = name.trim();

				try {
					channelId = Integer.parseInt(id);
				} catch (Exception e) {
					errorMessages.add("There was an error trying to edit the requested Channel. Please try agian.");
					res.sendRedirect("/channel/");
					return;
				}

				// Validation of user input
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
						if (channelId != check.getId()) {
							errorMessages.add(name + " already exists. ");
							numErrors++;
						}
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

				try {
					sourceEndpoint = Integer.parseInt(inEndpoint);
				} catch (Exception e) {
					errorMessages.add("The requested Source Endpoint is invalid. Please try again.");
					numErrors++;
				}

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
				
				if (filters == null) {
					errorMessages.add("The requested filter does not exist. Please try again.");
					numErrors++;
				} else {
					try {
						filterIds = new int[filters.length];
						for (int i = 0; i < filters.length; i++) {
							filterIds[i] = Integer.parseInt(filters[i]);
						}
					} catch (Exception e) {
						errorMessages.add("Filter is invalid.");
						numErrors++;
					}
				}

				if (transformers == null) {
					errorMessages.add("The requested transformer does not exist. Please try agian.");
					numErrors++;
				} else {
					try {
						transformerIds = new int[transformers.length];
						for (int i = 0; i < transformers.length; i++) {
							transformerIds[i] = Integer.parseInt(transformers[i]);
						}
					} catch (NumberFormatException e) {
						errorMessages.add("Transformer is invalid.");
						numErrors++;
					}
				}

				for (int i = 0; i < destinationEndpoints.length; i++) {
					if (destinationEndpoints[i] == sourceEndpoint) {
						errorMessages.add("Inbound and Outbound Endpoints cannot be the same.");
						numErrors++;
						break;
					}
				}

				// If input is invalid, halt execution
				if (numErrors > 0) {
					res.sendRedirect("/channel/?state=edit&" + parametersAsQuery(req, redirExclude));
					return;
				}

				try {
					editChannel = cm.getChannel(channelId);

					// If the channel exists:
					if (editChannel != null) {
						editChannel.setName(name);
						editChannel.setEncoding(hl7);
						editChannel.setDescription(description);
						editChannel.setOutbound(direction.equals("outbound"));
						editChannel.setSourceEndpointId(sourceEndpoint);
						editChannel.setDestinationEndpointId(idsToString(destinationEndpoints));
						editChannel.setFilterId(idsToString(filterIds));
						editChannel.setTransformerId(idsToString(transformerIds));

						if (deployed != null) {
							if (deployed.equals("1")) {
								editChannel.setEnabled(true);
							} else {
								editChannel.setEnabled(false);
							}
						} else {
							editChannel.setEnabled(false);
						}

						cm.updateChannel(editChannel);
					}
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}

				if (editChannel != null) {
					req.setAttribute("highlightId", new Integer(editChannel.getId()));
					infoMessages.add("Edited Channel: " + name);
				} else {
					errorMessages.add("The channel you are trying to edit no longer exists.");
				}
			}
		}
	}

	public String display(String state, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setAttribute("hl7Versions", HL7_VERSIONS);

		if (state.equals("new")) {
			try {
				req.setAttribute("endpoints", cm.getEndpointList());
				req.setAttribute("filtersHash", (Hashtable<Integer, Filter>) filterListToHash(cm.getFilterList()));
				req.setAttribute("filters", cm.getFilterList());
				req.setAttribute("transformers", cm.getTransformerList());
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}
			return CHANNEL_FOLDER + "new.jsp";
		} else if (state.equals("edit")) {
			Channel editChannel = null;
			int channelId = 0;
			
			channelId = Integer.parseInt(req.getParameter("id"));

			if (channelId != 0) {
				try {
					editChannel = cm.getChannel(channelId);
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
			}

			if (editChannel != null) {
				try {
					ArrayList<Filter> usedFilters = new ArrayList<Filter>();
					ArrayList<Transformer> usedTransformers = new ArrayList<Transformer>();
					ArrayList<Endpoint> usedOutboundEndpoints = new ArrayList<Endpoint>();
					
					int filterIdList[] = stringToIds(editChannel.getFilterId());
					int transformerIdList[] = stringToIds(editChannel.getTransformerId());
					int outboundEndpointIdList[] = stringToIds(editChannel.getDestinationEndpointId());

					for (int i = 0; i < filterIdList.length; i++) {
						usedFilters.add(cm.getFilter(filterIdList[i]));
					}
					
					for (int i = 0; i < transformerIdList.length; i++) {
						usedTransformers.add(cm.getTransformer(transformerIdList[i]));
					}

					for (int i = 0; i < outboundEndpointIdList.length; i++) {
						usedOutboundEndpoints.add(cm.getEndpoint(outboundEndpointIdList[i]));
					}

					req.setAttribute("channel", editChannel);
					
					req.setAttribute("currentFilters", usedFilters);
					req.setAttribute("currentTransformers", usedTransformers);
					req.setAttribute("currentOutboundEndpoints", usedOutboundEndpoints);

					req.setAttribute("filters", cm.getFilterList());
					req.setAttribute("transformers", cm.getTransformerList());
					req.setAttribute("endpoints", cm.getEndpointList());
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
				
				return CHANNEL_FOLDER + "edit.jsp";
			} else {
				errorMessages.add("The channel you are trying to edit has been deleted or has not yet been created.");
				return display("index", req, res);
			}
		} else if (state.equals("clone")) {
			Channel cloneChannel = null;
			int channelId = 0;

			try {
				channelId = Integer.parseInt(req.getParameter("id"));
			} catch (Exception e) {
			}

			if (channelId != 0) {
				try {
					cloneChannel = cm.getChannel(channelId);
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
			}

			if (cloneChannel != null) {
				try {
					ArrayList curFilters = new ArrayList();
					ArrayList curTransformers = new ArrayList();
					int filterIdList[] = stringToIds(cloneChannel.getFilterId());
					int transformerIdList[] = stringToIds(cloneChannel.getTransformerId());
					for (int i = 0; i < filterIdList.length; i++) {
						curFilters.add(cm.getFilter(filterIdList[i]));
					}
					for (int i = 0; i < transformerIdList.length; i++) {
						curTransformers.add(cm.getTransformer(transformerIdList[i]));
					}
					req.setAttribute("channel", cloneChannel);
					req.setAttribute("endpoints", cm.getEndpointList());
					req.setAttribute("currentFilters", curFilters);
					req.setAttribute("currentTransformers", curTransformers);
					req.setAttribute("filters", cm.getFilterList());
					req.setAttribute("transformers", cm.getTransformerList());
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
				return CHANNEL_FOLDER + "clone.jsp";
			} else {
				errorMessages.add("The request id " + req.getParameter("id") + " is invalid");
				return "/error.jsp";
			}
		} else if (state.equals("search")) {
			String searchParam = (String) req.getParameter("term");
			List searchList = null;
			try {
				searchList = cm.getChannelList();
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}
			if (searchParam == null || searchParam.equals("")) {
				req.setAttribute("channels", searchList);
				errorMessages.add("The search term is not valid.");
				return CHANNEL_FOLDER + "index.jsp";
			}

			ArrayList<Channel> finalList = new ArrayList<Channel>();
			int counter = 0;
			boolean match;

			for (int i = 0; i < searchList.size(); i++) {
				Channel check = (Channel) searchList.get(i);
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

			req.setAttribute("channels", finalList);
			infoMessages.add("Search Matched " + counter + " entries.");

			return CHANNEL_FOLDER + "search.jsp";
		} else {
			try {
				req.setAttribute("channels", cm.getChannelList());
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}
			return CHANNEL_FOLDER + "index.jsp";
		}
	}

}
