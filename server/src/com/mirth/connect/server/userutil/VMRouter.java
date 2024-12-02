/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.userutil.Response;
import com.mirth.connect.userutil.Status;
import com.mirth.connect.util.ErrorMessageBuilder;

/**
 * Utility class used to dispatch messages to channels.
 */
public class VMRouter {
    private Logger logger = LogManager.getLogger(getClass());
    private ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private EngineController engineController = ControllerFactory.getFactory().createEngineController();

    private TrackingEnhancer trackingEnhancer;

    /**
     * Instantiates a VMRouter object.
     */
    public VMRouter() {}

    /**
     * Instantiates a VMRouter object with additional message tracking enhancements.
     * 
     * @param channelId channel ID or "NONE" if null
     * @param messageId message ID or -1L if null
     * @param sourceMap the message's source map
    */
    public VMRouter(String channelId, Long messageId, SourceMap sourceMap) {
        this.trackingEnhancer = new TrackingEnhancer(channelId, messageId, sourceMap);
    }

    /**
     * Dispatches a message to a channel, specified by the deployed channel name. If the dispatch
     * fails for any reason (for example, if the target channel is not started), a {@link Response} object
     * with the {@link Status#ERROR} status and the error message will be returned.
     * 
     * @param channelName The name of the deployed channel to dispatch the message to.
     * @param message The message to dispatch to the channel.
     * @return The {@link Response} object returned by the channel, if its source connector is configured to
     *         return one.
     */
    public Response routeMessage(String channelName, String message) {
        return routeMessage(channelName, createRawMessage(message, null, null));
    }

    /**
     * Dispatches a message to a channel, specified by the deployed channel name. If the dispatch
     * fails for any reason (for example, if the target channel is not started), a {@link Response} object
     * with the {@link Status#ERROR} status and the error message will be returned.
     * 
     * @param channelName The name of the deployed channel to dispatch the message to.
     * @param rawMessage A {@link RawMessage} object to dispatch to the channel.
     * @return The {@link Response} object returned by the channel, if its source connector is configured to
     *         return one.
     */
    public Response routeMessage(String channelName, RawMessage rawMessage) {
        com.mirth.connect.model.Channel channel = channelController.getDeployedChannelByName(channelName);

        if (channel == null) {
            String message = "Could not find channel to route to for channel name: " + channelName;
            logger.error(message);
            return new Response(Status.ERROR, message);
        }

        return routeMessageByChannelId(channel.getId(), rawMessage);
    }

    /**
        Route a message to the specified channelName. Information about the chain of source channel Ids and
        source message Ids will be included in the sourceMap of the downstream message automatically in a
        similar manner as if a Channel Writer was being used.

        @param channelName The name of the channel to which to route the message.
        @param message The content of the message to be sent, textual or binary. As String or byte[].
        @param sourceMap A map containing entries to include in the sourceMap of the sent message.
        @return The {@link Response} object returned by the channel.

        @see #routeMessage(String, Object, Map, Collection)
    */
    public Response routeMessage(String channelName, Object message, Map<String, Object> sourceMap) {
        return routeMessage(channelName, message, sourceMap, null);
    }

    /**
        Route a message to the specified channelName. Information about the chain of source channel Ids and
        source message Ids will be included in the sourceMap of the downstream message automatically in a
        similar manner as if a Channel Writer was being used.

        @param channelName The name of the channel to which to route the message.
        @param message The content of the message to be sent, textual or binary. As String or byte[].
        @param sourceMap A map containing entries to include in the sourceMap of the sent message.
        @param destinationSet A collection of integers (metadata IDs) representing which destinations to dispatch the message to.
            Null may be passed to indicate all destinations. If unspecified, all destinations is the default.
        @return The {@link Response} object returned by the channel.

        @see VMRouter#routeMessage(String, RawMessage)
    */
    public Response routeMessage(String channelName, Object message, Map<String, Object> sourceMap, Collection<Number> destinationSet) {
        return routeMessage(channelName, createRawMessage(message, sourceMap, destinationSet));
    }

