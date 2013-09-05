/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import javax.swing.table.DefaultTableCellRenderer;

/** CellRenderer that has the alignment set to CENTER. */
public class CenterCellRenderer extends DefaultTableCellRenderer {

    public CenterCellRenderer() {
        super();
        this.setHorizontalAlignment(CENTER);
    }
}
