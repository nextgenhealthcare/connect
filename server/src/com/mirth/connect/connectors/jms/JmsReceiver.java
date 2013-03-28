package com.mirth.connect.connectors.jms;

import java.io.IOException;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.ConnectorType;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mirth.connect.util.ErrorConstants;

public class JmsReceiver extends SourceConnector {
    final private static ConnectorType CONNECTOR_TYPE = ConnectorType.LISTENER;

    private JmsClient jmsClient;
    private JmsReceiverProperties connectorProperties;
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void onDeploy() throws DeployException {
        connectorProperties = (JmsReceiverProperties) getConnectorProperties();
        jmsClient = new JmsClient(this, CONNECTOR_TYPE, connectorProperties);
        monitoringController.updateStatus(getChannelId(), getMetaDataId(), CONNECTOR_TYPE, Event.INITIALIZED);
    }

    @Override
    public void onUndeploy() {}

    @Override
    public void onStart() throws StartException {
        try {
            jmsClient.start();
        } catch (Exception e) {
            throw new StartException("Failed to establish connection to the JMS broker", e);
        }

        TemplateValueReplacer replacer = new TemplateValueReplacer();
        String channelId = getChannelId();
        String destinationName = replacer.replaceValues(connectorProperties.getDestinationName(), channelId);

        try {
            MessageConsumer consumer;
            Destination destination = jmsClient.getDestination(destinationName);
            String selector = replacer.replaceValues(connectorProperties.getSelector(), channelId);

            if (connectorProperties.isTopic() && connectorProperties.isDurableTopic()) {
                consumer = jmsClient.getSession().createDurableSubscriber((Topic) destination, connectorProperties.getClientId(), selector, true);
            } else {
                consumer = jmsClient.getSession().createConsumer(destination, selector, true);
            }

            consumer.setMessageListener(new JmsReceiverMessageListener());
            logger.debug("Message consumer created");
        } catch (Exception e) {
            try {
                jmsClient.stop();
            } catch (Exception e1) {
                logger.error(e1);
            }

            throw new StartException("Failed to initialize JMS message consumer for destination \"" + destinationName + "\"", e);
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
    public void handleRecoveredResponse(DispatchResult dispatchResult) {
        finishDispatch(dispatchResult);
    }

    private class JmsReceiverMessageListener implements MessageListener {
        /*
         * This method is executed for every JMS message received by the MessageConsumer created in
         * onStart()
         */
        @Override
        public void onMessage(Message message) {
            RawMessage rawMessage = null;
            DispatchResult dispatchResult = null;
            boolean attemptedResponse = false;
            String responseError = null;

            monitoringController.updateStatus(getChannelId(), getMetaDataId(), CONNECTOR_TYPE, Event.BUSY);

            try {
                rawMessage = jmsMessageToRawMessage(message);
            } catch (Exception e) {
                reportError("Failed to read JMS message", e);
                return;
            }

            try {
                dispatchResult = dispatchRawMessage(rawMessage);
                attemptedResponse = true;

                try {
                    message.acknowledge();
                } catch (JMSException e) {
                    reportError("Failed to acknowledge JMS message", e);
                    responseError = "Failed to acknowledge message: " + e.getMessage();
                }
            } catch (ChannelException e) {
                reportError("Failed to process message", e);
            } finally {
                finishDispatch(dispatchResult, attemptedResponse, responseError);
                monitoringController.updateStatus(getChannelId(), getMetaDataId(), CONNECTOR_TYPE, Event.DONE);
            }
        }

        /**
         * Convert a JMS message into a RawMessage to dispatch to the source connector
         */
        private RawMessage jmsMessageToRawMessage(Message message) throws JMSException, IOException {
            if (message instanceof TextMessage) {
                return new RawMessage(((TextMessage) message).getText());
            }

            if (message instanceof BytesMessage) {
                return bytesMessageToRawMessage((BytesMessage) message);
            }

            if (message instanceof ObjectMessage) {
                // for the ObjectMessage type, simply use the object's string representation as the raw message
                return new RawMessage(((ObjectMessage) message).getObject().toString());
            }

            /*
             * Use the message object's string representation as the raw message for other message
             * formats, such as MapMessage and StreamMessage.
             */
            // TODO we could possibly transform MapMessages and/or StreamMessages into XML for the raw message
            return new RawMessage(message.toString());
        }

        private RawMessage bytesMessageToRawMessage(BytesMessage message) throws JMSException, IOException {
            BytesMessage bytesMessage = (BytesMessage) message;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final int bufferSize = 10240;
            int numBytesRead;

            do {
                byte[] buffer = new byte[bufferSize];
                numBytesRead = bytesMessage.readBytes(buffer, bufferSize);

                if (numBytesRead > 0) {
                    outputStream.write(buffer);
                }
            } while (numBytesRead > 0);

            return new RawMessage(outputStream.toByteArray());
        }
    }

    private void reportError(String errorMessage, Exception e) {
        logger.error(errorMessage + " (channel: " + ChannelController.getInstance().getDeployedChannelById(getChannelId()).getName() + ")", e);
        alertController.sendAlerts(getChannelId(), ErrorConstants.ERROR_407, null, e.getCause());
    }
}
