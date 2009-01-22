/*
 * MirthTreeTable.java
 *
 * Created on October 24, 2007, 11:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.components;


import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.TreeTableModel;

/**
 *
 * @author chrisr
 */
public class MirthTreeTable extends JXTreeTable {
    
    /** Creates a new instance of MirthTreeTable */
    public MirthTreeTable()
    {
        super();
    }

    public MirthTreeTable(TreeTableModel treeModel)
    {
        super(treeModel);
    }
}
