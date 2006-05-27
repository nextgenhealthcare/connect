package com.webreach.mirth.client;


public class JComboBox extends javax.swing.JComboBox {
    private Frame parent;
    
    public JComboBox()
    {
        super();
    }
    
    public JComboBox(Frame parent) {
        super();
        this.parent = parent;
        this.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxChanged(evt);
            }
        });
    }
    
    public void comboBoxChanged(java.awt.event.ActionEvent evt)
    {
        parent.channelEditTasks.getContentPane().getComponent(0).setVisible(true);
    }
}