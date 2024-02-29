/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.codetemplates;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("codeTemplateSummary")
public class CodeTemplateSummary implements Serializable {
    private String codeTemplateId;
    private boolean deleted;
    private CodeTemplate codeTemplate;

    public CodeTemplateSummary(String codeTemplateId) {
        this(codeTemplateId, null);
    }

    public CodeTemplateSummary(String codeTemplateId, CodeTemplate codeTemplate) {
        this.codeTemplateId = codeTemplateId;
        this.codeTemplate = codeTemplate;
    }

    public String getCodeTemplateId() {
        return codeTemplateId;
    }

    public void setCodeTemplateId(String codeTemplateId) {
        this.codeTemplateId = codeTemplateId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public CodeTemplate getCodeTemplate() {
        return codeTemplate;
    }

    public void setCodeTemplate(CodeTemplate codeTemplate) {
        this.codeTemplate = codeTemplate;
    }
}