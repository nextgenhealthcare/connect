package com.mirth.connect.plugins;

import org.jdesktop.swingx.JXTaskPane;

import com.mirth.connect.client.ui.components.MirthTable;

public abstract class TaskPlugin extends ClientPlugin {

    public TaskPlugin(String name) {
        super(name);
    }
    
    public abstract void onRowSelected(MirthTable channelTable);
    
    public abstract void onRowDeselected();
    
    public abstract JXTaskPane getTaskPane();
}
