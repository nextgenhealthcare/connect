package com.webreach.mirth.client;


public class JRadioButton extends javax.swing.JRadioButton {
    private Frame parent;
    
    public JRadioButton()
    {
        super();
    }
    
    public JRadioButton(Frame parent) {
        super();
        this.parent = parent;
        this.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioButtonChanged(evt);
            }
        });
    }
    
    public void radioButtonChanged(java.awt.event.ActionEvent evt)
    {
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(true);
    }
}