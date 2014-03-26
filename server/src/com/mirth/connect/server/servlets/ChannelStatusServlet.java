/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.io.RuntimeIOException;

import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.DashboardChannelInfo;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;

public class ChannelStatusServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // MIRTH-1745
        response.setCharacterEncoding("UTF-8");

        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        EngineController engineController = ControllerFactory.getFactory().createEngineController();
        ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
        PrintWriter out = response.getWriter();
        Operation operation = Operations.getOperation(request.getParameter("op"));

        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();

            Set<String> channelIds = getSerializedParameter(request, "channelIds", parameterMap, serializer, Set.class);
            Map<String, List<Integer>> connectorInfo = getSerializedParameter(request, "connectorInfo", parameterMap, serializer, Map.class);
            Integer fetchSize = getIntegerParameter(request, "fetchSize", parameterMap);

            boolean isChannelOperation = operation.equals(Operations.CHANNEL_START) || operation.equals(Operations.CHANNEL_STOP) || operation.equals(Operations.CHANNEL_HALT) || operation.equals(Operations.CHANNEL_PAUSE) || operation.equals(Operations.CHANNEL_RESUME);
            boolean isConnectorOperation = operation.equals(Operations.CHANNEL_START_CONNECTOR) || operation.equals(Operations.CHANNEL_STOP_CONNECTOR);

            if (!isUserAuthorized(request, parameterMap.isEmpty() ? null : parameterMap)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else if (operation.equals(Operations.CHANNEL_GET_STATUS_INITIAL)) {
                response.setContentType(APPLICATION_XML);

                // Return a partial dashboard status list, and a list of remaining channel IDs
                Set<String> remainingChannelIds = engineController.getDeployedIds();

                if (remainingChannelIds.size() > fetchSize) {
                    channelIds = new HashSet<String>(fetchSize);

                    for (Iterator<String> it = remainingChannelIds.iterator(); it.hasNext() && channelIds.size() < fetchSize;) {
                        channelIds.add(it.next());
                        it.remove();
                    }
                } else {
                    channelIds = remainingChannelIds;
                    remainingChannelIds = Collections.emptySet();
                }

                List<DashboardStatus> channelStatuses = engineController.getChannelStatusList(channelIds);
                if (doesUserHaveChannelRestrictions(request)) {
                    channelStatuses = redactChannelStatuses(request, channelStatuses);
                }

                serializer.serialize(new DashboardChannelInfo(channelStatuses, remainingChannelIds), out);
            } else if (operation.equals(Operations.CHANNEL_GET_STATUS)) {
                response.setContentType(APPLICATION_XML);

                // Return dashboard statuses only for the list of channel IDs
                List<DashboardStatus> channelStatuses = engineController.getChannelStatusList(channelIds);
                if (doesUserHaveChannelRestrictions(request)) {
                    channelStatuses = redactChannelStatuses(request, channelStatuses);
                }

                serializer.serialize(channelStatuses, out);
            } else if (operation.equals(Operations.CHANNEL_GET_STATUS_ALL)) {
                response.setContentType(APPLICATION_XML);
                List<DashboardStatus> channelStatuses = null;

                if (doesUserHaveChannelRestrictions(request)) {
                    channelStatuses = redactChannelStatuses(request, engineController.getChannelStatusList());
                } else {
                    channelStatuses = engineController.getChannelStatusList();
                }

                serializer.serialize(channelStatuses, out);
            } else if (isChannelOperation) {
                if (doesUserHaveChannelRestrictions(request)) {
                    channelIds = redactChannelIds(request, channelIds);
                }

                for (String channelId : channelIds) {
                    if (operation.equals(Operations.CHANNEL_START)) {
                        engineController.startChannel(channelId);
                    } else if (operation.equals(Operations.CHANNEL_STOP)) {
                        engineController.stopChannel(channelId);
                    } else if (operation.equals(Operations.CHANNEL_HALT)) {
                        engineController.haltChannel(channelId);
                    } else if (operation.equals(Operations.CHANNEL_PAUSE)) {
                        engineController.pauseChannel(channelId);
                    } else if (operation.equals(Operations.CHANNEL_RESUME)) {
                        engineController.resumeChannel(channelId);
                    }
                }
            } else if (isConnectorOperation) {
                if (doesUserHaveChannelRestrictions(request)) {
                    connectorInfo = redactConnectorInfo(request, connectorInfo);
                }

                for (String channelId : connectorInfo.keySet()) {
                    for (Integer metaDataId : connectorInfo.get(channelId)) {
                        if (operation.equals(Operations.CHANNEL_START_CONNECTOR)) {
                            engineController.startConnector(channelId, metaDataId);
                        } else if (operation.equals(Operations.CHANNEL_STOP_CONNECTOR)) {
                            engineController.stopConnector(channelId, metaDataId);
                        }
                    }
                }
            }
        } catch (RuntimeIOException rio) {
            logger.debug(rio);
        } catch (Throwable t) {
            // log the error, but don't throw the exception back since the client may no longer be waiting for a response
            logger.error(ExceptionUtils.getStackTrace(t));
        }
    }

    private List<DashboardStatus> redactChannelStatuses(HttpServletRequest request, List<DashboardStatus> channelStatuses) throws ServletException {
        List<String> authorizedChannelIds = getAuthorizedChannelIds(request);
        List<DashboardStatus> authorizedStatuses = new ArrayList<DashboardStatus>();

        for (DashboardStatus status : channelStatuses) {
            if (authorizedChannelIds.contains(status.getChannelId())) {
                authorizedStatuses.add(status);
            }
        }

        return authorizedStatuses;
    }

    private Map<String, List<Integer>> redactConnectorInfo(HttpServletRequest request, Map<String, List<Integer>> connectorInfo) throws ServletException {
        List<String> authorizedChannelIds = getAuthorizedChannelIds(request);
        Map<String, List<Integer>> finishedConnectorInfo = new HashMap<String, List<Integer>>();

        for (String channelId : connectorInfo.keySet()) {
            if (authorizedChannelIds.contains(channelId)) {
                finishedConnectorInfo.put(channelId, connectorInfo.get(channelId));
            }
        }

        return finishedConnectorInfo;
    }
}
