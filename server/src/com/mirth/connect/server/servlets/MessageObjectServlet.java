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

import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.Attachment;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.MessageObjectFilter;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.util.DICOMUtil;

public class MessageObjectServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                String operation = request.getParameter("op");
                String uid = null;
                boolean useNewTempTable = false;
                Map<String, Object> parameterMap = new HashMap<String, Object>();

                if (request.getParameter("uid") != null && !request.getParameter("uid").equals("")) {
                    uid = request.getParameter("uid");
                    useNewTempTable = true;
                } else {
                    uid = request.getSession().getId();
                }

                if (operation.equals(Operations.MESSAGE_CREATE_TEMP_TABLE)) {
                    MessageObjectFilter filter = (MessageObjectFilter) serializer.fromXML(request.getParameter("filter"));
                    parameterMap.put("messageFilter", filter);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType("text/plain");
                        out.println(messageObjectController.createMessagesTempTable(filter, uid, useNewTempTable));
                    }
                } else if (operation.equals(Operations.MESSAGE_FILTER_TABLES_REMOVE)) {
                    if (!isUserAuthorized(request, null)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        messageObjectController.removeFilterTable(uid);
                    }
                } else if (operation.equals(Operations.MESSAGE_GET_BY_PAGE)) {
                    if (!isUserAuthorized(request, null)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        int page = Integer.parseInt(request.getParameter("page"));
                        int pageSize = Integer.parseInt(request.getParameter("pageSize"));
                        int max = Integer.parseInt(request.getParameter("maxMessages"));
                        response.setContentType("application/xml");
                        out.print(serializer.toXML(messageObjectController.getMessagesByPage(page, pageSize, max, uid, true)));
                    }

                } else if (operation.equals(Operations.MESSAGE_GET_BY_PAGE_LIMIT)) {
                    MessageObjectFilter filter = (MessageObjectFilter) serializer.fromXML(request.getParameter("filter"));
                    parameterMap.put("filter", filter);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        int page = Integer.parseInt(request.getParameter("page"));
                        int pageSize = Integer.parseInt(request.getParameter("pageSize"));
                        int max = Integer.parseInt(request.getParameter("maxMessages"));
                        response.setContentType("application/xml");
                        out.print(serializer.toXML(messageObjectController.getMessagesByPageLimit(page, pageSize, max, uid, filter)));
                    }
                } else if (operation.equals(Operations.MESSAGE_REMOVE)) {
                    MessageObjectFilter filter = (MessageObjectFilter) serializer.fromXML(request.getParameter("filter"));
                    parameterMap.put("filter", filter);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        messageObjectController.removeMessages(filter);
                    }
                } else if (operation.equals(Operations.MESSAGE_CLEAR)) {
                    String channelId = request.getParameter("data");
                    parameterMap.put("channelId", channelId);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        messageObjectController.clearMessages(channelId);
                    }
                } else if (operation.equals(Operations.MESSAGE_REPROCESS)) {
                    MessageObjectFilter filter = (MessageObjectFilter) serializer.fromXML(request.getParameter("filter"));
                    boolean replace = Boolean.valueOf(request.getParameter("replace"));
                    List<String> destinations = (List<String>) serializer.fromXML(request.getParameter("destinations"));
                    parameterMap.put("filter", filter);
                    parameterMap.put("replace", replace);
                    parameterMap.put("destinations", destinations);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        messageObjectController.reprocessMessages(filter, replace, destinations);
                    }
                } else if (operation.equals(Operations.MESSAGE_PROCESS)) {
                    MessageObject message = (MessageObject) serializer.fromXML(request.getParameter("message"));
                    parameterMap.put("message", message);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        messageObjectController.processMessage(message);
                    }
                } else if (operation.equals(Operations.MESSAGE_IMPORT)) {
                    MessageObject message = (MessageObject) serializer.fromXML(request.getParameter("message"));
                    parameterMap.put("message", message);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        messageObjectController.importMessage(message);
                    }
                } else if (operation.equals(Operations.MESSAGE_ATTACHMENT_GET)) {
                    String attachmentId = request.getParameter("attachmentId");
                    parameterMap.put("attachmentId", attachmentId);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType("application/xml");
                        Attachment attachment = messageObjectController.getAttachment(attachmentId);
                        out.println(serializer.toXML(attachment));
                    }
                } else if (operation.equals(Operations.MESSAGE_ATTACHMENT_GET_BY_MESSAGE_ID)) {
                    String messageId = request.getParameter("messageId");
                    parameterMap.put("messageId", messageId);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType("application/xml");
                        List<Attachment> list = messageObjectController.getAttachmentsByMessageId(messageId);
                        out.println(serializer.toXML(list));
                    }
                } else if (operation.equals(Operations.MESSAGE_ATTACHMENT_GET_ID_BY_MESSAGE_ID)) {
                    String messageId = request.getParameter("messageId");
                    parameterMap.put("messageId", messageId);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType("application/xml");
                        List<Attachment> list = messageObjectController.getAttachmentIdsByMessageId(messageId);
                        out.println(serializer.toXML(list));
                    }
                } else if (operation.equals(Operations.MESSAGE_DICOM_MESSAGE_GET)) {
                    MessageObject message = (MessageObject) serializer.fromXML(request.getParameter("message"));
                    parameterMap.put("message", message);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        String dicomMessage = DICOMUtil.getDICOMRawData(message);
                        out.println(dicomMessage);
                    }
                }
            } catch (Throwable t) {
                logger.error(ExceptionUtils.getStackTrace(t));
                throw new ServletException(t);
            }
        }
    }
}
