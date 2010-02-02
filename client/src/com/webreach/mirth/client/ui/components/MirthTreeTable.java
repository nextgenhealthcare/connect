package com.webreach.mirth.client.ui.components;

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
