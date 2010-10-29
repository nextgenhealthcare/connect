/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.util.ArrayList;

import com.mirth.connect.client.ui.Frame;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.model.CodeTemplate;

public class ClientPlugin {

    protected String name;
    protected Frame parent = PlatformUI.MIRTH_FRAME;

    public ClientPlugin() {
    }

    public ClientPlugin(String name) {
        this.name = name;
    }

    public ArrayList<CodeTemplate> getReferenceItems() {
        return new ArrayList<CodeTemplate>();
    }

    public String getName() {
        return name;
    }
}
