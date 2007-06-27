package com.webreach.mirth.client.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.jdesktop.swingx.JXTree;

public class Test extends JFrame
{
    JXTree jt = new JXTree(new MirthTreeNode("Root"));
    MyFilter mf = new MyFilter();
    JTextArea jta = new JTextArea("Filter");
    FilterTreeModel ftm = new FilterTreeModel((MirthTreeNode) jt.getModel().getRoot(), mf);
    
    public Test()
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container content = getContentPane();
        ((MirthTreeNode) ftm.getRoot()).add(new MirthTreeNode("Child1"));
        ((MirthTreeNode) ftm.getRoot()).add(new MirthTreeNode("Child2"));
        ((MirthTreeNode) ftm.getRoot()).add(new MirthTreeNode("Child3"));
        
        ((MirthTreeNode)((MirthTreeNode) ftm.getRoot()).getChildAt(0)).add(new MirthTreeNode("Child1"));
        ((MirthTreeNode)((MirthTreeNode) ftm.getRoot()).getChildAt(0)).add(new MirthTreeNode("Child1"));
        ((MirthTreeNode)((MirthTreeNode) ftm.getRoot()).getChildAt(0)).add(new MirthTreeNode("Child3"));
        
        ((MirthTreeNode)((MirthTreeNode) ftm.getRoot()).getChildAt(1)).add(new MirthTreeNode("Child1"));
        ((MirthTreeNode)((MirthTreeNode) ftm.getRoot()).getChildAt(1)).add(new MirthTreeNode("Child2"));
        ((MirthTreeNode)((MirthTreeNode) ftm.getRoot()).getChildAt(1)).add(new MirthTreeNode("Child2"));
        
        ((MirthTreeNode)((MirthTreeNode) ftm.getRoot()).getChildAt(2)).add(new MirthTreeNode("Child1"));
        ((MirthTreeNode)((MirthTreeNode) ftm.getRoot()).getChildAt(2)).add(new MirthTreeNode("Child3"));
        ((MirthTreeNode)((MirthTreeNode) ftm.getRoot()).getChildAt(2)).add(new MirthTreeNode("Child3"));
        
        jt.setModel(ftm);
        jta.setText("");
        content.add(new JScrollPane(jt), BorderLayout.CENTER);
        content.add(jta, BorderLayout.SOUTH);
        jta.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent arg0)
            {
            }

            public void keyReleased(KeyEvent e)
            {
                if(jta.getText().length() > 0)
                    ftm.setFiltered(false, ftm.getRoot());
                else
                    ftm.setFiltered(true, ftm.getRoot());
            }

            public void keyTyped(KeyEvent e)
            {
            }
        });
        setSize(300, 300);
        setVisible(true);
    }

    public static void main(String[] args)
    {
        new Test();
    }
    
    class FilterTreeModel extends DefaultTreeModel
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

        public void setFiltered(boolean pass, TreeNode tn)
        {
            filter.setFiltered(pass);
            
            int realCount = super.getChildCount(tn);
            
            if(realCount == 0)
                return;
            
            for (int i = 0; i < realCount; i++)
            {
                setFiltered(pass, tn.getChildAt(i));
            }
            
            int count = tn.getChildCount();
            Object[] path = { tn };
            int[] childIndices = new int[count];
            Object[] children = new Object[count];
            
            for (int i = 0; i < count; i++)
            {
                childIndices[i] = i;
                children[i] = tn.getChildAt(i);
                setFiltered(pass, tn.getChildAt(i));
            }
            fireTreeStructureChanged(this, path, childIndices, children);
        }

        public int getChildCount(Object parent)
        {
            int realCount = super.getChildCount(parent);
            int filterCount = 0;
            for (int i = 0; i < realCount; i++)
            {
                MirthTreeNode mtn = (MirthTreeNode) super.getChild(parent, i);
                if (filter.pass(mtn))
                {
                    filterCount++;
                }
            }
            
            return filterCount;
        }

        public Object getChild(Object parent, int index)
        {
            int cnt = -1;
            for (int i = 0; i < super.getChildCount(parent); i++)
            {
                MirthTreeNode child = (MirthTreeNode) super.getChild(parent, i);
                if (filter.pass(child))//((MirthTreeNode)parent).isFiltered())
                {
                    child.setFiltered(true);
                    cnt++;
                }
                else
                {
                    child.setFiltered(false);
                }
//                if(cnt >= 0)
//                    ((MirthTreeNode)parent).setFiltered(true);
                if (cnt == index)
                    return child;
            }
            return null;
        }
    }
    
    
    class MirthTreeNode extends DefaultMutableTreeNode
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
    
    interface Filter
    {
        public boolean pass(Object obj);

        public void setFiltered(boolean pass);

        public boolean isFiltered();
    }

    class MyFilter implements Filter
    {
        boolean pass = true;

        public boolean pass(Object obj)
        {
            if (pass)
                return true;
            String s = obj.toString();
            return s.indexOf(jta.getText()) != -1;
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