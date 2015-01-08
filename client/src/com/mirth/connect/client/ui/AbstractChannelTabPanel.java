package com.mirth.connect.client.ui;

import javax.swing.JPanel;

import com.mirth.connect.model.Channel;

public abstract class AbstractChannelTabPanel extends JPanel {
    
    public abstract void load(Channel channel);
    
    public abstract void save(Channel channel);
}