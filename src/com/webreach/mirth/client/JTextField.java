package com.webreach.mirth.client;

import java.awt.event.KeyEvent;

public class JTextField extends javax.swing.JTextField
{
    private Frame parent;
    
    public JTextField()
    {
        super();
    }
    
    public JTextField(Frame parent)
    {
        super();
        this.parent = parent;
    }
    
    public void processKeyEvent(KeyEvent ev)
    {
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(true);
        super.processKeyEvent(ev);
    }
}