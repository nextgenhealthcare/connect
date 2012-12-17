/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class ChannelServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // MIRTH-1745
        response.setCharacterEncoding("UTF-8");
        
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                ChannelController channelController = ControllerFactory.getFactory().createChannelController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                Operation operation = Operations.getOperation(request.getParameter("op"));
                Map<String, Object> parameterMap = new HashMap<String, Object>();
                ServerEventContext context = new ServerEventContext();
                context.setUserId(getCurrentUserId(request));
                
                if (operation.equals(Operations.CHANNEL_GET)) {
                    response.setContentType(APPLICATION_XML);
                    List<Channel> channels = null;
                    Channel channel = (Channel) serializer.fromXML(request.getParameter("channel"));
                    parameterMap.put("channel", channel);

                    if (!isUserAuthorized(request, parameterMap)) {
                        channels = new ArrayList<Channel>();
                    } else if (doesUserHaveChannelRestrictions(request)) {
                        channels = redactChannels(request, channelController.getChannel(channel));
                    } else {
                        channels = channelController.getChannel(channel);
                    }

                    serializer.toXML(channels, out);
                } else if (operation.equals(Operations.CHANNEL_UPDATE)) {
                    Channel channel = (Channel) serializer.fromXML(request.getParameter("channel"));
                    boolean override = Boolean.valueOf(request.getParameter("override")).booleanValue();
                    parameterMap.put("channel", channel);
                    parameterMap.put("override", override);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(TEXT_PLAIN);
                        // NOTE: This needs to be print rather than println to
                        // avoid the newline
                        out.print(channelController.updateChannel(channel, context, override));
                    }
                } else if (operation.equals(Operations.CHANNEL_REMOVE)) {
                    Channel channel = (Channel) serializer.fromXML(request.getParameter("channel"));
                    parameterMap.put("channel", channel);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        channelController.removeChannel(channel, context);
                    }
                } else if (operation.equals(Operations.CHANNEL_GET_SUMMARY)) {
                    response.setContentType(APPLICATION_XML);
                    List<ChannelSummary> channelSummaries = null;
                    Map<String, Integer> cachedChannels = (Map<String, Integer>) serializer.fromXML(request.getParameter("cachedChannels"));
                    parameterMap.put("cachedChannels", cachedChannels);

                    if (!isUserAuthorized(request, parameterMap)) {
                        channelSummaries = new ArrayList<ChannelSummary>();
                    } else if (doesUserHaveChannelRestrictions(request)) {
                        channelSummaries = redactChannelSummaries(request, channelController.getChannelSummary(cachedChannels));
                    } else {
                        channelSummaries = channelController.getChannelSummary(cachedChannels);
                    }

                    serializer.toXML(channelSummaries, out);
                } else if (operation.equals(Operations.CHANNEL_GET_TAGS)) {
                    response.setContentType(APPLICATION_XML);
                    Set<String> tags = null;

                    if (!isUserAuthorized(request, parameterMap)) {
                        tags = new LinkedHashSet<String>();
                    } else if (doesUserHaveChannelRestrictions(request)) {
                        tags = channelController.getChannelTags(new HashSet<String>(getAuthorizedChannelIds(request)));
                    } else {
                        tags = channelController.getChannelTags();
                    }

                    serializer.toXML(tags, out);
                } else if (operation.equals(Operations.CHANNEL_GET_CONNECTOR_NAMES)) {
                    response.setContentType(APPLICATION_XML);
                    String channelId = request.getParameter("channelId");
                    parameterMap.put("channelId", channelId);
                    
                    Map<Integer, String> connectorNames = null;

                    if (!isUserAuthorized(request, parameterMap)) {
                        connectorNames = new LinkedHashMap<Integer, String>();
                    } else if (doesUserHaveChannelRestrictions(request)) {
                        if (getAuthorizedChannelIds(request).contains(channelId)) {
                            connectorNames = channelController.getConnectorNames(channelId);
                        }
                    } else {
                        connectorNames = channelController.getConnectorNames(channelId);
                    }

                    serializer.toXML(connectorNames, out);
                } else if (operation.equals(Operations.CHANNEL_GET_METADATA_COLUMNS)) {
                    response.setContentType(APPLICATION_XML);
                    String channelId = request.getParameter("channelId");
                    parameterMap.put("channelId", channelId);
                    
                    List<MetaDataColumn> metaDataColumns = null;

                    if (!isUserAuthorized(request, parameterMap)) {
                        metaDataColumns = new ArrayList<MetaDataColumn>();
                    } else if (doesUserHaveChannelRestrictions(request)) {
                        if (getAuthorizedChannelIds(request).contains(channelId)) {
                            metaDataColumns = channelController.getMetaDataColumns(channelId);
                        }
                    } else {
                        metaDataColumns = channelController.getMetaDataColumns(channelId);
                    }

                    serializer.toXML(metaDataColumns, out);
                }
            } catch (RuntimeIOException rio) {
                logger.debug(rio);
            } catch (Throwable t) {
                logger.error(ExceptionUtils.getStackTrace(t));
                throw new ServletException(t);
            }
        }
    }
    
    private List<Channel> redactChannels(HttpServletRequest request, List<Channel> channels) throws ServletException {
        List<String> authorizedChannelIds = getAuthorizedChannelIds(request);
        List<Channel> authorizedChannels = new ArrayList<Channel>();
        
        for (Channel channel : channels) {
            if (authorizedChannelIds.contains(channel.getId())) {
                authorizedChannels.add(channel);
            }
        }
        
        return authorizedChannels;
    }
    
    private List<ChannelSummary> redactChannelSummaries(HttpServletRequest request, List<ChannelSummary> channelSummaries) throws ServletException {
        List<String> authorizedChannelIds = getAuthorizedChannelIds(request);
        List<ChannelSummary> authorizedChannelSummaries = new ArrayList<ChannelSummary>();
        
        for (ChannelSummary channelSummary : channelSummaries) {
            if (authorizedChannelIds.contains(channelSummary.getId())) {
                authorizedChannelSummaries.add(channelSummary);
            }
        }
        
        return authorizedChannelSummaries;
    }
}
