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
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webreach.mirth.managers.ManagerException;
import com.webreach.mirth.managers.StatusUtil;

public class MainController extends MirthController {
	public void execute(String op, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (op != null) {
			if (op.equals("start")) {
				String name = req.getParameter("name");

				if (name != null) {
					try {
						sm.startChannel(name);
//						sm.resumeCannel(name);
					} catch (ManagerException e) {
						errorMessages.add(stackToString(e));
					}
				}

			} else if (op.equals("stop")) {
				String name = req.getParameter("name");

				if (name != null) {
					try {
						sm.stopChannel(name);
//						sm.pauseChannel(name);
					} catch (ManagerException e) {
						errorMessages.add(stackToString(e));
					}
				}
			} else if (op.equals("deploy")) {
				try {
					sm.deployChannels();	
				} catch (ManagerException e) {
					errorMessages.add(stackToString(e));
				}
			} else if (op.equals("clearLogs")) {
				String name = req.getParameter("name");
				
				if (name != null) {
					try {
						lm.clearLogs(name);	
					} catch (ManagerException e) {
						errorMessages.add(stackToString(e));
					}
				}
			}  else if (op.equals("clearMessages")) {
				String name = req.getParameter("name");
				
				if (name != null) {
					try {
						mm.clearMessages(name);	
					} catch (ManagerException e) {
						errorMessages.add(stackToString(e));
					}
				}
			}
			
		}
	}

	public String display(String state, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if (state.equals("login")) {
			return MAIN_FOLDER + "login.jsp";
		} else if (state.equals("stats")) {
			String name = req.getParameter("name");
			req.setAttribute("statsList", sm.getChannelStats(name));
			req.setAttribute("listKeys", sm.getChannelStats(name).keySet());
			
			return MAIN_FOLDER + "stats.jsp";
		} else if (state.equals("logs")) {
			String name = req.getParameter("name");
			req.setAttribute("logs", lm.getChannelLogs(name));
			req.setAttribute("name", name);
			
			return MAIN_FOLDER + "logs.jsp";
		} else if (state.equals("messages")) {
			String name = req.getParameter("name");
			req.setAttribute("messages", mm.getChannelMessages(name));
			req.setAttribute("name", req.getParameter("name"));
			
			return MAIN_FOLDER + "messages.jsp";
		} else if (state.equals("message")) {
			String id = req.getParameter("id");
			req.setAttribute("content", mm.getMessageContent(id));
			req.setAttribute("contentxml", mm.getMessageContentXml(id));
			req.setAttribute("name", req.getParameter("name"));
			
			return MAIN_FOLDER + "message.jsp";
		} else {
			ArrayList<String> channelNames = sm.getDeployedChannelNames();
			ArrayList<Status> statusList = new ArrayList<Status>();

			for (Iterator iter = channelNames.iterator(); iter.hasNext();) {
				String name = (String) iter.next();
				
				Status status = new Status();
				status.setName(name);
				// removes the ID from the channel name before displaying it
				status.setDisplayName(StatusUtil.cleanChannelName(name));
				status.setRunning(sm.isChannelRunning(name));
				status.setChanged(chm.isChannelChanged(name));
				status.setError(sm.getChannelErrorCount(name));
				status.setReceived(sm.getChannelReceivedMessageCount(name));
				status.setSent(sm.getChannelSentMessageCount(name));
				status.setQueue(sm.getQueuedMessageCount(name));
				
				statusList.add(status);
			}

			req.setAttribute("statusList", statusList);

			if (chm.isConfigurationChanged()) {
				infoMessages.add("Channel configuration has changed");
			}

			return MAIN_FOLDER + "index.jsp";
		}
	}

}
