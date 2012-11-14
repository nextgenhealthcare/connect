/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.channel;

import com.mirth.connect.donkey.model.channel.ChannelState;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;

/**
 * The base class for all source connectors.
 */
public abstract class SourceConnector extends Connector implements ConnectorInterface {
    private Channel channel;
    private boolean respondAfterProcessing = true;
    private MetaDataReplacer metaDataReplacer;
    private ChannelState currentState = ChannelState.STOPPED;

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
        setChannelId(channel.getChannelId());
    }

    public boolean isRespondAfterProcessing() {
        return respondAfterProcessing;
    }

    public void setRespondAfterProcessing(boolean respondAfterProcessing) {
        this.respondAfterProcessing = respondAfterProcessing;
    }

    public MetaDataReplacer getMetaDataReplacer() {
        return metaDataReplacer;
    }

    public void setMetaDataReplacer(MetaDataReplacer metaDataReplacer) {
        this.metaDataReplacer = metaDataReplacer;
    }

    public ChannelState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(ChannelState currentState) {
        this.currentState = currentState;
    }

    @Deprecated
    public boolean isRunning() {
        return currentState != ChannelState.STOPPED;
    }

    /**
     * Start the connector
     */
    @Override
    public void start() throws StartException {
        setCurrentState(ChannelState.STARTING);

        onStart();

        setCurrentState(ChannelState.STARTED);
    }

    /**
     * Stop the connector
     */
    @Override
    public void stop() throws StopException {
        //TODO make this happen before the poll connector's stop method
        setCurrentState(ChannelState.STOPPING);

        onStop();

        setCurrentState(ChannelState.STOPPED);
    }

    /**
     * Stop the connector
     */
    @Override
    public void halt() throws StopException {
        //TODO make this happen before the poll connector's stop method
        setCurrentState(ChannelState.STOPPING);

        onStop();

        setCurrentState(ChannelState.STOPPED);
    }

    /**
     * Takes a raw message and processes it if the connector is set to wait for the destination
     * connectors to complete, otherwise it queues the message for processing
     * 
     * @param rawMessage
     *            A raw message
     * @return The MessageResponse, containing the message id and a response if one was received
     * @throws StoppedException
     * @throws ChannelErrorException
     * @throws StoppingException
     */
    public MessageResponse handleRawMessage(RawMessage rawMessage) throws ChannelException {
        if (currentState == ChannelState.STOPPED) {
            throw new ChannelException(false, true);
        }

        return channel.handleRawMessage(rawMessage);
    }

    /**
     * Store the response that was sent back to the originating system and mark
     * the message as 'processed' if the source connector waits for all
     * destinations to complete
     * 
     * @throws ChannelErrorException
     * @throws StoppingException
     */
    public void storeMessageResponse(MessageResponse messageResponse) throws ChannelException {
        channel.storeMessageResponse(messageResponse);
    }

    /**
     * Handles a response generated for a message that was recovered by the
     * channel
     * 
     * @throws ChannelErrorException
     * @throws StoppingException
     * @throws StoppedException
     */
    public void handleRecoveredResponse(MessageResponse messageResponse) throws ChannelException {
        storeMessageResponse(messageResponse);
    }

    /**
     * Takes a message that was recovered by the channel, extracts the
     * appropriate response from it and sends a MessageResponse to the source
     * connector's handleRecoveredResponse() method. This method has no
     * modifier, because it is only intended to be accessed by the Channel, not
     * connector sub-classes.
     * 
     * @throws ChannelErrorException
     * @throws StoppingException
     * @throws StoppedException
     */
    void handleRecoveredMessage(Message message) throws ChannelException {
        ResponseSelector responseSelector = channel.getResponseSelector();

        if (responseSelector.canRespond() && message != null) {
            handleRecoveredResponse(new MessageResponse(message.getMessageId(), responseSelector.getResponse(message), true, message));
        }
    }
}
