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
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplateLibrary;
import com.mirth.connect.model.CodeTemplateSummary;
import com.mirth.connect.model.ServerEventContext;
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
                ServerEventContext context = new ServerEventContext();
                context.setUserId(getCurrentUserId(request));

                if (operation.equals(Operations.CODE_TEMPLATE_LIBRARY_GET)) {
                    Set<String> libraryIds = serializer.deserialize(request.getParameter("libraryIds"), Set.class);
                    boolean includeCodeTemplates = Boolean.valueOf(request.getParameter("includeCodeTemplates")).booleanValue();
                    parameterMap.put("libraryIds", libraryIds);
                    parameterMap.put("includeCodeTemplates", includeCodeTemplates);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(APPLICATION_XML);
                        serializer.serialize(codeTemplateController.getLibraries(libraryIds, includeCodeTemplates), out);
                    }
                } else if (operation.equals(Operations.CODE_TEMPLATE_LIBRARY_UPDATE)) {
                    List<CodeTemplateLibrary> libraries = serializer.deserializeList(request.getParameter("libraries"), CodeTemplateLibrary.class);
                    boolean override = Boolean.valueOf(request.getParameter("override")).booleanValue();
                    parameterMap.put("libraries", libraries);
                    parameterMap.put("override", override);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(TEXT_PLAIN);
                        // NOTE: This needs to be print rather than println to avoid the newline
                        out.print(codeTemplateController.updateLibraries(libraries, context, override));
                    }
                } else if (operation.equals(Operations.CODE_TEMPLATE_GET)) {
                    @SuppressWarnings("unchecked")
                    Set<String> codeTemplateIds = serializer.deserialize(request.getParameter("codeTemplateIds"), Set.class);
                    parameterMap.put("codeTemplateIds", codeTemplateIds);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(APPLICATION_XML);
                        serializer.serialize(codeTemplateController.getCodeTemplates(codeTemplateIds), out);
                    }
                } else if (operation.equals(Operations.CODE_TEMPLATE_GET_SUMMARY)) {
                    response.setContentType(APPLICATION_XML);
                    List<CodeTemplateSummary> codeTemplateSummaries = null;
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> clientRevisions = serializer.deserialize(request.getParameter("clientRevisions"), Map.class);
                    parameterMap.put("clientRevisions", clientRevisions);

                    if (!isUserAuthorized(request, parameterMap)) {
                        codeTemplateSummaries = new ArrayList<CodeTemplateSummary>();
                    } else {
                        codeTemplateSummaries = codeTemplateController.getCodeTemplateSummary(clientRevisions);
                    }

                    serializer.serialize(codeTemplateSummaries, out);
                } else if (operation.equals(Operations.CODE_TEMPLATE_UPDATE)) {
                    CodeTemplate codeTemplate = serializer.deserialize(request.getParameter("codeTemplate"), CodeTemplate.class);
                    boolean override = Boolean.valueOf(request.getParameter("override")).booleanValue();
                    parameterMap.put("codeTemplate", codeTemplate);
                    parameterMap.put("override", override);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(TEXT_PLAIN);
                        // NOTE: This needs to be print rather than println to avoid the newline
                        out.print(codeTemplateController.updateCodeTemplate(codeTemplate, context, override));
                    }
                } else if (operation.equals(Operations.CODE_TEMPLATE_REMOVE)) {
                    CodeTemplate codeTemplate = serializer.deserialize(request.getParameter("codeTemplate"), CodeTemplate.class);
                    parameterMap.put("codeTemplate", codeTemplate);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        codeTemplateController.removeCodeTemplate(codeTemplate, context);
                    }
                } else if (operation.equals(Operations.CODE_TEMPLATE_UPDATE_ALL)) {
                    List<CodeTemplateLibrary> libraries = serializer.deserializeList(request.getParameter("libraries"), CodeTemplateLibrary.class);
                    List<CodeTemplateLibrary> removedLibraries = serializer.deserializeList(request.getParameter("removedLibraries"), CodeTemplateLibrary.class);
                    List<CodeTemplate> updatedCodeTemplates = serializer.deserializeList(request.getParameter("updatedCodeTemplates"), CodeTemplate.class);
                    List<CodeTemplate> removedCodeTemplates = serializer.deserializeList(request.getParameter("removedCodeTemplates"), CodeTemplate.class);
                    boolean override = Boolean.valueOf(request.getParameter("override")).booleanValue();
                    parameterMap.put("libraries", libraries);
                    parameterMap.put("removedLibraries", removedLibraries);
                    parameterMap.put("updatedCodeTemplates", updatedCodeTemplates);
                    parameterMap.put("removedCodeTemplates", removedCodeTemplates);
                    parameterMap.put("override", override);

                    if (!isUserAuthorized(request, parameterMap)) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.setContentType(APPLICATION_XML);
                        serializer.serialize(codeTemplateController.updateLibrariesAndTemplates(libraries, removedLibraries, updatedCodeTemplates, removedCodeTemplates, context, override), out);
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
