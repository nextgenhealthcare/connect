/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.buffered;

public class DaoTask {
    private DaoTaskType taskType;
    private Object[] parameters;

    public DaoTask(DaoTaskType taskType, Object[] parameters) {
        this.taskType = taskType;
        this.parameters = parameters;
    }

    public DaoTaskType getTaskType() {
        return taskType;
    }

    public Object[] getParameters() {
        return parameters;
    }
}
