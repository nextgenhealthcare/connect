/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.util.List;

public abstract class QueuingSwingWorkerTask<T, V> {

    private String key;
    private String displayText;
    private QueuingSwingWorker<T, V> worker;

    public QueuingSwingWorkerTask(String key, String displayText) {
        this.key = key;
        this.displayText = displayText;
    }

    protected abstract T doInBackground() throws Exception;

    /*
     * This needs to be called from doInBackground, but the publish method cannot be directly called
     * here on the delegate SwingWorker because it's a protected method. We also cannot override the
     * method and expand visibility because it's a final method. So instead, we call the exposed
     * publishDelegate method, and QueuingSwingWorker will call publish instead.
     */
    protected final void publish(V... chunks) {
        if (worker != null) {
            worker.publishDelegate(chunks);
        }
    }

    protected void process(List<V> chunks) {}

    protected void done() {}

    public String getKey() {
        return key;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setWorker(QueuingSwingWorker<T, V> worker) {
        this.worker = worker;
    }
}