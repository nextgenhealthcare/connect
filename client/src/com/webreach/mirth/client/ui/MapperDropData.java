package com.webreach.mirth.client.ui;

import javax.swing.tree.TreeNode;

public class MapperDropData
{
	private TreeNode node;
    private String variable;
    private String mapping;
    
    public MapperDropData(TreeNode node, String variable, String mapping)
    {
    	setNode(node);
        setVariable(variable);
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

    public String getVariable()
    {
        return variable;
    }

    public void setVariable(String variable)
    {
        this.variable = variable;
    }
}
