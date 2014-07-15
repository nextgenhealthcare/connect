/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.message.batch;

import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;

public abstract class ResponseHandler {

    protected DispatchResult dispatchResult;
    private DispatchResult resultForResponse;
    private boolean useFirstResponse;

    public ResponseHandler() {}

    public DispatchResult getDispatchResult() {
        return dispatchResult;
    }

    public void setDispatchResult(DispatchResult dispatchResult) {
        this.dispatchResult = dispatchResult;
        if (!useFirstResponse || resultForResponse == null) {
            resultForResponse = dispatchResult;
        }
    }

    public DispatchResult getResultForResponse() {
        return resultForResponse;
    }

    public void setUseFirstResponse(boolean useFirstResponse) {
        this.useFirstResponse = useFirstResponse;
    }

    public abstract void responseProcess() throws Exception;

    public abstract void responseError(ChannelException e);

}
