/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;

import com.mirth.connect.model.CodeTemplate;

public abstract class CodeTemplateController extends Controller {
    public static CodeTemplateController getInstance() {
        return ControllerFactory.getFactory().createCodeTemplateController();
    }

    public abstract List<CodeTemplate> getCodeTemplate(CodeTemplate codeTemplate) throws ControllerException;

    public abstract void updateCodeTemplates(List<CodeTemplate> codeTemplates) throws ControllerException;

    public abstract void removeCodeTemplate(CodeTemplate codeTemplate) throws ControllerException;
}
