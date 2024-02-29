/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import javax.swing.JPanel;

import com.mirth.connect.model.ResourceProperties;

public abstract class ResourcePropertiesPanel extends JPanel {

    public abstract void fillProperties(ResourceProperties properties);

    public abstract void setProperties(ResourceProperties properties);

    public abstract ResourceProperties getDefaults();

    public abstract String checkProperties();

    public abstract void resetInvalidProperties();
}