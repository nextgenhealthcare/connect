package com.mirth.connect.connectors.jdbc;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.UndeployException;

public interface DatabaseDispatcherDelegate {
    public void deploy() throws DeployException;

    public void undeploy() throws UndeployException;

    /**
     * Using the given DatabaseDispatcherProperties, execute the dispatcher's query or script with
     * the given ConnectorMessage.
     */
    public Response send(DatabaseDispatcherProperties connectorProperties, ConnectorMessage connectorMessage) throws DatabaseDispatcherException, InterruptedException;
}
