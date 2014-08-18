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
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.model.DatabaseTask;
import com.mirth.connect.model.DatabaseTask.Status;
import com.mirth.connect.server.util.DatabaseUtil;
import com.mirth.connect.server.util.SqlConfig;

public class DefaultDatabaseTaskController implements DatabaseTaskController {

    private static final String TASK_REMOVE_OLD_CHANNEL = "removeOldChannelTable";
    private static final String TASK_REMOVE_OLD_MESSAGE = "removeOldMessageTable";
    private static final String TASK_REMOVE_OLD_ATTACHMENT = "removeOldAttachmentTable";

    private static DatabaseTaskController instance = null;
    private Logger logger = Logger.getLogger(getClass());

    // Shared data
    private DatabaseTask currentTask;
    private ReadWriteLock taskReadWriteLock = new ReentrantReadWriteLock(true);
    private Lock taskRunLock = new ReentrantLock(true);

    private DefaultDatabaseTaskController() {}

    public static DatabaseTaskController create() {
        synchronized (DefaultDatabaseTaskController.class) {
            if (instance == null) {
                instance = new DefaultDatabaseTaskController();
            }

            return instance;
        }
    }

    @Override
    public Map<String, DatabaseTask> getDatabaseTasks() throws Exception {
        Map<String, DatabaseTask> tasks = new HashMap<String, DatabaseTask>();
        SqlConfig.getSqlSessionManager().startManagedSession();
        Connection connection = SqlConfig.getSqlSessionManager().getConnection();

        try {
            if (DatabaseUtil.tableExists(connection, "OLD_CHANNEL")) {
                DatabaseTask task = new DatabaseTask(TASK_REMOVE_OLD_CHANNEL, "Remove Old 2.x Channel Table", "Remove the OLD_CHANNEL table which was renamed as part of the upgrade from 2.x to 3.x.");
                logger.debug("Adding database task: " + task.getName());
                tasks.put(task.getId(), task);
            }

            if (DatabaseUtil.tableExists(connection, "OLD_MESSAGE")) {
                DatabaseTask task = new DatabaseTask(TASK_REMOVE_OLD_MESSAGE, "Remove Old 2.x Message Table", "Remove the OLD_MESSAGE table which was renamed as part of the upgrade from 2.x to 3.x.");
                logger.debug("Adding database task: " + task.getName());
                tasks.put(task.getId(), task);
            }

            if (DatabaseUtil.tableExists(connection, "OLD_ATTACHMENT")) {
                DatabaseTask task = new DatabaseTask(TASK_REMOVE_OLD_ATTACHMENT, "Remove Old 2.x Attachment Table", "Remove the OLD_ATTACHMENT table which was renamed as part of the upgrade from 2.x to 3.x.");
                logger.debug("Adding database task: " + task.getName());
                tasks.put(task.getId(), task);
            }
        } finally {
            if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                SqlConfig.getSqlSessionManager().close();
            }
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
                SqlConfig.getSqlSessionManager().startManagedSession();
                Connection connection = SqlConfig.getSqlSessionManager().getConnection();
                Statement statement = null;

                try {
                    statement = connection.createStatement();

                    if (task.getId().equals(TASK_REMOVE_OLD_CHANNEL)) {
                        statement.executeUpdate("DROP TABLE OLD_CHANNEL");
                        return "Table OLD_CHANNEL successfully dropped.";
                    } else if (task.getId().equals(TASK_REMOVE_OLD_MESSAGE)) {
                        statement.executeUpdate("DROP TABLE OLD_MESSAGE");
                        return "Table OLD_MESSAGE successfully dropped.";
                    } else if (task.getId().equals(TASK_REMOVE_OLD_ATTACHMENT)) {
                        statement.executeUpdate("DROP TABLE OLD_ATTACHMENT");
                        return "Table OLD_ATTACHMENT successfully dropped.";
                    } else {
                        throw new Exception("Unknown task ID: " + task.getId());
                    }
                } finally {
                    DbUtils.closeQuietly(statement);
                    if (SqlConfig.getSqlSessionManager().isManagedSessionStarted()) {
                        SqlConfig.getSqlSessionManager().close();
                    }
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
            // Nothing to do yet for cancelling
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
        } finally {
            taskReadWriteLock.writeLock().unlock();
        }
    }

    private void stopTask() {
        try {
            taskReadWriteLock.writeLock().lock();
            try {
                currentTask = null;
            } finally {
                taskReadWriteLock.writeLock().unlock();
            }
        } finally {
            taskRunLock.unlock();
        }
    }
}