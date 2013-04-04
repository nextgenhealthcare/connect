/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jms;

import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ErrorConstants;
import com.mirth.connect.util.ErrorMessageBuilder;

public class JmsDispatcher extends DestinationConnector {
    final private static ConnectorType CONNECTOR_TYPE = ConnectorType.SENDER;

    private JmsClient jmsClient;
    private JmsDispatcherProperties connectorProperties;
    private MessageProducer producer;
    private TemplateValueReplacer replacer = new TemplateValueReplacer();
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void onDeploy() throws DeployException {
        connectorProperties = (JmsDispatcherProperties) getConnectorProperties();
        jmsClient = new JmsClient(this, CONNECTOR_TYPE, connectorProperties);
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), CONNECTOR_TYPE, Event.INITIALIZED);
    }

    @Override
    public void onUndeploy() {}

    @Override
    public void onStart() throws StartException {
        try {
            jmsClient.start();
            producer = jmsClient.getSession().createProducer(null);
        } catch (Exception e) {
            throw new StartException("Failed to establish JMS connection", e);
        }

        monitoringController.updateStatus(getChannelId(), getMetaDataId(), CONNECTOR_TYPE, Event.CONNECTED);
    }

    @Override
    public void onStop() throws StopException {
        try {
            jmsClient.stop();
        } catch (Exception e) {
            throw new StopException("Failed to close JMS connection", e);
        }

        monitoringController.updateStatus(getChannelId(), getMetaDataId(), CONNECTOR_TYPE, Event.DISCONNECTED);
    }

    @Override
    public ConnectorProperties getReplacedConnectorProperties(ConnectorMessage message) {
        JmsDispatcherProperties jmsDispatcherProperties = (JmsDispatcherProperties) SerializationUtils.clone((JmsDispatcherProperties) getConnectorProperties());
        jmsDispatcherProperties.setTemplate(replacer.replaceValues(jmsDispatcherProperties.getTemplate(), message));
        jmsDispatcherProperties.setDestinationName(replacer.replaceValues(jmsDispatcherProperties.getDestinationName(), message));

        return jmsDispatcherProperties;
    }

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) throws InterruptedException {
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), CONNECTOR_TYPE, Event.BUSY);
        JmsDispatcherProperties jmsDispatcherProperties = (JmsDispatcherProperties) connectorProperties;
        Session session = jmsClient.getSession();

        try {
            producer.send(jmsClient.getDestination(jmsDispatcherProperties.getDestinationName()), session.createTextMessage(jmsDispatcherProperties.getTemplate()));
            return new Response(Status.SENT, null, "Message sent successfully.");
        } catch (Exception e) {
            logger.error("An error occurred in channel \"" + ChannelController.getInstance().getDeployedChannelById(getChannelId()).getName() + "\": " + e.getMessage(), ExceptionUtils.getRootCause(e));
            alertController.sendAlerts(getChannelId(), ErrorConstants.ERROR_407, e.getMessage(), e);
            return new Response(Status.QUEUED, null, ErrorMessageBuilder.buildErrorResponse("Error occurred when attempting to send JMS message.", e), ErrorMessageBuilder.buildErrorMessage(ErrorConstants.ERROR_407, e.getMessage(), e));
        } finally {
            monitoringController.updateStatus(getChannelId(), getMetaDataId(), CONNECTOR_TYPE, Event.DONE);
        }
    }
}