    /**
     * Dispatches a message to a channel, specified by the deployed channel ID. If the dispatch
     * fails for any reason (for example, if the target channel is not started), a {@link Response} object
     * with the {@link Status#ERROR} status and the error message will be returned.
     * 
     * @param channelId The ID of the deployed channel to dispatch the message to.
     * @param message The message to dispatch to the channel.
     * @return The {@link Response} object returned by the channel, if its source connector is configured to
     * return one.
     */
    public Response routeMessageByChannelId(String channelId, String message) {
        return routeMessageByChannelId(channelId, createRawMessage(message, null, null));
    }

    /**
     * Dispatches a message to a channel, specified by the deployed channel ID. If the dispatch
     * fails for any reason (for example, if the target channel is not started), a {@link Response} object
     * with the {@link Status#ERROR} status and the error message will be returned.
     * 
     * @param channelId The ID of the deployed channel to dispatch the message to.
     * @param rawMessage A {@link RawMessage} object to dispatch to the channel.
     * @return The {@link Response} object returned by the channel, if its source connector is configured to
     * return one.
     */
    public Response routeMessageByChannelId(String channelId, RawMessage rawMessage) {
        try {
            DispatchResult dispatchResult = engineController.dispatchRawMessage(channelId, convertRawMessage(rawMessage), false, true);

            Response response = null;
            if (dispatchResult != null && dispatchResult.getSelectedResponse() != null) {
                response = new Response(dispatchResult.getSelectedResponse());
            }

            return response;
        } catch (Throwable e) {
            String message = "Error routing message to channel id: " + channelId;
            logger.error(message, e);
            String responseStatusMessage = ErrorMessageBuilder.buildErrorResponse(message, e);
            String responseError = ErrorMessageBuilder.buildErrorMessage(this.getClass().getSimpleName(), message, e);
            return new Response(Status.ERROR, null, responseStatusMessage, responseError);
        }
    }

    /**
        Route a message to the specified channelId. Information about the chain of source channel Ids and
        source message Ids will be included in the sourceMap of the downstream message automatically in a
        similar manner as if a Channel Writer was being used.

        @param channelId The unique identifier of the channel to which to route the message.
        @param message The content of the message to be sent, textual or binary. As String or byte[].
        @return The {@link Response} object returned by the channel.

        @see #routeMessageByChannelId(String, Object, Map, Collection)
    */
    public Response routeMessageByChannelId(String channelId, Object message) {
        return routeMessageByChannelId(channelId, message, null, null);
    }

    /**
        Route a message to the specified channelId. Information about the chain of source channel Ids and
        source message Ids will be included in the sourceMap of the downstream message automatically in a
        similar manner as if a Channel Writer was being used.

        @param channelId The unique identifier of the channel to which to route the message.
        @param message The content of the message to be sent, textual or binary. As String or byte[].
        @param sourceMap A map containing entries to include in the sourceMap of the sent message.
        @return The {@link Response} object returned by the channel.

        @see #routeMessageByChannelId(String, Object, Map, Collection)
    */
    public Response routeMessageByChannelId(String channelId, Object message, Map<String, Object> sourceMap) {
        return routeMessageByChannelId(channelId, message, sourceMap, null);
    }

    /**
        Route a message to the specified channelId. Information about the chain of source channel Ids and
        source message Ids will be included in the sourceMap of the downstream message automatically in a
        similar manner as if a Channel Writer was being used.

        @param channelId The unique identifier of the channel to which to route the message.
        @param message The content of the message to be sent, textual or binary. As String or byte[].
        @param sourceMap A map containing entries to include in the sourceMap of the sent message.
        @param destinationSet A collection of integers (metadata IDs) representing which destinations to dispatch the message to.
            Null may be passed to indicate all destinations. If unspecified, all destinations is the default.
        @return The {@link Response} object returned by the channel.

        @see {@link VMRouter#routeMessageByChannelId(String, RawMessage)}
    */
    public Response routeMessageByChannelId(String channelId, Object message, Map<String, Object> sourceMap, Collection<Number> destinationSet) {
        return routeMessageByChannelId(channelId, createRawMessage(message, sourceMap, destinationSet));
    }

    private com.mirth.connect.donkey.model.message.RawMessage convertRawMessage(RawMessage message) {
        if (message.isBinary()) {
            return new com.mirth.connect.donkey.model.message.RawMessage(message.getRawBytes(), message.getDestinationMetaDataIds(), message.getSourceMap());
        } else {
            return new com.mirth.connect.donkey.model.message.RawMessage(message.getRawData(), message.getDestinationMetaDataIds(), message.getSourceMap());
        }
    }

