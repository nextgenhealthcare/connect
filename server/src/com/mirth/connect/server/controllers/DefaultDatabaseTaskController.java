/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections.MapUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.channel.Channel;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDaoFactory;
import com.mirth.connect.donkey.server.data.jdbc.QuerySource;
import com.mirth.connect.model.DatabaseTask;
import com.mirth.connect.model.DatabaseTask.Status;
import com.mirth.connect.server.ExtensionLoader;
import com.mirth.connect.server.channel.ChannelFuture;
import com.mirth.connect.server.channel.ChannelTask;
import com.mirth.connect.server.channel.ChannelTaskHandler;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;

import edu.emory.mathcs.backport.java.util.Collections;

public class DefaultDatabaseTaskController implements DatabaseTaskController {

    private static final String TASK_REMOVE_OLD_CHANNEL = "removeOldChannelTable";
    private static final String TASK_REMOVE_OLD_MESSAGE = "removeOldMessageTable";
    private static final String TASK_REMOVE_OLD_ATTACHMENT = "removeOldAttachmentTable";
    private static final String TASK_ADD_D_MM_INDEX3 = "addMetadataIndex3";

    private static DatabaseTaskController instance = null;
    private Logger logger = Logger.getLogger(getClass());
    private EngineController engineController = ControllerFactory.getFactory().createEngineController();
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();

    // Shared data
    private DatabaseTask currentTask;
    private AtomicBoolean cancelled = new AtomicBoolean(false);
    private ReadWriteLock taskReadWriteLock = new ReentrantReadWriteLock(true);
    private Lock taskRunLock = new ReentrantLock(true);

    private DefaultDatabaseTaskController() {}

    public static DatabaseTaskController create() {
        synchronized (DefaultDatabaseTaskController.class) {
            if (instance == null) {
                instance = ExtensionLoader.getInstance().getControllerInstance(DatabaseTaskController.class);

                if (instance == null) {
                    instance = new DefaultDatabaseTaskController();
                }
            }

            return instance;
        }
    }

    @Override
    public Map<String, DatabaseTask> getDatabaseTasks() throws Exception {
        Map<String, DatabaseTask> tasks = new HashMap<String, DatabaseTask>();
        SqlSession session = SqlConfig.getSqlSessionManager().openSession();

        try {
            Connection connection = session.getConnection();

            // Only add the task to remove OLD_CHANNEL if OLD_MESSAGE has already been dropped
            if (DatabaseUtil.tableExists(connection, "OLD_MESSAGE")) {
                DatabaseTask task = new DatabaseTask(TASK_REMOVE_OLD_MESSAGE, "Remove Old 2.x Message Table", "Remove the OLD_MESSAGE table which was renamed as part of the upgrade from 2.x to 3.x.");
                logger.debug("Adding database task: " + task.getName());
                tasks.put(task.getId(), task);
            } else if (DatabaseUtil.tableExists(connection, "OLD_CHANNEL")) {
                DatabaseTask task = new DatabaseTask(TASK_REMOVE_OLD_CHANNEL, "Remove Old 2.x Channel Table", "Remove the OLD_CHANNEL table which was renamed as part of the upgrade from 2.x to 3.x.");
                logger.debug("Adding database task: " + task.getName());
                tasks.put(task.getId(), task);
            }

            if (DatabaseUtil.tableExists(connection, "OLD_ATTACHMENT")) {
                DatabaseTask task = new DatabaseTask(TASK_REMOVE_OLD_ATTACHMENT, "Remove Old 2.x Attachment Table", "Remove the OLD_ATTACHMENT table which was renamed as part of the upgrade from 2.x to 3.x.");
                logger.debug("Adding database task: " + task.getName());
                tasks.put(task.getId(), task);
            }

            DonkeyDao dao = Donkey.getInstance().getDaoFactory().getDao();
            try {
                Map<String, Long> localChannelIdMap = dao.getLocalChannelIds();
                Map<String, String> affectedChannels = new HashMap<String, String>();

                for (String channelId : localChannelIdMap.keySet()) {
                    long localChannelId = localChannelIdMap.get(channelId);
                    String tableName = "D_MM" + localChannelId;

                    if (!DatabaseUtil.indexExists(connection, tableName, tableName + "_INDEX3")) {
                        affectedChannels.put(channelId, getChannelName(channelId));
                    }
                }

                if (MapUtils.isNotEmpty(affectedChannels)) {
                    StringBuilder confirmationMessage = new StringBuilder("This index will only be created on channels that are stopped. Are you sure you wish to continue?");

                    DatabaseTask task = new DatabaseTask(TASK_ADD_D_MM_INDEX3, "Add Metadata Index", "Add index (ID, STATUS, SERVER_ID) on the message metadata table to improve queue performance.", confirmationMessage.toString());
                    task.setAffectedChannels(affectedChannels);
                    logger.debug("Adding migration task: " + task.getName());
                    tasks.put(task.getId(), task);
                }
            } finally {
                if (dao != null) {
                    dao.close();
                }
            }
        } finally {
            session.close();
        }

        DatabaseTask currentTask = getCurrentTask();
        if (currentTask != null) {
            tasks.put(currentTask.getId(), currentTask);
        }

        return tasks;
    }

