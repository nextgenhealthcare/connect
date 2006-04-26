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
import com.webreach.mirth.managers.types.mirth.Script;
import com.webreach.mirth.managers.types.mirth.Transformer;

public class TransformerController extends MirthController {

	public static final String[] SCRIPT_TYPES = { "JavaScript", "Java", "Python", "XSLT" };

	public void execute(String op, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String[] redirExclude = { "op", "state" };
		int numErrors = 0;

		// if an execution was specified
		if (op != null) {

			// new
			if (op.equals("new")) {
				Transformer newTransformer = null;
				Script newScript = null;

				String name = req.getParameter("name");
				String description = req.getParameter("description");
				String type = req.getParameter("script");
				String script = req.getParameter("scriptString");

				if (name != null) {
					name = name.trim();
				}

				if (!isValidName(name)) {
					errorMessages.add("\"" + name + "\" is an invalid name. Please use only alphanumeric characters or the following symbols " + VALID_NAME_SYMBOLS);
					numErrors++;
				}

				// check for duplicate names
				List searchList = null;
				
				try {
					searchList = cm.getTransformerList();
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
				
				for (int i = 0; i < searchList.size(); i++) {
					Transformer check = (Transformer) searchList.get(i);
					
					if (check.getName().toLowerCase().equals(name.toLowerCase())) {
						errorMessages.add(name + " already exists. ");
						numErrors++;
					}
				}

				if (script == null || script.equals("")) {
					errorMessages.add("A script must be entered.");
					numErrors++;
				}

				if (numErrors > 0) {
					res.sendRedirect("/transformer/?state=new&" + parametersAsQuery(req, redirExclude));
					return;
				}

				// create the transformer
				try {
					newScript = cm.createScript();
					newScript.setType(type);
					newScript.setValue(script);

					newTransformer = cm.createTransformer();
					newTransformer.setName(name);
					newTransformer.setDescription(description);
					newTransformer.setScript(newScript);

					// add new transformer
					cm.addTransformer(newTransformer);
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}

				if (newTransformer != null) {
					infoMessages.add("Added Transformer: " + newTransformer.getName());
					req.setAttribute("highlightId", new Integer(newTransformer.getId()));
					String origin = req.getParameter("orig");
					
					if (origin != null && !origin.trim().equals("")) {
						res.sendRedirect(origin + newTransformer.getId());
					}
				} else {
					errorMessages.add("Failed to create new Transformer: " + name);
				}
			} else if (op.equals("delete")) {
				// execute delete command
				Transformer deleteTransformer = null;
				String name = "";

				int transformerId = Integer.parseInt(req.getParameter("id"));

				if (transformerId != 0) {
					try {
						deleteTransformer = cm.getTransformer(transformerId);
						if (deleteTransformer != null) {
							name = deleteTransformer.getName();
							cm.removeTransformer(transformerId);
						}
					} catch (ManagerException e) {
						errorMessages.add(stackToString(e));
					}
				}

				if (deleteTransformer != null) {
					infoMessages.add("Deleted Transformer: " + name);
				} else {
					errorMessages.add("Transformer has already been deleted.");
				}
			} else if (op.equals("edit")) {
				Transformer editTransformer = null;

				int transformerId = Integer.parseInt(req.getParameter("id"));
				String name = req.getParameter("name").trim();
				String description = req.getParameter("description");
				String type = req.getParameter("script");
				String script = req.getParameter("scriptString");

				if (transformerId != 0) {
					if (name != null)
						name = name.trim();

					if (!isValidName(name)) {
						errorMessages.add("\"" + name + "\" is an invalid name. Please use only alphanumeric characters or the following symbols " + VALID_NAME_SYMBOLS);
						numErrors++;
					}

					// test for duplicate names
					List searchList = null;
					
					try {
						searchList = cm.getTransformerList();
					} catch (ManagerException e) {
						errorMessages.add(stackToString(e));
					}
					
					for (int i = 0; i < searchList.size(); i++) {
						Transformer check = (Transformer) searchList.get(i);
						
						if (check.getName().toLowerCase().equals(name.toLowerCase())) {
							if (transformerId != check.getId()) {
								errorMessages.add(name + " already exists. ");
								numErrors++;
							}
						}
					}

					if (script == null || script.equals("")) {
						errorMessages.add("A script must be entered.");
						numErrors++;
					}

					if (numErrors > 0) {
						res.sendRedirect("/transformer/?state=edit&" + parametersAsQuery(req, redirExclude));
						return;
					}

					try {
						Script editScript = cm.createScript();
						editScript.setType(type);
						editScript.setValue(script);

						editTransformer = cm.getTransformer(transformerId);
						
						if (editTransformer != null) {
							if (name != null) {
								editTransformer.setName(name);
								editTransformer.setDescription(description);
								editTransformer.setScript(editScript);
							}
							
							cm.updateTransformer(editTransformer);
						}

					} catch (ManagerException e) {
						errorMessages.add(stackToString(e));
					}
				}

				// user feedback
				if (editTransformer != null) {
					req.setAttribute("highlightId", new Integer(editTransformer.getId()));
					infoMessages.add("Edited Transformer: " + name);
				} else {
					errorMessages.add("The Transformer you are trying to edit no longer exists.");
				}
			}
		}
	}

	public String display(String state, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setAttribute("scripts", SCRIPT_TYPES);
		
		if (state.equals("new")) {
			return TRANSFORMER_FOLDER + "new.jsp";
		} else if (state.equals("edit")) {
			int transformerId = Integer.parseInt(req.getParameter("id"));
			
			if (transformerId != 0) {
				try {
					Transformer editTransformer = cm.getTransformer(transformerId);
					req.setAttribute("transformer", editTransformer);
					
					if (editTransformer.getScript().getType().equals("JavaScript")) {
						req.setAttribute("javascriptValue", editTransformer.getScript().getValue());
					} else if (editTransformer.getScript().getType().equals("python")) {
						req.setAttribute("pythonValue", editTransformer.getScript().getValue());
					}
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
				
				return TRANSFORMER_FOLDER + "edit.jsp";
			} else {
				errorMessages.add("The request id " + req.getParameter("id") + " is invalid");
				return "/error.jsp";
			}
		} else if (state.equals("delete")) {
			int transformerId = Integer.parseInt(req.getParameter("id"));
			
			if (transformerId != 0) {
				try {
					req.setAttribute("transformer", cm.getTransformer(transformerId));
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
			}
			
			return TRANSFORMER_FOLDER + "delete.jsp";
		} else if (state.equals("search")) {
			String searchParam = (String) req.getParameter("term");
			List searchList = null;
			
			try {
				searchList = cm.getTransformerList();
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}

			if (searchParam == null || searchParam.equals("")) {
				req.setAttribute("filters", searchList);
				errorMessages.add("The search term is not valid.");
				return TRANSFORMER_FOLDER + "index.jsp";
			}

			ArrayList<Transformer> finalList = new ArrayList<Transformer>();

			int counter = 0;
			boolean match;

			for (int i = 0; i < searchList.size(); i++) {
				Transformer check = (Transformer) searchList.get(i);
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

			req.setAttribute("transformers", finalList);
			infoMessages.add("Search Matched " + counter + " entries.");

			return TRANSFORMER_FOLDER + "search.jsp";
		} else {
			try {
				List transformerList = cm.getTransformerList();
				List channelList = cm.getChannelList();
				String usedTransformers = "";
				
				for (int i = 0; i < channelList.size(); i++) {
					Channel channel = (Channel) channelList.get(i);
					
					for (int j = 0; j < transformerList.size(); j++) {
						Transformer transformer = (Transformer) transformerList.get(j);
						
						if (!usedTransformers.contains(String.valueOf(transformer.getId()))) {
							if (channel.getTransformerId().contains(String.valueOf(transformer.getId()))) {
								usedTransformers += String.valueOf(transformer.getId()) + "-";
							}
						}
					}
				}
				
				req.setAttribute("transformers", cm.getTransformerList());
				req.setAttribute("usedTransformers", usedTransformers);
			} catch (ManagerException e) {
				errorMessages.add(stackToString(e));
			}
			return TRANSFORMER_FOLDER + "index.jsp";
		}
	}
}