    /**
        Create a {@link RawMessage} with the specified content, sourceMap, and destinationSet.

        @param message The content of the message to be sent, textual or binary. As String or byte[].
        @param sourceMap A map containing entries to include in the sourceMap of the {@link RawMessage} (optional).
        @param destinationSet A collection of integers (metadata IDs) representing which destinations to dispatch the message to.
            Null may be passed to indicate all destinations. If unspecified, all destinations is the default (optional).
        @return A {@link RawMessage} object containing the message, source, and destination information.
    */
    public RawMessage createRawMessage(Object message, Map<String, Object> sourceMap, Collection<Number> destinationSet) {
        if(trackingEnhancer != null) {
            sourceMap = trackingEnhancer.enrich(sourceMap);
        }

        if(message instanceof byte[]) {
            return new RawMessage((byte[])message, destinationSet, sourceMap);
        } else {
            return new RawMessage(message.toString(), destinationSet, sourceMap);
        }
    }

    /**
     * Adds additional message tracking data.
     * 
     * TrackingEnhancer
     */
    private class TrackingEnhancer {
        private String channelId;
        private Long messageId;
        private SourceMap envSourceMap;
        
        /**
         * Constructor
         * 
         * @param channelId channel ID; null defaults to "NONE"
         * @param messageId message ID; null defaults to -1L
         * @param sourceMap the message's source map
         */
        private TrackingEnhancer(String channelId, Long messageId, SourceMap sourceMap) {
            this.channelId = channelId != null ? channelId : "NONE";
            this.messageId = messageId != null ? messageId : -1L;
            this.envSourceMap = sourceMap;
        }

        /**
         * Enrich the given source map with additional message tracking data.
         * 
         * @param messageSourceMap
         * @return a new Map
         */
        private Map<String, Object> enrich(Map<String, Object> messageSourceMap) {
            if (messageSourceMap == null) {
                messageSourceMap = Collections.emptyMap();
            }

            List<String> sourceChannelIds = lookupAsList("sourceChannelIds", "sourceChannelId");
            List<String> sourceMessageIds = lookupAsList("sourceMessageIds", "sourceMessageId");

            HashMap<String,Object> newSourceMap = new HashMap<String,Object>(messageSourceMap);
            String channelId = this.channelId;
            Long messageId = this.messageId;

            sourceChannelIds.add(channelId);
            sourceMessageIds.add(messageId.toString());

            newSourceMap.put("sourceChannelIds", sourceChannelIds);
            newSourceMap.put("sourceChannelId", channelId);
            newSourceMap.put("sourceMessageIds", sourceMessageIds);
            newSourceMap.put("sourceMessageId", messageId);

            return newSourceMap;
        }

        /**
         * Given the specified lookup keys, return the first non-null value as a List.
         * The expectation is the first lookup will return a List, while the second returns an Object.
         * 
         * @param primary primary lookup key to return a List
         * @param secondary secondary lookup key to return an Object
         * @return a List containing the first non-null lookup value, else an empty List
         */
        private List<String> lookupAsList(String primary, String secondary) {
            List<String> result = new ArrayList<String>();

            Object primaryValue = lookupInEnvSourceMap(primary);

            if(primaryValue != null) {
                //all of this to not assume the result is a List<String>
                if(primaryValue instanceof Collection) {
                    ((Collection<?>)primaryValue).stream()
                    .map(i -> i.toString())
                    .forEach(result::add);
                } else if(primaryValue instanceof Object[]) {
                    Arrays.stream((Object[])primaryValue)
                    .map(i -> i.toString())
                    .forEach(result::add);
                }
            } else {
                Object secondaryValue = lookupInEnvSourceMap(secondary);
                if(secondaryValue != null) {
                    result.add(secondaryValue.toString());
                }
            }

            return result;
        }

        /**
         * Look up a value from the environment's {@link SourceMap}
         * @param key
         * @return its mapped value, can be null
         */
        private Object lookupInEnvSourceMap(String key) {
            return this.envSourceMap.get(key);
        }
    }
}
