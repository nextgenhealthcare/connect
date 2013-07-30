/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;

public interface DatabaseDispatcherDelegate {
    public void deploy() throws DeployException;

    public void undeploy() throws UndeployException;

    public void start() throws StartException;

    public void stop() throws StopException;

    public void halt() throws HaltException;

    /**
     * Using the given DatabaseDispatcherProperties, execute the dispatcher's query or script with
     * the given ConnectorMessage.
     */
    public Response send(DatabaseDispatcherProperties connectorProperties, ConnectorMessage connectorMessage) throws DatabaseDispatcherException, InterruptedException;
}
