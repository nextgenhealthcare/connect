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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.io.RuntimeIOException;

import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.Operations;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageController;
import com.mirth.connect.server.util.DICOMUtil;
import com.mirth.connect.util.export.MessageExportOptions;

public class MessageObjectServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // MIRTH-1745
        response.setCharacterEncoding("UTF-8");

        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                MessageController messageController = ControllerFactory.getFactory().createMessageController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                Operation operation = Operations.getOperation(request.getParameter("op"));
                // TODO: remove?
//                String uid = null;
//                boolean useNewTempTable = false;
                List<String> authorizedChannelIds = getAuthorizedChannelIds(request);
                Map<String, Object> parameterMap = new HashMap<String, Object>();

                // TODO: remove?
//                if (request.getParameter("uid") != null && !request.getParameter("uid").equals("")) {
//                    uid = request.getParameter("uid");
//                    useNewTempTable = true;
//                } else {
//                    uid = request.getSession().getId();
//                }

                if (operation.equals(Operations.GET_MAX_MESSAGE_ID)) {
                    String channelId = request.getParameter("channelId");
                    
                    parameterMap.put("channelId", channelId);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(APPLICATION_XML);
                        out.print(messageController.getMaxMessageId(channelId));
                    }
                } else if (operation.equals(Operations.GET_MESSAGES)) {
                    String channelId = request.getParameter("channelId");
                    MessageFilter filter = (MessageFilter) serializer.fromXML(request.getParameter("filter"));

                    parameterMap.put("channelId", channelId);
                    parameterMap.put("filter", filter);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        Integer offset = null;
                        Integer limit = null;
                        boolean includeContent = (request.getParameter("includeContent").equals("y"));

                        try {
                            offset = Integer.parseInt(request.getParameter("offset"));
                        } catch (NumberFormatException e) {}
                        
                        try {
                            limit = Integer.parseInt(request.getParameter("limit"));
                        } catch (NumberFormatException e) {}
                        
                        Channel channel = ControllerFactory.getFactory().createEngineController().getDeployedChannel(channelId);
                        response.setContentType(APPLICATION_XML);
                        serializer.toXML(messageController.getMessages(filter, channel, includeContent, offset, limit), out);
                    }
                } else if (operation.equals(Operations.GET_SEARCH_COUNT)) {
                    String channelId = request.getParameter("channelId");
                    MessageFilter filter = (MessageFilter) serializer.fromXML(request.getParameter("filter"));
                    
                    parameterMap.put("channelId", channelId);
                    parameterMap.put("filter", filter);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        Channel channel = ControllerFactory.getFactory().createEngineController().getDeployedChannel(channelId);
                        response.setContentType(APPLICATION_XML);
                        out.print(messageController.getMessageCount(filter, channel));
                    }
                } else if (operation.equals(Operations.GET_MESSAGE_CONTENT)) {
                	String channelId = request.getParameter("channelId");
                	Long messageId = (Long) serializer.fromXML(request.getParameter("messageId"));
                	parameterMap.put("channelId", channelId);
                	parameterMap.put("messageId", messageId);
                	
                	if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                	} else {
                		serializer.toXML(messageController.getMessageContent(channelId, messageId), out);
                	}
                } else if (operation.equals(Operations.MESSAGE_REMOVE)) {
                    // TODO: update calls to this servlet operation so that they pass channelId
                    String channelId = request.getParameter("channelId");
                    MessageFilter filter = (MessageFilter) serializer.fromXML(request.getParameter("filter"));
                    
                    parameterMap.put("channelId", channelId);
                    parameterMap.put("filter", filter);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        messageController.removeMessages(channelId, filter);
                    }
                } else if (operation.equals(Operations.CONNECTOR_MESSAGE_REMOVE)) {
                    // TODO: update calls to this servlet operation so that they pass channelId
                    String channelId = request.getParameter("channelId");
                    MessageFilter filter = (MessageFilter) serializer.fromXML(request.getParameter("filter"));
                    
                    parameterMap.put("channelId", channelId);
                    parameterMap.put("filter", filter);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        messageController.removeConnectorMessages(channelId, filter);
                    }
                } else if (operation.equals(Operations.MESSAGE_CLEAR)) {
                    String channelId = request.getParameter("data");
                    parameterMap.put("channelId", channelId);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        messageController.clearMessages(channelId);
                    }
                } else if (operation.equals(Operations.MESSAGE_REPROCESS)) {
                    String channelId = request.getParameter("channelId");
                    MessageFilter filter = (MessageFilter) serializer.fromXML(request.getParameter("filter"));
                    boolean replace = Boolean.valueOf(request.getParameter("replace"));
                    List<Integer> reprocessMetaDataIds = (List<Integer>) serializer.fromXML(request.getParameter("reprocessMetaDataIds"));
                    parameterMap.put("filter", filter);
                    parameterMap.put("replace", replace);
                    parameterMap.put("destinations", reprocessMetaDataIds);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        messageController.reprocessMessages(channelId, filter, replace, reprocessMetaDataIds, getCurrentUserId(request));
                    }
                } else if (operation.equals(Operations.MESSAGE_PROCESS)) {
                    String channelId = request.getParameter("channelId");
                    String rawData = request.getParameter("message");

                    @SuppressWarnings("unchecked")
                    List<Integer> metaDataIds = (List<Integer>) serializer.fromXML(request.getParameter("metaDataIds"));
                    
                    RawMessage rawMessage = new RawMessage(rawData, metaDataIds, null);

                    parameterMap.put("channelId", channelId);
                    parameterMap.put("message", rawData);
                    parameterMap.put("metaDataIds", metaDataIds);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        try {
                            ControllerFactory.getFactory().createEngineController().handleRawMessage(channelId, rawMessage);
                        } catch (ChannelException e) {
                            throw new ServletException("An error occurred when attempting to process the message");
                        }
                    }
                } else if (operation.equals(Operations.MESSAGE_IMPORT)) {
                    Message message = (Message) serializer.fromXML(request.getParameter("message"));
                    parameterMap.put("message", message);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        messageController.importMessage(message);
                    }
                } else if (operation.equals(Operations.MESSAGE_EXPORT)) {
                    MessageExportOptions options = (MessageExportOptions) serializer.fromXML(request.getParameter("options"));
                    parameterMap.put("options", options);
                    
                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(options.getChannelId()))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        out.print(messageController.exportMessages(options));
                    }
                } else if (operation.equals(Operations.MESSAGE_DICOM_MESSAGE_GET)) {
                    ConnectorMessage message = (ConnectorMessage) serializer.fromXML(request.getParameter("message"));
                    parameterMap.put("message", message);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        String dicomMessage = DICOMUtil.getDICOMRawData(message);
                        out.println(dicomMessage);
                    }
                } else if (operation.equals(Operations.MESSAGE_ATTACHMENT_GET_ID_BY_MESSAGE_ID)) {
                    String channelId = request.getParameter("channelId");
                    Long messageId = (Long) serializer.fromXML(request.getParameter("messageId"));
                    parameterMap.put("channelId", channelId);
                    parameterMap.put("messageId", messageId);
                    
                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        serializer.toXML(messageController.getMessageAttachmentIds(channelId, messageId), out);
                    }
                } else if (operation.equals(Operations.MESSAGE_ATTACHMENT_GET)) {
                    String channelId = request.getParameter("channelId");
                    String attachmentId = request.getParameter("attachmentId");
                    parameterMap.put("channelId", channelId);
                    parameterMap.put("attachmentId", attachmentId);
                    
                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        serializer.toXML(messageController.getMessageAttachment(channelId, attachmentId), out);
                    }
                } else if (operation.equals(Operations.MESSAGE_ATTACHMENT_GET_BY_MESSAGE_ID)) {
                    String channelId = request.getParameter("channelId");
                    Long messageId = (Long) serializer.fromXML(request.getParameter("messageId"));
                    parameterMap.put("channelId", channelId);
                    parameterMap.put("messageId", messageId);
                    
                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        serializer.toXML(messageController.getMessageAttachment(channelId, messageId), out);
                    }
                }
            } catch (RuntimeIOException rio) {
                logger.debug(rio);
            } catch (ServletException se) {
                throw se;
            } catch (Throwable t) {
                logger.error(ExceptionUtils.getStackTrace(t));
                throw new ServletException(t);
            }
        }
    }
}
