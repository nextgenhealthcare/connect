package com.webreach.mirth.client.ui;

import javax.swing.table.DefaultTableCellRenderer;

/** CellRenderer that has the alignment set to CENTER. */
public class CenterCellRenderer extends DefaultTableCellRenderer {

    public CenterCellRenderer() {
        super();
        this.setHorizontalAlignment(CENTER);
    }
}
