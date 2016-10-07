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

import com.mirth.connect.client.ui.AbstractSortableTreeTableNode;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;

public class CodeTemplateLibraryTreeTableNode extends AbstractSortableTreeTableNode {

    private CodeTemplateLibrary library;

    public CodeTemplateLibraryTreeTableNode(CodeTemplateLibrary library) {
        setLibrary(library);
    }

    public String getLibraryId() {
        return library.getId();
    }

    public CodeTemplateLibrary getLibrary() {
        return library;
    }

    public void setLibrary(CodeTemplateLibrary library) {
        this.library = new CodeTemplateLibrary(library);
    }

    @Override
    public int getColumnCount() {
        return CodeTemplatePanel.TEMPLATE_NUM_COLUMNS;
    }

    @Override
    public Object getValueAt(int i) {
        // @formatter:off
        switch (i) {
            case CodeTemplatePanel.TEMPLATE_NAME_COLUMN: return library.getName();
            case CodeTemplatePanel.TEMPLATE_ID_COLUMN: return library.getId();
            case CodeTemplatePanel.TEMPLATE_TYPE_COLUMN: return null;
            case CodeTemplatePanel.TEMPLATE_DESCRIPTION_COLUMN: return library.getDescription();
            case CodeTemplatePanel.TEMPLATE_REVISION_COLUMN: return library.getRevision();
            case CodeTemplatePanel.TEMPLATE_LAST_MODIFIED_COLUMN: return library.getLastModified();
            default: return null;
        }
        // @formatter:on
    }

    @Override
    public void setValueAt(Object value, int i) {
        switch (i) {
            case CodeTemplatePanel.TEMPLATE_NAME_COLUMN:
                library.setName((String) value);
                break;
            case CodeTemplatePanel.TEMPLATE_ID_COLUMN:
                library.setId((String) value);
                break;
            case CodeTemplatePanel.TEMPLATE_DESCRIPTION_COLUMN:
                library.setDescription((String) value);
                break;
            case CodeTemplatePanel.TEMPLATE_REVISION_COLUMN:
                library.setRevision((int) value);
                break;
            case CodeTemplatePanel.TEMPLATE_LAST_MODIFIED_COLUMN:
                library.setLastModified((Calendar) value);
                break;
        }
    }
}