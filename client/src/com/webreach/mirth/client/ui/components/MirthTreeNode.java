/*
 * MirthTreeNode.java
 *
 * Created on July 5, 2007, 2:43 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.components;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author brendanh
 */
public class MirthTreeNode extends DefaultMutableTreeNode
{
    private boolean filtered = false;
    
    public MirthTreeNode(String nodeValue)
    {
        super(nodeValue);
    }
    
    public boolean isFiltered()
    {
        return filtered;
    }
    
    public void setFiltered(boolean filtered)
    {
        this.filtered = filtered;
    }
}
