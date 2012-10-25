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

public class TaskExecutor implements Runnable {
    private static int BUFFER_SIZE = 100;
    
    @Override
    public void run() {
        List<Task> tasks = TaskController.getInstance().getPendingTasks();

        for (Task task : tasks) {
            switch (task.getTaskType()) {
                case REPROCESS_MESSAGES:
                    executeReprocessingTasks(task);
                    break;
            }
        }
    }
    
    private void executeReprocessingTasks(Task task) {
        List<ReprocessingTask> reprocessingTasks = TaskController.getInstance().getNextReprocessingTasks(BUFFER_SIZE);
        
        while (!reprocessingTasks.isEmpty()) {
            for (ReprocessingTask reprocessingTask : reprocessingTasks) {
                executeReprocessingTask(reprocessingTask);
            }
            
            reprocessingTasks = TaskController.getInstance().getNextReprocessingTasks(BUFFER_SIZE);
        }
    }
    
    private void executeReprocessingTask(ReprocessingTask reprocessingTask) {
        
    }
}
