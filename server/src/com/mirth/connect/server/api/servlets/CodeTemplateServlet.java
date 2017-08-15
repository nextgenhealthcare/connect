/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.collections4.CollectionUtils;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.client.core.api.servlets.CodeTemplateServletInterface;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrarySaveResult;
import com.mirth.connect.model.codetemplates.CodeTemplateSummary;
import com.mirth.connect.server.api.DontCheckAuthorized;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.CodeTemplateController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class CodeTemplateServlet extends MirthServlet implements CodeTemplateServletInterface {

    private static final CodeTemplateController codeTemplateController = ControllerFactory.getFactory().createCodeTemplateController();

    public CodeTemplateServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc);
    }

    @Override
    public List<CodeTemplateLibrary> getCodeTemplateLibraries(Set<String> libraryIds, boolean includeCodeTemplates) {
        try {
            if (CollectionUtils.isEmpty(libraryIds)) {
                libraryIds = null;
            }
            return codeTemplateController.getLibraries(libraryIds, includeCodeTemplates);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public List<CodeTemplateLibrary> getCodeTemplateLibrariesPost(Set<String> libraryIds, boolean includeCodeTemplates) {
        return getCodeTemplateLibraries(libraryIds, includeCodeTemplates);
    }

    @Override
    public CodeTemplateLibrary getCodeTemplateLibrary(String libraryId, boolean includeCodeTemplates) {
        try {
            List<CodeTemplateLibrary> libraries = codeTemplateController.getLibraries(Collections.singleton(libraryId), includeCodeTemplates);
            if (CollectionUtils.isEmpty(libraries)) {
                throw new MirthApiException(Status.NOT_FOUND);
            }
            return libraries.iterator().next();
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public boolean updateCodeTemplateLibraries(List<CodeTemplateLibrary> libraries, boolean override) {
        try {
            return codeTemplateController.updateLibraries(libraries, context, override);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public List<CodeTemplate> getCodeTemplates(Set<String> codeTemplateIds) {
        try {
            if (CollectionUtils.isEmpty(codeTemplateIds)) {
                codeTemplateIds = null;
            }
            return codeTemplateController.getCodeTemplates(codeTemplateIds);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }
    
    @Override
    public List<CodeTemplate> getCodeTemplatesPost(Set<String> codeTemplateIds) {
        return getCodeTemplates(codeTemplateIds);
    }

    @Override
    public CodeTemplate getCodeTemplate(String codeTemplateId) {
        try {
            List<CodeTemplate> codeTemplates = codeTemplateController.getCodeTemplates(Collections.singleton(codeTemplateId));
            if (CollectionUtils.isEmpty(codeTemplates)) {
                throw new MirthApiException(Status.NOT_FOUND);
            }
            return codeTemplates.iterator().next();
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    @DontCheckAuthorized
    public List<CodeTemplateSummary> getCodeTemplateSummary(Map<String, Integer> clientRevisions) {
        parameterMap.put("clientRevisions", clientRevisions);
        if (!isUserAuthorized()) {
            return new ArrayList<CodeTemplateSummary>();
        } else {
            try {
                return codeTemplateController.getCodeTemplateSummary(clientRevisions);
            } catch (ControllerException e) {
                throw new MirthApiException(e);
            }
        }
    }

    @Override
    public boolean updateCodeTemplate(String codeTemplateId, CodeTemplate codeTemplate, boolean override) {
        try {
            return codeTemplateController.updateCodeTemplate(codeTemplate, context, override);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void removeCodeTemplate(String codeTemplateId) {
        try {
            codeTemplateController.removeCodeTemplate(codeTemplateId, context);
        } catch (ControllerException e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public CodeTemplateLibrarySaveResult updateLibrariesAndTemplates(List<CodeTemplateLibrary> libraries, Set<String> removedLibraryIds, List<CodeTemplate> updatedCodeTemplates, Set<String> removedCodeTemplateIds, boolean override) {
        return codeTemplateController.updateLibrariesAndTemplates(libraries, removedLibraryIds, updatedCodeTemplates, removedCodeTemplateIds, context, override);
    }
}