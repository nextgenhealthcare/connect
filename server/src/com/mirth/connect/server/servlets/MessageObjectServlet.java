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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.Attachment;
import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.MessageObjectFilter;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.util.DICOMUtil;

public class MessageObjectServlet extends MirthServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else if (!isUserAuthorized(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            try {
                MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
                ObjectXMLSerializer serializer = new ObjectXMLSerializer();
                PrintWriter out = response.getWriter();
                String operation = request.getParameter("op");
                String uid = null;
                boolean useNewTempTable = false;

                if (request.getParameter("uid") != null && !request.getParameter("uid").equals("")) {
                    uid = request.getParameter("uid");
                    useNewTempTable = true;
                } else {
                    uid = request.getSession().getId();
                }

                if (operation.equals(Operations.MESSAGE_CREATE_TEMP_TABLE)) {
                    String filter = request.getParameter("filter");
                    response.setContentType("text/plain");
                    out.println(messageObjectController.createMessagesTempTable((MessageObjectFilter) serializer.fromXML(filter), uid, useNewTempTable));
                } else if (operation.equals(Operations.MESSAGE_FILTER_TABLES_REMOVE)) {
                    messageObjectController.removeFilterTable(uid);
                } else if (operation.equals(Operations.MESSAGE_GET_BY_PAGE)) {
                    String page = request.getParameter("page");
                    String pageSize = request.getParameter("pageSize");
                    String maxMessages = request.getParameter("maxMessages");
                    response.setContentType("application/xml");
                    out.print(serializer.toXML(messageObjectController.getMessagesByPage(Integer.parseInt(page), Integer.parseInt(pageSize), Integer.parseInt(maxMessages), uid, true)));
                } else if (operation.equals(Operations.MESSAGE_GET_BY_PAGE_LIMIT)) {
                    String page = request.getParameter("page");
                    String pageSize = request.getParameter("pageSize");
                    String maxMessages = request.getParameter("maxMessages");
                    String filter = request.getParameter("filter");
                    response.setContentType("application/xml");
                    out.print(serializer.toXML(messageObjectController.getMessagesByPageLimit(Integer.parseInt(page), Integer.parseInt(pageSize), Integer.parseInt(maxMessages), uid, (MessageObjectFilter) serializer.fromXML(filter))));
                } else if (operation.equals(Operations.MESSAGE_REMOVE)) {
                    String filter = request.getParameter("filter");
                    messageObjectController.removeMessages((MessageObjectFilter) serializer.fromXML(filter));
                } else if (operation.equals(Operations.MESSAGE_CLEAR)) {
                    String channelId = request.getParameter("data");
                    messageObjectController.clearMessages(channelId);
                } else if (operation.equals(Operations.MESSAGE_REPROCESS)) {
                    MessageObjectFilter filter = (MessageObjectFilter) serializer.fromXML(request.getParameter("filter"));
                    boolean replace = Boolean.valueOf(request.getParameter("replace"));
                    List<String> destinations = (List<String>) serializer.fromXML(request.getParameter("destinations"));
                    messageObjectController.reprocessMessages(filter, replace, destinations);
                } else if (operation.equals(Operations.MESSAGE_PROCESS)) {
                    String message = request.getParameter("message");
                    messageObjectController.processMessage((MessageObject) serializer.fromXML(message));
                } else if (operation.equals(Operations.MESSAGE_IMPORT)) {
                    String message = request.getParameter("message");
                    messageObjectController.importMessage((MessageObject) serializer.fromXML(message));
                } else if (operation.equals(Operations.MESSAGE_ATTACHMENT_GET)) {
                    response.setContentType("application/xml");
                    Attachment attachment = messageObjectController.getAttachment(request.getParameter("attachmentId"));
                    out.println(serializer.toXML(attachment));
                } else if (operation.equals(Operations.MESSAGE_ATTACHMENT_GET_BY_MESSAGE_ID)) {
                    response.setContentType("application/xml");
                    List<Attachment> list = messageObjectController.getAttachmentsByMessageId(request.getParameter("messageId"));
                    out.println(serializer.toXML(list));
                } else if (operation.equals(Operations.MESSAGE_ATTACHMENT_GET_ID_BY_MESSAGE_ID)) {
                    response.setContentType("application/xml");
                    List<Attachment> list = messageObjectController.getAttachmentIdsByMessageId(request.getParameter("messageId"));
                    out.println(serializer.toXML(list));
                } else if (operation.equals(Operations.MESSAGE_DICOM_MESSAGE_GET)) {
                    String message = request.getParameter("message");
                    String dicomMessage = DICOMUtil.getDICOMRawData((MessageObject) serializer.fromXML(message));
                    out.println(dicomMessage);
                }
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }
}
