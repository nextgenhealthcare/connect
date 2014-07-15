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
import java.util.Collection;
import java.util.HashMap;
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
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageController;
import com.mirth.connect.server.util.DICOMMessageUtil;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class MessageObjectServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // MIRTH-1745
        response.setCharacterEncoding("UTF-8");

        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                final MessageController messageController = ControllerFactory.getFactory().createMessageController();
                ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
                PrintWriter out = response.getWriter();
                Operation operation = Operations.getOperation(request.getParameter("op"));
                List<String> authorizedChannelIds = getAuthorizedChannelIds(request);
                Map<String, Object> parameterMap = new HashMap<String, Object>();

                if (operation.equals(Operations.MESSAGE_GET_MAX_ID)) {
                    String channelId = request.getParameter("channelId");

                    parameterMap.put("channelId", channelId);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(APPLICATION_XML);
                        out.print(messageController.getMaxMessageId(channelId));
                    }
                } else if (operation.equals(Operations.MESSAGE_GET)) {
                    String channelId = request.getParameter("channelId");
                    MessageFilter filter = serializer.deserialize(request.getParameter("filter"), MessageFilter.class);

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
                        } catch (NumberFormatException e) {
                        }

                        try {
                            limit = Integer.parseInt(request.getParameter("limit"));
                        } catch (NumberFormatException e) {
                        }

                        Channel channel = ControllerFactory.getFactory().createEngineController().getDeployedChannel(channelId);
                        response.setContentType(APPLICATION_XML);
                        serializer.serialize(messageController.getMessages(filter, channel, includeContent, offset, limit), out);
                    }
                } else if (operation.equals(Operations.MESSAGE_GET_COUNT)) {
                    String channelId = request.getParameter("channelId");
                    MessageFilter filter = serializer.deserialize(request.getParameter("filter"), MessageFilter.class);

                    parameterMap.put("channelId", channelId);
                    parameterMap.put("filter", filter);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        Channel channel = ControllerFactory.getFactory().createEngineController().getDeployedChannel(channelId);
                        response.setContentType(APPLICATION_XML);
                        out.print(messageController.getMessageCount(filter, channel));
                    }
                } else if (operation.equals(Operations.MESSAGE_GET_CONTENT)) {
                    String channelId = request.getParameter("channelId");
                    Long messageId = serializer.deserialize(request.getParameter("messageId"), Long.class);
                    parameterMap.put("channelId", channelId);
                    parameterMap.put("messageId", messageId);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        serializer.serialize(messageController.getMessageContent(channelId, messageId), out);
                    }
                } else if (operation.equals(Operations.MESSAGE_REMOVE)) {
                    String channelId = request.getParameter("channelId");
                    MessageFilter filter = serializer.deserialize(request.getParameter("filter"), MessageFilter.class);

                    parameterMap.put("channelId", channelId);
                    parameterMap.put("filter", filter);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        messageController.removeMessages(channelId, filter);
                    }
                } else if (operation.equals(Operations.MESSAGE_CLEAR)) {
                    @SuppressWarnings("unchecked")
                    Set<String> channelIds = serializer.deserialize(request.getParameter("channelIds"), Set.class);
                    Boolean restartRunningChannels = serializer.deserialize(request.getParameter("restartRunningChannels"), Boolean.class);
                    Boolean clearStatistics = serializer.deserialize(request.getParameter("clearStatistics"), Boolean.class);

                    parameterMap.put("channelIds", channelIds);
                    parameterMap.put("restartRunningChannels", restartRunningChannels);
                    parameterMap.put("clearStatistics", clearStatistics);

                    if (!isUserAuthorized(request, parameterMap) || hasUnauthorizedChannels(request, channelIds, authorizedChannelIds)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        messageController.clearMessages(channelIds, restartRunningChannels, clearStatistics);
                    }
                } else if (operation.equals(Operations.MESSAGE_REPROCESS)) {
                    final String channelId = request.getParameter("channelId");
                    final MessageFilter filter = serializer.deserialize(request.getParameter("filter"), MessageFilter.class);
                    final boolean replace = Boolean.valueOf(request.getParameter("replace"));
                    final List<Integer> reprocessMetaDataIds = serializer.deserializeList(request.getParameter("reprocessMetaDataIds"), Integer.class);
                    parameterMap.put("filter", filter);
                    parameterMap.put("replace", replace);
                    parameterMap.put("destinations", reprocessMetaDataIds);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        Runnable reprocessTask = new Runnable() {
                            @Override
                            public void run() {
                                messageController.reprocessMessages(channelId, filter, replace, reprocessMetaDataIds);
                            }
                        };

                        // Process the message on a new thread so the client is not waiting for it to complete.
                        new Thread(reprocessTask).start();
                    }
                } else if (operation.equals(Operations.MESSAGE_PROCESS)) {
                    final String channelId = request.getParameter("channelId");
                    String rawData = request.getParameter("message");

                    @SuppressWarnings("unchecked")
                    List<Integer> metaDataIds = serializer.deserializeList(request.getParameter("metaDataIds"), Integer.class);

                    final RawMessage rawMessage = new RawMessage(rawData, metaDataIds);

                    parameterMap.put("channelId", channelId);
                    parameterMap.put("message", rawData);
                    parameterMap.put("metaDataIds", metaDataIds);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        Runnable processTask = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ControllerFactory.getFactory().createEngineController().dispatchRawMessage(channelId, rawMessage, true, true);
                                } catch (ChannelException e) {
                                    // Do nothing. An error should have been logged.
                                } catch (BatchMessageException e) {
                                    logger.error("Error processing batch message", e);
                                }
                            }
                        };

                        // Process the message on a new thread so the client is not waiting for it to complete.
                        new Thread(processTask).start();
                    }
                } else if (operation.equals(Operations.MESSAGE_IMPORT)) {
                    String channelId = request.getParameter("channelId");
                    Message message = serializer.deserialize(request.getParameter("message"), Message.class);
                    parameterMap.put("message", message);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        messageController.importMessage(channelId, message);
                    }
                } else if (operation.equals(Operations.MESSAGE_IMPORT_SERVER)) {
                    String channelId = request.getParameter("channelId");
                    String path = request.getParameter("path");
                    Boolean includeSubfolders = serializer.deserialize(request.getParameter("includeSubfolders"), Boolean.class);

                    parameterMap.put("channelId", channelId);
                    parameterMap.put("path", path);
                    parameterMap.put("includeSubfolders", includeSubfolders);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        serializer.serialize(messageController.importMessagesServer(channelId, path, includeSubfolders), out);
                    }
                } else if (operation.equals(Operations.MESSAGE_EXPORT)) {
                    String channelId = request.getParameter("channelId");
                    MessageFilter messageFilter = serializer.deserialize(request.getParameter("filter"), MessageFilter.class);
                    Integer pageSize = serializer.deserialize(request.getParameter("pageSize"), Integer.class);
                    Boolean includeAttachments = serializer.deserialize(request.getParameter("includeAttachments"), Boolean.class);
                    MessageWriterOptions writerOptions = serializer.deserialize(request.getParameter("writerOptions"), MessageWriterOptions.class);
                    writerOptions.setBaseFolder(System.getProperty("user.dir"));

                    parameterMap.put("channelId", channelId);
                    parameterMap.put("messageFilter", messageFilter);
                    parameterMap.put("pageSize", pageSize);
                    parameterMap.put("writerOptions", writerOptions);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        out.print(messageController.exportMessages(channelId, messageFilter, pageSize, includeAttachments, writerOptions));
                    }
                } else if (operation.equals(Operations.MESSAGE_DICOM_MESSAGE_GET)) {
                    ConnectorMessage message = serializer.deserialize(request.getParameter("message"), ConnectorMessage.class);
                    parameterMap.put("message", message);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        String dicomMessage = DICOMMessageUtil.getDICOMRawData(message);
                        out.println(dicomMessage);
                    }
                } else if (operation.equals(Operations.MESSAGE_ATTACHMENT_GET_ID_BY_MESSAGE_ID)) {
                    String channelId = request.getParameter("channelId");
                    Long messageId = serializer.deserialize(request.getParameter("messageId"), Long.class);
                    parameterMap.put("channelId", channelId);
                    parameterMap.put("messageId", messageId);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        serializer.serialize(messageController.getMessageAttachmentIds(channelId, messageId), out);
                    }
                } else if (operation.equals(Operations.MESSAGE_ATTACHMENT_GET)) {
                    String channelId = request.getParameter("channelId");
                    String attachmentId = request.getParameter("attachmentId");
                    parameterMap.put("channelId", channelId);
                    parameterMap.put("attachmentId", attachmentId);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        serializer.serialize(messageController.getMessageAttachment(channelId, attachmentId), out);
                    }
                } else if (operation.equals(Operations.MESSAGE_ATTACHMENT_GET_BY_MESSAGE_ID)) {
                    String channelId = request.getParameter("channelId");
                    Long messageId = serializer.deserialize(request.getParameter("messageId"), Long.class);
                    parameterMap.put("channelId", channelId);
                    parameterMap.put("messageId", messageId);

                    if (!isUserAuthorized(request, parameterMap) || (doesUserHaveChannelRestrictions(request) && !authorizedChannelIds.contains(channelId))) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        serializer.serialize(messageController.getMessageAttachment(channelId, messageId), out);
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

    private boolean hasUnauthorizedChannels(HttpServletRequest request, Collection<String> channelIds, List<String> authorizedChannelIds) throws ServletException {
        if (doesUserHaveChannelRestrictions(request)) {
            for (String channelId : channelIds) {
                if (!authorizedChannelIds.contains(channelId)) {
                    return true;
                }
            }
        }

        return false;
    }
}
