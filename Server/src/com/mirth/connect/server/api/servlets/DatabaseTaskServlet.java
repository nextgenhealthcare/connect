/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.api.servlets;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.collections4.MapUtils;

import com.mirth.connect.client.core.api.MirthApiException;
import com.mirth.connect.client.core.api.servlets.DatabaseTaskServletInterface;
import com.mirth.connect.model.DatabaseTask;
import com.mirth.connect.server.api.MirthServlet;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.DatabaseTaskController;

public class DatabaseTaskServlet extends MirthServlet implements DatabaseTaskServletInterface {

    private static final DatabaseTaskController databaseTaskController = ControllerFactory.getFactory().createDatabaseTaskController();

    public DatabaseTaskServlet(@Context HttpServletRequest request, @Context SecurityContext sc) {
        super(request, sc);
    }

    @Override
    public Map<String, DatabaseTask> getDatabaseTasks() {
        try {
            return databaseTaskController.getDatabaseTasks();
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public DatabaseTask getDatabaseTask(String databaseTaskId) {
        Map<String, DatabaseTask> tasks;
        try {
            tasks = databaseTaskController.getDatabaseTasks();
        } catch (Exception e) {
            throw new MirthApiException(e);
        }

        if (MapUtils.isEmpty(tasks) || !tasks.containsKey(databaseTaskId)) {
            throw new MirthApiException(Status.NOT_FOUND);
        }
        return tasks.get(databaseTaskId);
    }

    @Override
    public String runDatabaseTask(String databaseTaskId) {
        try {
            return databaseTaskController.runDatabaseTask(databaseTaskId);
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }

    @Override
    public void cancelDatabaseTask(String databaseTaskId) {
        try {
            databaseTaskController.cancelDatabaseTask(databaseTaskId);
        } catch (Exception e) {
            throw new MirthApiException(e);
        }
    }
}