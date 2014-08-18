/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.Map;

import com.mirth.connect.model.DatabaseTask;

public interface DatabaseTaskController {

    public Map<String, DatabaseTask> getDatabaseTasks() throws Exception;

    public String runDatabaseTask(DatabaseTask task) throws Exception;

    public void cancelDatabaseTask(DatabaseTask task) throws Exception;
}