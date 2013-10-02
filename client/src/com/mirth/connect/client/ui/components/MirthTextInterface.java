/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

public interface MirthTextInterface {

    public void cut();

    public void copy();

    public void paste();

    public void selectAll();

    public void replaceSelection(String text);

    public boolean isEditable();

    public boolean isEnabled();

    public boolean isVisible();

    public String getSelectedText();

    public String getText();
}
