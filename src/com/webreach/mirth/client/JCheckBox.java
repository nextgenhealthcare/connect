package com.webreach.mirth.client;


public class JCheckBox extends javax.swing.JCheckBox {
    private Frame parent;
    
    public JCheckBox()
    {
        super();
    }
    
    public JCheckBox(Frame parent) {
        super();
        this.parent = parent;
        this.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkBoxChanged(evt);
            }
        });
    }
    
    public void checkBoxChanged(java.awt.event.ActionEvent evt)
    {
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(true);
    }
}