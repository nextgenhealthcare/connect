/*
 * MirthTree.java
 *
 * Created on July 5, 2007, 2:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.components;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import org.jdesktop.swingx.JXTree;

/**
 *
 * @author brendanh
 */
public class MirthTree extends JXTree
{
    private MyFilter mf;
    private FilterTreeModel ftm;
    
    /** Creates a new instance of MirthTree */
    public MirthTree()
    {
        mf = new MyFilter();
        ftm = new FilterTreeModel(null, mf);
        this.setModel(ftm);
    }  
    
    public MirthTree(MirthTreeNode root)
    {
        mf = new MyFilter();
        ftm = new FilterTreeModel(root, mf);
        this.setModel(ftm);
    }
    
    public class FilterTreeModel extends DefaultTreeModel
    {
        Filter filter;
        
        public TreeNode getRoot()
        {
            return root;
        }
        
        public FilterTreeModel(TreeNode root, Filter filter)
        {
            super(root);
            this.filter = filter;
        }
        
        public void setFiltered(boolean pass)
        {
            this.filter.setFiltered(!pass);
        }
        public void updateTreeStructure(){
        	TreeNode tn = this.getRoot();
        	int count = tn.getChildCount();
            Object[] path = { tn };
            int[] childIndices = new int[count];
            Object[] children = new Object[count];
        	this.fireTreeStructureChanged(tn, path, childIndices, children);
        }
        public boolean performFilter(TreeNode tn, String text, boolean exact, boolean ignoreChildren)
        {
            if(tn == null)
                return false;
            
            int realCount = super.getChildCount(tn);
            
            boolean passed = filter.pass(tn, text, exact) || ignoreChildren;
            boolean originalPassed = passed;
            
            for (int i = 0; i < realCount; i++)
            {
                boolean childPassed = performFilter(tn.getChildAt(i), text, exact, originalPassed);
                passed = passed || childPassed;
            }
            
            ((MirthTreeNode)tn).setFiltered(!passed);
            
            int count = tn.getChildCount();
            Object[] path = { tn };
            int[] childIndices = new int[count];
            Object[] children = new Object[count];
           //
            
            return passed;
        }
        
        public int getChildCount(Object parent)
        {
            int realCount = super.getChildCount(parent);
            int visibleCount = 0;
            for (int i = 0; i < realCount; i++)
            {
                MirthTreeNode mtn = (MirthTreeNode) super.getChild(parent, i);
                if (!mtn.isFiltered())
                {
                    visibleCount++;
                }
            }
            
            return visibleCount;
        }
        
        public Object getChild(Object parent, int index)
        {
            int cnt = -1;
            for (int i = 0; i < super.getChildCount(parent); i++)
            {
                MirthTreeNode child = (MirthTreeNode) super.getChild(parent, i);
                if (!child.isFiltered())
                {
                    cnt++;
                }
                
                if (cnt == index)
                    return child;
            }
            return null;
        }
    }
    
    interface Filter
    {
        public boolean pass(Object obj, String text, boolean exact);
        
        public void setFiltered(boolean pass);
        
        public boolean isFiltered();
    }
    
    class MyFilter implements Filter
    {
        boolean pass = true;
        
        public boolean pass(Object obj, String text, boolean exact)
        {
            if (pass)
                return true;
            
            String s = obj.toString();
            if(exact)
                return s.equals(text);
            else
                return s.toLowerCase().indexOf(text.toLowerCase()) != -1;                
        }
        
        public void setFiltered(boolean pass)
        {
            this.pass = pass;
        }
        
        public boolean isFiltered()
        {
            return pass;
        }
    }
}
