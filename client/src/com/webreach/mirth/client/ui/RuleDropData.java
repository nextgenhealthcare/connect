package com.webreach.mirth.client.ui;

import javax.swing.tree.TreeNode;

public class RuleDropData
{
	private TreeNode node;
    private String mapping;
    
    public RuleDropData(TreeNode node, String mapping)
    {
    	setNode(node);
        setMapping(mapping);
    }
    
    public TreeNode getNode()
    {
        return node;
    }

    public void setNode(TreeNode node)
    {
        this.node = node;
    }

    public String getMapping()
    {
        return mapping;
    }

    public void setMapping(String mapping)
    {
        this.mapping = mapping;
    }
}
