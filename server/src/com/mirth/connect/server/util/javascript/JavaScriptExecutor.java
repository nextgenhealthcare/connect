/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util.javascript;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class JavaScriptExecutor<T> {
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public T execute(JavaScriptTask<T> task) throws JavaScriptExecutorException, InterruptedException {
        Future<T> future = executor.submit(task);
        
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw new JavaScriptExecutorException(e.getCause());
        } catch (InterruptedException e) {
            future.cancel(true);
            task.getContextFactory().setRunning(false);
            // TODO wait for the task thread to complete before exiting?
            throw e;
        }
    }
}
