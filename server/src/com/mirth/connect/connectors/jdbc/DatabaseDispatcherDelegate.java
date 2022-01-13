/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.ConnectorTaskException;

public interface DatabaseDispatcherDelegate {
    public void deploy() throws ConnectorTaskException;

    public void undeploy() throws ConnectorTaskException;

    public void start() throws ConnectorTaskException;

    public void stop() throws ConnectorTaskException;

    public void halt() throws ConnectorTaskException;

    /**
     * Using the given DatabaseDispatcherProperties, execute the dispatcher's query or script with
     * the given ConnectorMessage.
     */
    public Response send(DatabaseDispatcherProperties connectorProperties, ConnectorMessage connectorMessage) throws DatabaseDispatcherException, InterruptedException;
}
