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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.io.RuntimeIOException;

import com.mirth.connect.client.core.Operation;
import com.mirth.connect.client.core.Operations;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.controllers.CodeTemplateController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class CodeTemplateServlet extends MirthServlet {
    private Logger logger = Logger.getLogger(this.getClass());

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // MIRTH-1745
        response.setCharacterEncoding("UTF-8");
        
        if (!isUserLoggedIn(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            try {
                CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();
                ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
                PrintWriter out = response.getWriter();
                Operation operation = Operations.getOperation(request.getParameter("op"));
                Map<String, Object> parameterMap = new HashMap<String, Object>();

                if (operation.equals(Operations.CODE_TEMPLATE_GET)) {
                    CodeTemplate codeTemplate = (CodeTemplate) serializer.fromXML(request.getParameter("codeTemplate"));
                    parameterMap.put("codeTemplate", codeTemplate);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(APPLICATION_XML);
                        serializer.toXML(codeTemplateController.getCodeTemplate(codeTemplate), out);
                    }
                } else if (operation.equals(Operations.CODE_TEMPLATE_UPDATE)) {
                    List<CodeTemplate> codeTemplates = (List<CodeTemplate>) serializer.fromXML(request.getParameter("codeTemplates"));
                    parameterMap.put("codeTemplates", codeTemplates);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        codeTemplateController.updateCodeTemplates(codeTemplates);
                    }
                } else if (operation.equals(Operations.CODE_TEMPLATE_REMOVE)) {
                    CodeTemplate codeTemplate = (CodeTemplate) serializer.fromXML(request.getParameter("codeTemplate"));
                    parameterMap.put("codeTemplate", codeTemplate);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        codeTemplateController.removeCodeTemplate(codeTemplate);
                    }
                }
            } catch (RuntimeIOException rio) {
                logger.debug(rio);
            } catch (Throwable t) {
                logger.error(ExceptionUtils.getStackTrace(t));
                throw new ServletException(t);
            }
        }
    }
}
