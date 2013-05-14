/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ConnectorEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.event.ConnectorEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class DatabaseDispatcher extends DestinationConnector {

    private DatabaseDispatcherDelegate delegate;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private Logger logger = Logger.getLogger(getClass());
    private EventController eventController = ControllerFactory.getFactory().createEventController();

    @Override
    public void onDeploy() throws DeployException {
        /*
         * A delegate object is used to handle the sending operation, since the sending logic is
         * very different depending on whether JavaScript is enabled or not
         */
        if (((DatabaseDispatcherProperties) getConnectorProperties()).isUseScript()) {
            delegate = new DatabaseDispatcherScript(this);
        } else {
            delegate = new DatabaseDispatcherQuery();
        }

        delegate.deploy();
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.IDLE));
    }

    @Override
    public void onUndeploy() throws UndeployException {
        delegate.undeploy();
    }

    @Override
    public void onStart() {}

    @Override
    public void onStop() {}

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage message) {
        DatabaseDispatcherProperties databaseDispatcherProperties = (DatabaseDispatcherProperties) connectorProperties;
        databaseDispatcherProperties.setUrl(replacer.replaceValues(databaseDispatcherProperties.getUrl(), message));
        databaseDispatcherProperties.setUsername(replacer.replaceValues(databaseDispatcherProperties.getUsername(), message));
        databaseDispatcherProperties.setPassword(replacer.replaceValues(databaseDispatcherProperties.getPassword(), message));

        List<String> paramNames = new ArrayList<String>();

        /*
         * JdbcUtils.extractParameters() will extract the Apache velocity variables from the query,
         * putting them into paramNames and replacing them in the query with ? placeholders. We then
         * call JdbcUtils.getParameters() with the list of paramNames to generate a list of
         * placeholder values using a TemplateValueReplacer along with the current ConnectorMessage.
         */
        databaseDispatcherProperties.setQuery(JdbcUtils.extractParameters(databaseDispatcherProperties.getQuery(), paramNames));
        databaseDispatcherProperties.setParameters(JdbcUtils.getParameters(paramNames, getChannelId(), message, null));
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage message) throws InterruptedException {
        DatabaseDispatcherProperties databaseDispatcherProperties = (DatabaseDispatcherProperties) connectorProperties;
        String info = "URL: " + databaseDispatcherProperties.getUrl();
        eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.READING, info));

        try {
            return delegate.send(databaseDispatcherProperties, message);
        } catch (InterruptedException e) {
            throw e;
        } catch (DatabaseDispatcherException e) {
            logger.error("An error occurred in channel \"" + ChannelController.getInstance().getDeployedChannelById(getChannelId()).getName() + "\": " + e.getMessage(), ExceptionUtils.getRootCause(e));
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.DESTINATION_CONNECTOR, connectorProperties.getName(), e.getMessage(), e));
            return new Response(Status.QUEUED, null, ErrorMessageBuilder.buildErrorResponse("Error writing to database.", e), ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_406, e.getMessage(), e));
        } finally {
            eventController.dispatchEvent(new ConnectorEvent(getChannelId(), getMetaDataId(), ConnectorEventType.IDLE));
        }
    }
}
