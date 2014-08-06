package com.mirth.connect.plugins.datapruner;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ExtensionController;

public abstract class DataPrunerController {
    private static DataPrunerController instance;

    public static DataPrunerController getInstance() {
        synchronized (DataPrunerController.class) {
            Logger logger = Logger.getLogger(DataPrunerController.class);

            /*
             * Eventually, plugins will be able to specify controller classes to override, see
             * MIRTH-3351
             */
            if (instance == null) {
                ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
                extensionController.loadExtensions();

                if (extensionController.getPluginMetaData().containsKey("Basic Clustering") || extensionController.getPluginMetaData().containsKey("Advanced Clustering")) {
                    try {
                        String clusterDataPrunerController = "com.mirth.connect.plugins.clustering.server.datapruner.ClusterDataPrunerController";
                        instance = (DataPrunerController) Class.forName(clusterDataPrunerController).newInstance();
                        logger.debug("using data pruner controller: " + clusterDataPrunerController);
                    } catch (Exception e) {
                    }
                }

                if (instance == null) {
                    instance = new DefaultDataPrunerController();
                    logger.debug("using default data pruner controller");
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
