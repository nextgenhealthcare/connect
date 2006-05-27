package com.webreach.mirth.client;


public class JButton extends javax.swing.JButton {
    private Frame parent;
    
    public JButton()
    {
        super();
    }
    
    public JButton(Frame parent) {
        super();
        this.parent = parent;
        this.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPressed(evt);
            }
        });
    }
    
    public void buttonPressed(java.awt.event.ActionEvent evt)
    {
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(true);
    }
}