/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.datatransfer.DataFlavor;

public class TreeNodeFlavor extends DataFlavor {

    public TreeNodeFlavor() {
        super(javax.swing.tree.TreeNode.class, "TreeNode");
    }
}