    private DatabaseTask getCurrentTask() throws Exception {
        taskReadWriteLock.readLock().lock();
        try {
            return currentTask;
        } finally {
            taskReadWriteLock.readLock().unlock();
        }
    }

    @Override
    public String runDatabaseTask(DatabaseTask task) throws Exception {
        DatabaseTask databaseTask = getCurrentTask();

        if (databaseTask == null && taskRunLock.tryLock()) {
            try {
                startTask(task);

                if (task.getId().equals(TASK_REMOVE_OLD_CHANNEL)) {
                    executeUpdate("DROP TABLE OLD_CHANNEL");
                    return "Table OLD_CHANNEL successfully dropped.";
                } else if (task.getId().equals(TASK_REMOVE_OLD_MESSAGE)) {
                    executeUpdate("DROP TABLE OLD_MESSAGE");
                    return "Table OLD_MESSAGE successfully dropped.";
                } else if (task.getId().equals(TASK_REMOVE_OLD_ATTACHMENT)) {
                    executeUpdate("DROP TABLE OLD_ATTACHMENT");
                    return "Table OLD_ATTACHMENT successfully dropped.";
                } else if (task.getId().equals(TASK_ADD_D_MM_INDEX3)) {
                    DonkeyDaoFactory daoFactory = Donkey.getInstance().getDaoFactory();

                    if (daoFactory instanceof JdbcDaoFactory) {
                        Map<String, Long> localChannelIds = new HashMap<String, Long>();
                        DonkeyDao dao = daoFactory.getDao();
                        try {
                            localChannelIds = dao.getLocalChannelIds();
                        } finally {
                            if (dao != null) {
                                dao.close();
                            }
                        }

                        Map<String, Long> channelsToIndex = new HashMap<String, Long>();
                        Map<String, String> affectedChannels = new HashMap<String, String>();

                        SqlSession session = SqlConfig.getSqlSessionManager().openSession();

                        try {
                            Connection connection = session.getConnection();

                            for (Entry<String, Long> entry : localChannelIds.entrySet()) {
                                String channelId = entry.getKey();
                                long localChannelId = entry.getValue();
                                String tableName = "D_MM" + localChannelId;
                                String indexName = tableName + "_INDEX3";

                                if (!DatabaseUtil.indexExists(connection, tableName, indexName)) {
                                    channelsToIndex.put(channelId, localChannelId);
                                    affectedChannels.put(channelId, getChannelName(channelId));
                                }
                            }
                        } finally {
                            session.close();
                        }

                        taskReadWriteLock.writeLock().lock();
                        try {
                            currentTask.setAffectedChannels(new HashMap<String, String>(affectedChannels));
                        } finally {
                            taskReadWriteLock.writeLock().unlock();
                        }

                        class ChannelStoppedException extends Exception {
                        }

                        class AddIndexChannelTaskHandler extends ChannelTaskHandler {

                            private Map<String, String> affectedChannels;

                            public AddIndexChannelTaskHandler(Map<String, String> affectedChannels) {
                                this.affectedChannels = affectedChannels;
                            }

                            @Override
                            public void taskCompleted(String channelId, Integer metaDataId) {
                                affectedChannels.remove(channelId);

                                taskReadWriteLock.writeLock().lock();
                                try {
                                    currentTask.setAffectedChannels(new HashMap<String, String>(affectedChannels));
                                } finally {
                                    taskReadWriteLock.writeLock().unlock();
                                }
                            }

                            @Override
                            public void taskErrored(String channelId, Integer metaDataId, Exception e) {
                                if (!(e instanceof ChannelStoppedException)) {
                                    logger.error("Unable to add index to channel " + channelId + ".", e);
                                }
                            }

                            @Override
                            public void taskCancelled(String channelId, Integer metaDataId, CancellationException e) {
                                logger.error("Unable to add index to channel " + channelId + ".", e);
                            }
                        }

                        AddIndexChannelTaskHandler taskHandler = new AddIndexChannelTaskHandler(affectedChannels);
                        final QuerySource querySource = ((JdbcDaoFactory) daoFactory).getQuerySource();

                        for (Entry<String, Long> entry : channelsToIndex.entrySet()) {
                            if (isCancelled()) {
                                break;
                            }

                            final String channelId = entry.getKey();
                            final long localChannelId = entry.getValue();
                            final String tableName = "D_MM" + localChannelId;
                            final String indexName = tableName + "_INDEX3";

                            ChannelTask addIndexTask = new ChannelTask(channelId) {
                                @Override
                                public Void execute() throws Exception {
                                    Channel channel = engineController.getDeployedChannel(channelId);
                                    if (channel != null && channel.getCurrentState() != DeployedState.STOPPED) {
                                        throw new ChannelStoppedException();
                                    }

                                    Map<String, Object> values = new HashMap<String, Object>();
                                    values.put("localChannelId", localChannelId);
                                    String query = querySource.getQuery("createConnectorMessageTableIndex3", values);

                                    if (query != null) {
                                        logger.debug("Adding index " + indexName + " on table " + tableName + ".");
                                        executeUpdate(query);
                                    } else {
                                        throw new Exception("Error adding index: Update statement not found for database type: " + Donkey.getInstance().getConfiguration().getDatabaseProperties().getProperty("database"));
                                    }

                                    return null;
                                }
                            };

                            List<ChannelFuture> futures = engineController.submitTasks(Collections.singletonList(addIndexTask), taskHandler);

                            if (futures.size() > 0) {
                                futures.get(0).get();
                            }
                        }

                        int totalCount = channelsToIndex.size();
                        int successCount = totalCount - affectedChannels.size();
                        return "<html>" + successCount + " out of " + channelsToIndex.size() + " indices successfully added.</html>";
                    } else {
                        throw new Exception("Unable to perform task: DAO type is not JDBC.");
                    }
                } else {
                    throw new Exception("Unknown task ID: " + task.getId());
                }
            } finally {
                stopTask();
            }
        } else {
            throw new Exception("Another database task is already running: " + databaseTask.getName());
        }
    }

