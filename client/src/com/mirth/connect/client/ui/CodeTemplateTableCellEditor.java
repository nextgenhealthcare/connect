/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.List;

import com.mirth.connect.model.CodeTemplate;

public class CodeTemplateTableCellEditor extends TextFieldCellEditor {

    protected boolean valueChanged(String value) {

        // make sure the name is not longer than 40 characters
        if (value.length() > 40) {
            getParent().alertWarning(getParent(), "Code Template name cannot be longer than 40 characters.");
            return false;
        }

        List<CodeTemplate> codeTemplates = getParent().codeTemplates;

        // make sure the name doesn't already exist
        for (int i = 0; i < codeTemplates.size(); i++) {
            if (codeTemplates.get(i).getName().equalsIgnoreCase(value)) {
                return false;
            }
        }

        getParent().setSaveEnabled(true);
        // set the name to the new name.
        for (int i = 0; i < codeTemplates.size(); i++) {
            if (codeTemplates.get(i).getName().equalsIgnoreCase(getOriginalValue())) {
                codeTemplates.get(i).setName(value);
            }
        }

        return true;
    }
}
