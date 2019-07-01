/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import java.util.Map;
import java.util.Properties;

import com.mirth.connect.server.ExtensionLoader;

public abstract class DataPrunerController {
    private static DataPrunerController instance;

    public static DataPrunerController getInstance() {
        synchronized (DataPrunerController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(DataPrunerController.class);

                if (instance == null) {
                    instance = new DefaultDataPrunerController();
                }
            }

            return instance;
        }
    }

    public abstract void init(Properties properties) throws DataPrunerException;

    public abstract void start() throws DataPrunerException;

    public abstract void stop(boolean waitForJobsToComplete) throws DataPrunerException;

    public abstract void startPruner() throws DataPrunerException;

    public abstract void stopPruner() throws DataPrunerException, InterruptedException;

    public abstract void update(Properties properties) throws DataPrunerException;

    public abstract boolean isStarted() throws DataPrunerException;

    public abstract Map<String, String> getStatusMap() throws DataPrunerException;

    public abstract DataPrunerStatus getPrunerStatus() throws DataPrunerException;

    public abstract boolean isPrunerRunning() throws DataPrunerException;
}