    @Override
    public void cancelDatabaseTask(DatabaseTask task) throws Exception {
        DatabaseTask databaseTask = getCurrentTask();
        if (databaseTask == null || !databaseTask.getId().equals(task.getId())) {
            throw new Exception("Task \"" + task.getName() + "\" is not currently running.");
        }

        taskReadWriteLock.writeLock().lock();
        try {
            cancelled.set(true);
        } finally {
            taskReadWriteLock.writeLock().unlock();
        }
    }

    private void startTask(DatabaseTask task) {
        taskReadWriteLock.writeLock().lock();
        try {
            currentTask = task;
            currentTask.setStatus(Status.RUNNING);
            currentTask.setStartDateTime(Calendar.getInstance());
            cancelled.set(false);
        } finally {
            taskReadWriteLock.writeLock().unlock();
        }
    }

    private void stopTask() {
        try {
            taskReadWriteLock.writeLock().lock();
            try {
                currentTask = null;
                cancelled.set(false);
            } finally {
                taskReadWriteLock.writeLock().unlock();
            }
        } finally {
            taskRunLock.unlock();
        }
    }

    private boolean isCancelled() {
        taskReadWriteLock.readLock().lock();
        try {
            return cancelled.get();
        } finally {
            taskReadWriteLock.readLock().unlock();
        }
    }

    private void executeUpdate(String sql) throws SQLException {
        SqlSession session = SqlConfig.getSqlSessionManager().openSession();
        try {
            session.getConnection().createStatement().executeUpdate(sql);
        } finally {
            session.close();
        }
    }

    private String getChannelName(String channelId) {
        com.mirth.connect.model.Channel model = channelController.getDeployedChannelById(channelId);
        if (model == null) {
            model = channelController.getChannelById(channelId);
        }
        return model != null ? model.getName() : null;
    }
}