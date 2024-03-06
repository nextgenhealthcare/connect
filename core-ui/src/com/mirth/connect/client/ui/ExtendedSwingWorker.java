package com.mirth.connect.client.ui;

import java.awt.event.ActionListener;

import javax.swing.SwingWorker;

public abstract class ExtendedSwingWorker<T, V> extends SwingWorker<T, V> {

    protected ActionListener actionListener;
    
    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }
}
