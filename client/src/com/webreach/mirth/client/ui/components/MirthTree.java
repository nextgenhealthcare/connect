/*
 * MirthTree.java
 *
 * Created on July 5, 2007, 2:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.components;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.LinkedList;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTree;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.MapperDropData;
import com.webreach.mirth.client.ui.MessageBuilderDropData;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.TreeTransferable;
import com.webreach.mirth.client.ui.editors.MessageTreePanel;

/**
 *
 * @author brendanh
 */
public class MirthTree extends JXTree implements DropTargetListener
{
    private Frame parent;
    private MyFilter mf;
    private FilterTreeModel ftm;
    private String prefix;
    private String suffix;
    private DropTarget dropTarget;
    private DataFlavor supportedDropFlavor;
    
    /** Creates a new instance of MirthTree */
    public MirthTree()
    {
        mf = new MyFilter();
        ftm = new FilterTreeModel(null, mf);
        this.setModel(ftm);
        dropTarget = new DropTarget(this, this);
    }  
    
    public MirthTree(MirthTreeNode root, String prefix, String suffix)
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        mf = new MyFilter();
        ftm = new FilterTreeModel(root, mf);
        this.setModel(ftm);
        dropTarget = new DropTarget(this, this);
        this.prefix = prefix;
        this.suffix = suffix;
        
        if(prefix.equals(MessageTreePanel.MAPPER_PREFIX))
            this.supportedDropFlavor = TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR;
        else if(prefix.equals(MessageTreePanel.MESSAGE_BUILDER_PREFIX))
            this.supportedDropFlavor = TreeTransferable.MAPPER_DATA_FLAVOR;
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
    
    public void dragEnter(DropTargetDragEvent dtde)
    {
        try
        {
            Transferable tr = dtde.getTransferable();
            
            if (parent.currentContentPage == parent.channelEditPanel.transformerPane && tr.isDataFlavorSupported(supportedDropFlavor))
            {
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            }
            else
                dtde.rejectDrag();
        }
        catch (Exception e)
        {
            dtde.rejectDrag();
        }
    }

    public void dragExit(DropTargetEvent dte)
    {
        // TODO Auto-generated method stub
        
    }

    public void dragOver(DropTargetDragEvent dtde)
    {
        if(!dtde.isDataFlavorSupported(supportedDropFlavor))
            return;
        
        Point cursorLocationBis = dtde.getLocation();
        TreePath destinationPath = getPathForLocation(cursorLocationBis.x, cursorLocationBis.y);
        if(destinationPath != null)
        {
            if(((MirthTreeNode)destinationPath.getLastPathComponent()).isLeaf())
            {
                this.setSelectionPath(destinationPath);
            }
        }
    }

    public void drop(DropTargetDropEvent dtde)
    {
        if(parent.currentContentPage != parent.channelEditPanel.transformerPane)
            return;

        try
        {
            TreeNode selectedNode = (TreeNode) this.getLastSelectedPathComponent();
            
            if(selectedNode == null)
                return;
            
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            Transferable tr = dtde.getTransferable();
            
            if (supportedDropFlavor == TreeTransferable.MAPPER_DATA_FLAVOR)
            {
                Object transferData = tr.getTransferData(TreeTransferable.MAPPER_DATA_FLAVOR);
                MapperDropData data = (MapperDropData) transferData;
                parent.channelEditPanel.transformerPane.addMessageBuilder(constructPath(selectedNode).toString(), data.getMapping());
            }
            else if (supportedDropFlavor == TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR)
            {
                Object transferData = tr.getTransferData(TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR);
                MessageBuilderDropData data = (MessageBuilderDropData) transferData;
                parent.channelEditPanel.transformerPane.addMessageBuilder(data.getMessageSegment(), constructPath(selectedNode).toString());
            }
            else
            {
                dtde.rejectDrop();
            }
        }
        catch (Exception e)
        {
            dtde.rejectDrop();
        }
        
    }

    public void dropActionChanged(DropTargetDragEvent dtde)
    {
        // TODO Auto-generated method stub
        
    }
    
    private StringBuilder constructPath(TreeNode node)
    {
        StringBuilder sb = new StringBuilder();
        sb.insert(0, prefix);
        
        TreeNode parent = node.getParent();
                      
        LinkedList<String> nodeQ = new LinkedList<String>();
        while (parent != null)
        {
            nodeQ.add(parent.toString().replaceAll(" \\(.*\\)", ""));
            parent = parent.getParent();
        }
        if (!nodeQ.isEmpty())
            nodeQ.removeLast();

        while (!nodeQ.isEmpty())
        {
            sb.append("['" + nodeQ.removeLast() + "']");
        }
   
        sb.append(suffix);
        
        return sb;
    }
}
