/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.message.batch;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.SourceConnector;

public abstract class BatchAdaptorFactory {
    protected SourceConnector sourceConnector;
    private AtomicInteger batches = new AtomicInteger();
    private AtomicBoolean finished = new AtomicBoolean();

    public BatchAdaptorFactory(SourceConnector sourceConnector) {
        this.sourceConnector = sourceConnector;
    }

    public boolean startBatch() throws BatchMessageException {
        synchronized (batches) {
            if (!finished.get()) {
                batches.incrementAndGet();
                return true;
            }
        }

        return false;
    }

    public void finishBatch() {
        batches.decrementAndGet();
    }

    public void start() throws StartException {
        finished.set(false);
    }

    public void stop() throws StopException {
        finished.set(true);

        while (true) {
            synchronized (batches) {
                if (batches.get() == 0) {
                    return;
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new StopException(e);
            }
        }
    }

    public abstract BatchAdaptor createBatchAdaptor(BatchMessageSource batchMessageSource);

    public abstract void onDeploy() throws DeployException;

    public abstract void onUndeploy() throws UndeployException;

}
