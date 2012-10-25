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

import com.mirth.connect.client.ui.components.MirthFieldConstraints;
import com.mirth.connect.model.Alert;

public class AlertTableCellEditor extends TextFieldCellEditor {

    public AlertTableCellEditor() {
        super();

        // set the character limit to 40.
        getTextField().setDocument(new MirthFieldConstraints(40));
    }

    protected boolean valueChanged(String value) {
        List<Alert> alerts = getParent().alerts;

        // make sure the name doesn't already exist
        for (int i = 0; i < alerts.size(); i++) {
            if (alerts.get(i).getName().equalsIgnoreCase(value)) {
                return false;
            }
        }

        getParent().setSaveEnabled(true);
        // set the name to the new name.
        for (int i = 0; i < alerts.size(); i++) {
            if (alerts.get(i).getName().equalsIgnoreCase(getOriginalValue())) {
                alerts.get(i).setName(value);
            }
        }

        return true;
    }
}
