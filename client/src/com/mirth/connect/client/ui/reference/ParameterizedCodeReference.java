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

import com.mirth.connect.model.codetemplates.CodeTemplateContextSet;

public class ParameterizedCodeReference extends CodeReference {

    private String definitionString;
    private String template;

    public ParameterizedCodeReference(CodeTemplateContextSet contextSet, String category, String name, String description, String template) {
        super(contextSet, category, name, description, template.replaceAll("\\$\\{([^\\}]+)\\}", "$1"));
        this.definitionString = name.toLowerCase().replace(' ', '-');
        this.template = template.replaceAll("\\$(?!\\{)", "\\$\\$");
        setIconName(IconFactory.TEMPLATE_ICON);
        setSummary("<html><body><h4><b>" + StringUtils.trimToEmpty(name) + "</b></h4><hr/>" + StringUtils.trimToEmpty(description) + "<br/><br/><hr/><br/><code>" + StringUtils.trimToEmpty(getReplacementCode()).replaceAll("\r\n|\r|\n", "<br/>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;") + "</code></body></html>");
    }

    public String getDefinitionString() {
        return definitionString;
    }

    public void setDefinitionString(String definitionString) {
        this.definitionString = definitionString;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}