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
