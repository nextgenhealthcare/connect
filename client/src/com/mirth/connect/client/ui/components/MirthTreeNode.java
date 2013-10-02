/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import javax.swing.tree.DefaultMutableTreeNode;

public class MirthTreeNode extends DefaultMutableTreeNode {

    private boolean filtered = false;

    public MirthTreeNode(String nodeValue) {
        super(nodeValue);
    }

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }
}
