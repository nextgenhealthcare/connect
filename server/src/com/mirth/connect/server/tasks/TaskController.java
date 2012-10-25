/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.tasks;

import java.util.List;

public class TaskController {
    private static TaskController instance;
    
    public static TaskController getInstance() {
        synchronized (TaskController.class) {
            if (instance == null) {
                instance = new TaskController();
            }
        }
        
        return instance;
    }
    
    public List<Task> getPendingTasks() {
        return null;
    }

    public List<ReprocessingTask> getNextReprocessingTasks(int bufferSize) {
        // TODO Auto-generated method stub
        return null;
    }
}
