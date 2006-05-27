package com.webreach.mirth.client;

import java.awt.event.KeyEvent;

public class JTextArea extends javax.swing.JTextArea
{
    private Frame parent;
    
    public JTextArea()
    {
        super();
    }
    
    public JTextArea(Frame parent)
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