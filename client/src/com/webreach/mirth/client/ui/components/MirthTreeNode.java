package com.webreach.mirth.client.ui.components;

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
