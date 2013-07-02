/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.alert;

import javax.swing.JPanel;

import com.mirth.connect.model.alert.AlertModel;

public abstract class AlertEditPanel extends JPanel {

    public abstract String getAlertId();

    public abstract void updateVariableList();

    public abstract void addAlert();

    public abstract boolean editAlert(AlertModel alertModel);

    public abstract boolean saveAlert();
}
