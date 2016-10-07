/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.codetemplate;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.client.ui.AbstractSortableTreeTableNode;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.util.CodeTemplateUtil;

public class CodeTemplateTreeTableNode extends AbstractSortableTreeTableNode {

    private CodeTemplate codeTemplate;

    public CodeTemplateTreeTableNode(CodeTemplate codeTemplate) {
        setCodeTemplate(codeTemplate);
    }

    public String getCodeTemplateId() {
        return codeTemplate.getId();
    }

    public CodeTemplate getCodeTemplate() {
        return codeTemplate;
    }

    public void setCodeTemplate(CodeTemplate codeTemplate) {
        this.codeTemplate = new CodeTemplate(codeTemplate);
    }

    @Override
    public int getColumnCount() {
        return CodeTemplatePanel.TEMPLATE_NUM_COLUMNS;
    }

    @Override
    public Object getValueAt(int i) {
        // @formatter:off
        switch (i) {
            case CodeTemplatePanel.TEMPLATE_NAME_COLUMN: return codeTemplate.getName();
            case CodeTemplatePanel.TEMPLATE_ID_COLUMN: return codeTemplate.getId();
            case CodeTemplatePanel.TEMPLATE_TYPE_COLUMN: return codeTemplate.getProperties().getPluginPointName();
            case CodeTemplatePanel.TEMPLATE_DESCRIPTION_COLUMN:
                if (StringUtils.equals(CodeTemplateUtil.getDocumentation(CodeTemplate.DEFAULT_CODE).getDescription(), codeTemplate.getDescription())) {
                    return null;
                }
                return codeTemplate.getDescription();
            case CodeTemplatePanel.TEMPLATE_REVISION_COLUMN: return codeTemplate.getRevision();
            case CodeTemplatePanel.TEMPLATE_LAST_MODIFIED_COLUMN: return codeTemplate.getLastModified();
            default: return null;
        }
        // @formatter:on
    }

    @Override
    public void setValueAt(Object value, int i) {
        switch (i) {
            case CodeTemplatePanel.TEMPLATE_NAME_COLUMN:
                codeTemplate.setName((String) value);
                break;
            case CodeTemplatePanel.TEMPLATE_ID_COLUMN:
                codeTemplate.setId((String) value);
                break;
            case CodeTemplatePanel.TEMPLATE_REVISION_COLUMN:
                codeTemplate.setRevision((int) value);
                break;
            case CodeTemplatePanel.TEMPLATE_LAST_MODIFIED_COLUMN:
                codeTemplate.setLastModified((Calendar) value);
                break;
        }
    }
}