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

import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.SourceConnector;

public abstract class BatchAdaptorFactory {
    protected SourceConnector sourceConnector;
    private AtomicInteger batches = new AtomicInteger();
    private AtomicBoolean finished = new AtomicBoolean();
    private boolean useFirstReponse = false;
    private volatile String contextFactoryId;

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

    public void start() throws ConnectorTaskException, InterruptedException {
        finished.set(false);
    }

    public void stop() throws ConnectorTaskException, InterruptedException {
        finished.set(true);

        while (true) {
            synchronized (batches) {
                if (batches.get() == 0) {
                    return;
                }
            }

            Thread.sleep(100);
        }
    }

    public boolean isUseFirstReponse() {
        return useFirstReponse;
    }

    public void setUseFirstReponse(boolean useFirstReponse) {
        this.useFirstReponse = useFirstReponse;
    }

    public String getContextFactoryId() {
        return contextFactoryId;
    }

    public void setContextFactoryId(String contextFactoryId) {
        this.contextFactoryId = contextFactoryId;
    }

    public abstract BatchAdaptor createBatchAdaptor(BatchRawMessage batchRawMessage);

    public abstract void onDeploy() throws DeployException;

    public abstract void onUndeploy() throws UndeployException;

}
