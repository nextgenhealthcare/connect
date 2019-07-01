/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.mbeans;

import java.util.Properties;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.server.Command;
import com.mirth.connect.server.CommandQueue;
import com.mirth.connect.server.Mirth;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;

public class MirthService implements MirthServiceMBean {
    private Mirth mirthServer = null;
    private final String EXTENSION_NAME = "Extension Manager";

    public void start() {
        if (mirthServer == null || !mirthServer.isAlive()) {
            createProperties();
            mirthServer = new Mirth();
            mirthServer.start();
        }
    }

    public void stop() {
        CommandQueue queue = CommandQueue.getInstance();
        queue.addCommand(new Command(Command.Operation.SHUTDOWN_SERVER));
    }

    private void createProperties() {
        ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
        Properties props = null;

        try {
            props = extensionController.getPluginProperties(EXTENSION_NAME);
        } catch (ControllerException e1) {
        }
        if (props == null) {
            props = new Properties();
        }
        props.setProperty("disableInstall", "true");

        try {
            extensionController.setPluginProperties(EXTENSION_NAME, props);
        } catch (ControllerException e) {
        }
    }
}
