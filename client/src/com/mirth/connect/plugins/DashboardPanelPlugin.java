/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins;

import java.awt.Component;
import java.util.List;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.mirth.connect.client.core.ClientException;
import com.mirth.connect.model.ChannelStatus;

public abstract class DashboardPanelPlugin extends ClientPlugin {

    private JComponent component = new JPanel();

    public DashboardPanelPlugin(String name) {
        super(name);
    }

    public void setComponent(JComponent component) {
        this.component = component;
    }

    public JComponent getComponent() {
        return component;
    }

    public void alertException(Component parentComponent, StackTraceElement[] strace, String message) {
        parent.alertException(parentComponent, strace, message);
    }

    public void alertWarning(Component parentComponent, String message) {
        parent.alertWarning(parentComponent, message);
    }

    public void alertInformation(Component parentComponent, String message) {
        parent.alertInformation(parentComponent, message);
    }

    public void alertError(Component parentComponent, String message) {
        parent.alertError(parentComponent, message);
    }

    public boolean alertOkCancel(Component parentComponent, String message) {
        return parent.alertOkCancel(parentComponent, message);
    }

    public boolean alertOption(Component parentComponent, String message) {
        return parent.alertOption(parentComponent, message);
    }

    public void setWorking(String message, boolean working) {
        parent.setWorking(message, working);
    }

    public Properties getPropertiesFromServer() throws ClientException {
        return parent.mirthClient.getPluginProperties(name);
    }

    public void setPropertiesToServer(Properties properties) throws ClientException {
        parent.mirthClient.setPluginProperties(name, properties);
    }

    // used for setting actions to be called for updating when there is no status selected
    public abstract void update();

    // used for setting actions to be called for updating when there is a status selected
    public abstract void update(List<ChannelStatus> statuses);

    public Object invoke(String method, Object object) throws ClientException {
        return parent.mirthClient.invokePluginMethod(name, method, object);
    }

    // used for starting processes in the plugin when the program is started
    public abstract void start();

    // used for stopping processes in the plugin when the program is exited
    public abstract void stop();

    // Called when establishing a new session for the user
    public abstract void reset();
}
