/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.mirth.connect.client.core.Operations;
import com.mirth.connect.client.core.TaskConstants;
import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.util.messagewriter.MessageWriterOptions;

public class DataPrunerService implements ServicePlugin {
    public static final String PLUGINPOINT = "Data Pruner";
    private static final int DEFAULT_PRUNING_BLOCK_SIZE = 50000;

    private DataPrunerController dataPrunerController = DataPrunerController.getInstance();
    private ObjectXMLSerializer serializer = ObjectXMLSerializer.getInstance();
    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public String getPluginPointName() {
        return PLUGINPOINT;
    }

    @Override
    public void start() {
        try {
            dataPrunerController.start();
        } catch (DataPrunerException e) {
            logger.error("Failed to start data pruner service.", e);
        }
    }

    @Override
    public void stop() {
        try {
            dataPrunerController.stop(false);
        } catch (DataPrunerException e) {
            logger.error("Failed to stop data pruner service.", e);
        }
    }

    @Override
    public void init(Properties properties) {
        try {
            dataPrunerController.init(properties);
        } catch (DataPrunerException e) {
            logger.error("Failed to initialize data pruner service.", e);
        }
    }

    @Override
    public void update(Properties properties) {
        try {
            dataPrunerController.update(properties);
        } catch (DataPrunerException e) {
            logger.error("Failed to reschedule the data pruner.", e);
        }
    }

    @Override
    public Object invoke(String method, Object object, String sessionId) {
        try {
            if (method.equals("getStatus")) {
                return dataPrunerController.getStatusMap();
            } else if (method.equals("start")) {
                dataPrunerController.startPruner();
                return dataPrunerController.getPrunerStatus().getStartTime();
            } else if (method.equals("stop")) {
                try {
                    dataPrunerController.stopPruner();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Stopped waiting for the data pruner to stop, due to a thread interruption.", e);
                }
            }
        } catch (DataPrunerException e) {
            logger.error("Failed to invoke data pruner service method: " + method, e);
        }

        return null;
    }

    @Override
    public Properties getDefaultProperties() {
        Properties properties = new Properties();
        properties.put("interval", "disabled");
        properties.put("time", "12:00 AM");
        properties.put("pruningBlockSize", String.valueOf(DEFAULT_PRUNING_BLOCK_SIZE));
        properties.put("archiveEnabled", serializer.serialize(false));
//        properties.put("includeAttachments", serializer.serialize(false));
        properties.put("archiverOptions", serializer.serialize(new MessageWriterOptions()));
        properties.put("pruneEvents", Boolean.toString(false));
        properties.put("maxEventAge", "");
        return properties;
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        ExtensionPermission viewPermission = new ExtensionPermission(PLUGINPOINT, "View Settings", "Displays the Data Pruner settings.", new String[] { Operations.PLUGIN_PROPERTIES_GET.getName() }, new String[] { TaskConstants.SETTINGS_REFRESH });
        ExtensionPermission savePermission = new ExtensionPermission(PLUGINPOINT, "Save Settings", "Allows changing the Data Pruner settings.", new String[] { Operations.PLUGIN_PROPERTIES_SET.getName() }, new String[] { TaskConstants.SETTINGS_SAVE });

        return new ExtensionPermission[] { viewPermission, savePermission };
    }
}
