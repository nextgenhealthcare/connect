/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.alert;

import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import com.mirth.connect.model.alert.AlertStatus;

public abstract class AlertPanel extends JPanel {

    public abstract void updateAlertTable(List<AlertStatus> alertStatusList);

    public abstract Map<String, String> getAlertNames();

    public abstract List<String> getSelectedAlertIds();

    public abstract void setSelectedAlertIds(List<String> alertIds);

}
