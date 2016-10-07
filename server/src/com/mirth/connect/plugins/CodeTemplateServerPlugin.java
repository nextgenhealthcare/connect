/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;

public interface CodeTemplateServerPlugin extends ServerPlugin {

    public void save(CodeTemplateLibrary library, ServerEventContext context);

    public void remove(CodeTemplateLibrary library, ServerEventContext context);

    public void save(CodeTemplate codeTemplate, ServerEventContext context);

    public void remove(CodeTemplate codeTemplate, ServerEventContext context);
}