/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.TreeTableModel;

public class MirthTreeTable extends JXTreeTable {

    /** Creates a new instance of MirthTreeTable */
    public MirthTreeTable() {
        super();
    }

    public MirthTreeTable(TreeTableModel treeModel) {
        super(treeModel);
    }
}
