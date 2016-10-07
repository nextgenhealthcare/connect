/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.reference;

import org.apache.commons.lang3.StringUtils;
import org.fife.rsta.ac.js.IconFactory;

import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;
import com.mirth.connect.util.CodeTemplateUtil;

public class CodeReference extends Reference {

    public CodeReference(String category, CodeTemplate codeTemplate) {
        this(codeTemplate.getContextSet(), category, codeTemplate.getName(), codeTemplate.getDescription(), CodeTemplateUtil.stripDocumentation(codeTemplate.getCode()));
    }

    public CodeReference(CodeTemplateContextSet contextSet, String category, String name, String description, String replacementCode) {
        super(Type.CODE, contextSet, category, name, description, replacementCode);
        setIconName(IconFactory.TEMPLATE_ICON);
        setSummary("<html><body><h4><b>" + StringUtils.trimToEmpty(name) + "</b></h4><hr/>" + StringUtils.trimToEmpty(description) + "<br/><br/><hr/><br/><code>" + StringUtils.trimToEmpty(replacementCode).replaceAll("\r\n|\r|\n", "<br/>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;") + "</code></body></html>");
    }
}