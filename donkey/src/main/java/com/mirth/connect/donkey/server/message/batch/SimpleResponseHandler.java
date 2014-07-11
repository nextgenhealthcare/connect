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

public class SimpleResponseHandler extends ResponseHandler {

    @Override
    public void responseProcess() throws Exception {}

    @Override
    public void responseError(ChannelException e) {}

}
