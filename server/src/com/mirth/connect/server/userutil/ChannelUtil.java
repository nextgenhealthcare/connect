/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.userutil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mirth.connect.model.Channel;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.server.channel.ErrorTaskHandler;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.userutil.Status;

/**
 * This utility class allows the user to query information from channels or to perform actions on
 * channels.
 */
public class ChannelUtil {

    private static EngineController engineController = ControllerFactory.getFactory().createEngineController();
    private static ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private static ExecutorService executor = Executors.newCachedThreadPool();
    private static com.mirth.connect.donkey.server.controllers.ChannelController donkeyController = com.mirth.connect.donkey.server.controllers.ChannelController.getInstance();

    private ChannelUtil() {}

    /**
     * Get the name for a specified channel.
     * 
     * @param channelId
     *            The channel id of the deployed channel.
     * @return The channel name of the specified channel.
     */
    public static String getDeployedChannelName(String channelId) {
        String channelName = null;

        Channel channel = channelController.getDeployedChannelById(channelId);
        if (channel != null) {
            channelName = channel.getName();
        }

        return channelName;
    }

    /**
     * Start a deployed channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @return A {@link Future} object representing the result of the asynchronous operation. You
     *         can call {@link Future#get() get()} or {@link Future#get(long) get(timeoutInMillis)}
     *         to wait for the operation to finish.
     * @throws Exception
     */
    public static Future<Void> startChannel(final String channelIdOrName) throws Exception {
        return new Future<Void>(executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ErrorTaskHandler handler = new ErrorTaskHandler();
                engineController.startChannels(Collections.singleton(convertId(channelIdOrName)), handler);
                if (handler.isErrored()) {
                    throw handler.getError();
                }
                return null;
            }
        }));
    }

    /**
     * Stop a deployed channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @return A {@link Future} object representing the result of the asynchronous operation. You
     *         can call {@link Future#get() get()} or {@link Future#get(long) get(timeoutInMillis)}
     *         to wait for the operation to finish.
     * @throws Exception
     */
    public static Future<Void> stopChannel(final String channelIdOrName) throws Exception {
        return new Future<Void>(executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ErrorTaskHandler handler = new ErrorTaskHandler();
                engineController.stopChannels(Collections.singleton(convertId(channelIdOrName)), handler);
                if (handler.isErrored()) {
                    throw handler.getError();
                }
                return null;
            }
        }));
    }

    /**
     * Pause a deployed channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @return A {@link Future} object representing the result of the asynchronous operation. You
     *         can call {@link Future#get() get()} or {@link Future#get(long) get(timeoutInMillis)}
     *         to wait for the operation to finish.
     * @throws Exception
     */
    public static Future<Void> pauseChannel(final String channelIdOrName) throws Exception {
        return new Future<Void>(executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ErrorTaskHandler handler = new ErrorTaskHandler();
                engineController.pauseChannels(Collections.singleton(convertId(channelIdOrName)), handler);
                if (handler.isErrored()) {
                    throw handler.getError();
                }
                return null;
            }
        }));
    }

    /**
     * Resume a deployed channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @throws Exception
     */
    public static Future<Void> resumeChannel(final String channelIdOrName) throws Exception {
        return new Future<Void>(executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ErrorTaskHandler handler = new ErrorTaskHandler();
                engineController.resumeChannels(Collections.singleton(convertId(channelIdOrName)), handler);
                if (handler.isErrored()) {
                    throw handler.getError();
                }
                return null;
            }
        }));
    }

    /**
     * Halt a deployed channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @return A {@link Future} object representing the result of the asynchronous operation. You
     *         can call {@link Future#get() get()} or {@link Future#get(long) get(timeoutInMillis)}
     *         to wait for the operation to finish.
     * @throws Exception
     */
    public static Future<Void> haltChannel(final String channelIdOrName) throws Exception {
        return new Future<Void>(executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ErrorTaskHandler handler = new ErrorTaskHandler();
                engineController.haltChannels(Collections.singleton(convertId(channelIdOrName)), handler);
                if (handler.isErrored()) {
                    throw handler.getError();
                }
                return null;
            }
        }));
    }

    /**
     * Get the current state of a channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the channel.
     * @return The current DeployedState.
     */
    public static DeployedState getChannelState(String channelIdOrName) {
        DashboardStatus dashboardStatus = getDashboardStatus(channelIdOrName, null);
        return dashboardStatus != null ? DeployedState.fromDonkeyDeployedState(dashboardStatus.getState()) : null;
    }

    /**
     * Deploy a channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the channel.
     * @return A {@link Future} object representing the result of the asynchronous operation. You
     *         can call {@link Future#get() get()} or {@link Future#get(long) get(timeoutInMillis)}
     *         to wait for the operation to finish.
     */
    public static Future<Void> deployChannel(final String channelIdOrName) {
        return new Future<Void>(executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ErrorTaskHandler handler = new ErrorTaskHandler();
                engineController.deployChannels(Collections.singleton(convertId(channelIdOrName)), null, handler);
                if (handler.isErrored()) {
                    throw handler.getError();
                }
                return null;
            }
        }));
    }

    /**
     * Undeploy a channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @return A {@link Future} object representing the result of the asynchronous operation. You
     *         can call {@link Future#get() get()} or {@link Future#get(long) get(timeoutInMillis)}
     *         to wait for the operation to finish.
     */
    public static Future<Void> undeployChannel(final String channelIdOrName) {
        return new Future<Void>(executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ErrorTaskHandler handler = new ErrorTaskHandler();
                engineController.undeployChannels(Collections.singleton(convertId(channelIdOrName)), null, handler);
                if (handler.isErrored()) {
                    throw handler.getError();
                }
                return null;
            }
        }));
    }

    /**
     * Check if a channel is currently deployed.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the channel.
     * @return True if the channel is deployed, false if it is not.
     */
    public static boolean isChannelDeployed(String channelIdOrName) {
        return engineController.getDeployedIds().contains(convertId(channelIdOrName));
    }

    /**
     * Start a connector on a given channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the channel.
     * @param metaDataId
     *            The metadata id of the connector. Note that the source connector has a metadata id
     *            of 0.
     * @return A {@link Future} object representing the result of the asynchronous operation. You
     *         can call {@link Future#get() get()} or {@link Future#get(long) get(timeoutInMillis)}
     *         to wait for the operation to finish.
     * @throws Exception
     */
    public static Future<Void> startConnector(final String channelIdOrName, final Integer metaDataId) throws Exception {
        return new Future<Void>(executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ErrorTaskHandler handler = new ErrorTaskHandler();
                engineController.startConnector(Collections.singletonMap(convertId(channelIdOrName), Collections.singletonList(metaDataId)), handler);
                if (handler.isErrored()) {
                    throw handler.getError();
                }
                return null;
            }
        }));
    }

    /**
     * Stop a connector on a given channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the channel.
     * @param metaDataId
     *            The metadata id of the connector. Note that the source connector has a metadata id
     *            of 0.
     * @return A {@link Future} object representing the result of the asynchronous operation. You
     *         can call {@link Future#get() get()} or {@link Future#get(long) get(timeoutInMillis)}
     *         to wait for the operation to finish.
     * @throws Exception
     */
    public static Future<Void> stopConnector(final String channelIdOrName, final Integer metaDataId) throws Exception {
        return new Future<Void>(executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ErrorTaskHandler handler = new ErrorTaskHandler();
                engineController.stopConnector(Collections.singletonMap(convertId(channelIdOrName), Collections.singletonList(metaDataId)), handler);
                if (handler.isErrored()) {
                    throw handler.getError();
                }
                return null;
            }
        }));
    }

    /**
     * Get the current state of a connector.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the channel.
     * @param metaDataId
     *            The metadata id of the connector. Note that the source connector has a metadata id
     *            of 0.
     * @return The current connector state returned as the DeployedState enumerator.
     */
    public static DeployedState getConnectorState(String channelIdOrName, Number metaDataId) {
        DashboardStatus dashboardStatus = getDashboardStatus(channelIdOrName, metaDataId);
        return dashboardStatus != null ? DeployedState.fromDonkeyDeployedState(dashboardStatus.getState()) : null;
    }

    /**
     * Get the received count statistic for a specific channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @return The received count statistic as a Long for the specified channel.
     */
    public static Long getReceivedCount(String channelIdOrName) {
        return getReceivedCount(channelIdOrName, null);
    }

    /**
     * Get the received count statistic for a specific connector.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @param metaDataId
     *            The metadata id of the connector. Note that the source connector has a metadata id
     *            of 0.
     * @return The received count statistic as a Long for the specified connector.
     */
    public static Long getReceivedCount(String channelIdOrName, Number metaDataId) {
        return getStatisticByStatus(channelIdOrName, metaDataId, Status.RECEIVED);
    }

    /**
     * Get the filtered count statistic for a specific channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @return The filtered count statistic as a Long for the specified channel.
     */
    public static Long getFilteredCount(String channelIdOrName) {
        return getFilteredCount(channelIdOrName, null);
    }

    /**
     * Get the filtered count statistic for a specific connector.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @param metaDataId
     *            The metadata id of the connector. Note that the source connector has a metadata id
     *            of 0.
     * @return The filtered count statistic as a Long for the specified connector.
     */
    public static Long getFilteredCount(String channelIdOrName, Number metaDataId) {
        return getStatisticByStatus(channelIdOrName, metaDataId, Status.FILTERED);
    }

    /**
     * Get the queued count statistic for a specific channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @return The queued count statistic as a Long for the specified channel.
     */
    public static Long getQueuedCount(String channelIdOrName) {
        return getQueuedCount(channelIdOrName, null);
    }

    /**
     * Get the queued count statistic for a specific connector.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @param metaDataId
     *            The metadata id of the connector. Note that the source connector has a metadata id
     *            of 0.
     * @return The queued count statistic as a Long for the specified connector.
     */
    public static Long getQueuedCount(String channelIdOrName, Number metaDataId) {
        return getStatisticByStatus(channelIdOrName, metaDataId, Status.QUEUED);
    }

    /**
     * Get the sent count statistic for a specific channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @return The sent count statistic as a Long for the specified channel.
     */
    public static Long getSentCount(String channelIdOrName) {
        return getSentCount(channelIdOrName, null);
    }

    /**
     * Get the sent count statistic for a specific connector.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @param metaDataId
     *            The metadata id of the connector. Note that the source connector has a metadata id
     *            of 0.
     * @return The sent count statistic as a Long for the specified connector.
     */
    public static Long getSentCount(String channelIdOrName, Number metaDataId) {
        return getStatisticByStatus(channelIdOrName, metaDataId, Status.SENT);
    }

    /**
     * Get the error count statistic for a specific channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @return The error count statistic as a Long for the specified channel.
     */
    public static Long getErrorCount(String channelIdOrName) {
        return getErrorCount(channelIdOrName, null);
    }

    /**
     * Get the error count statistic for a specific connector.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @param metaDataId
     *            The metadata id of the connector. Note that the source connector has a metadata id
     *            of 0.
     * @return The error count statistic as a Long for the specified connector.
     */
    public static Long getErrorCount(String channelIdOrName, Number metaDataId) {
        return getStatisticByStatus(channelIdOrName, metaDataId, Status.ERROR);
    }

    private static String convertId(String channelIdOrName) {
        if (!channelController.getChannelIds().contains(channelIdOrName)) {
            // Assume the name was passed in instead, check the deployed cache first
            Channel channel = channelController.getDeployedChannelByName(channelIdOrName);
            if (channel != null) {
                return channel.getId();
            }

            // Check the regular cache second
            channel = channelController.getChannelByName(channelIdOrName);
            if (channel != null) {
                return channel.getId();
            }
        }

        return channelIdOrName;
    }

    private static DashboardStatus getDashboardStatus(String channelIdOrName, Number metaDataId) {
        DashboardStatus dashboardStatus = engineController.getChannelStatus(convertId(channelIdOrName));

        if (dashboardStatus != null) {
            if (metaDataId == null) {
                return dashboardStatus;
            } else {
                metaDataId = metaDataId.intValue();

                for (DashboardStatus childStatus : dashboardStatus.getChildStatuses()) {
                    if (childStatus.getMetaDataId().equals(metaDataId)) {
                        return childStatus;
                    }
                }
            }
        }

        return null;
    }

    private static Long getStatisticByStatus(String channelIdOrName, Number metaDataId, Status status) {
        DashboardStatus dashboardStatus = getDashboardStatus(channelIdOrName, metaDataId);
        if (dashboardStatus != null) {
            if (status == Status.QUEUED) {
                return dashboardStatus.getQueued();
            } else {
                return dashboardStatus.getStatistics().get(convertStatus(status));
            }
        }
        return null;
    }

    private static com.mirth.connect.donkey.model.message.Status convertStatus(Status status) {
        switch (status) {
            case RECEIVED:
                return com.mirth.connect.donkey.model.message.Status.RECEIVED;
            case FILTERED:
                return com.mirth.connect.donkey.model.message.Status.FILTERED;
            case TRANSFORMED:
                return com.mirth.connect.donkey.model.message.Status.TRANSFORMED;
            case SENT:
                return com.mirth.connect.donkey.model.message.Status.SENT;
            case QUEUED:
                return com.mirth.connect.donkey.model.message.Status.QUEUED;
            case ERROR:
                return com.mirth.connect.donkey.model.message.Status.ERROR;
            case PENDING:
                return com.mirth.connect.donkey.model.message.Status.PENDING;
            default:
                return null;
        }
    }

    /**
     * Reset all statistics for a specific channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @return A {@link Future} object representing the result of the asynchronous operation. You
     *         can call {@link Future#get() get()} or {@link Future#get(long) get(timeoutInMillis)}
     *         to wait for the operation to finish.
     * @throws Exception
     */
    public static Future<Void> resetStatistics(final String channelIdOrName) throws Exception {
        return new Future<Void>(executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                clearStatistics(channelIdOrName, null, null);
                return null;
            }
        }));
    }

    /**
     * Reset all statistics for the specified connector on the given channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @param metaDataId
     *            The metadata id of the deployed connector. Note that the source connector has a
     *            metadata id of 0 and the aggregate of null.
     * @return A {@link Future} object representing the result of the asynchronous operation. You
     *         can call {@link Future#get() get()} or {@link Future#get(long) get(timeoutInMillis)}
     *         to wait for the operation to finish.
     * @throws Exception
     */
    public static Future<Void> resetStatistics(final String channelIdOrName, final Integer metaDataId) throws Exception {
        return new Future<Void>(executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                clearStatistics(channelIdOrName, metaDataId, null);
                return null;
            }
        }));
    }

    /**
     * Reset the specified statistics for the specified connector on the given channel.
     * 
     * @param channelIdOrName
     *            The channel id or current name of the deployed channel.
     * @param metaDataId
     *            The metadata id of the deployed connector. Note that the source connector has a
     *            metadata id of 0 and the aggregate of null.
     * @param statuses
     *            A collection of statuses to reset.
     * @return A {@link Future} object representing the result of the asynchronous operation. You
     *         can call {@link Future#get() get()} or {@link Future#get(long) get(timeoutInMillis)}
     *         to wait for the operation to finish.
     * @throws Exception
     */
    public static Future<Void> resetStatistics(final String channelIdOrName, final Integer metaDataId, final Collection<Status> statuses) throws Exception {
        return new Future<Void>(executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                clearStatistics(channelIdOrName, metaDataId, statuses);
                return null;
            }
        }));
    }

    private static void clearStatistics(String channelIdOrName, Integer metaDataId, Collection<Status> statuses) {
        Map<String, List<Integer>> channelMap = new HashMap<String, List<Integer>>();
        Set<com.mirth.connect.donkey.model.message.Status> statusesToReset = new HashSet<com.mirth.connect.donkey.model.message.Status>();
        Set<com.mirth.connect.donkey.model.message.Status> resetableStatuses = new HashSet<com.mirth.connect.donkey.model.message.Status>();

        resetableStatuses.add(com.mirth.connect.donkey.model.message.Status.RECEIVED);
        resetableStatuses.add(com.mirth.connect.donkey.model.message.Status.FILTERED);
        resetableStatuses.add(com.mirth.connect.donkey.model.message.Status.ERROR);
        resetableStatuses.add(com.mirth.connect.donkey.model.message.Status.SENT);

        com.mirth.connect.donkey.server.channel.Channel deployedChannel = engineController.getDeployedChannel(convertId(channelIdOrName));
        if (deployedChannel != null) {
            List<Integer> connectorList = deployedChannel.getMetaDataIds();

            if (metaDataId == null) {
                connectorList.add(null);
            } else {
                Set<Integer> metaDataIds = new HashSet<Integer>(connectorList);
                connectorList.clear();
                if (metaDataIds.contains(metaDataId)) {
                    connectorList.add(metaDataId);
                }
            }

            if (!connectorList.isEmpty()) {
                channelMap.put(convertId(channelIdOrName), connectorList);

                if (statuses == null) {
                    statusesToReset.addAll(resetableStatuses);
                } else {
                    for (Status status : statuses) {
                        com.mirth.connect.donkey.model.message.Status convertedStatus = convertStatus(status);
                        if (resetableStatuses.contains(convertedStatus)) {
                            statusesToReset.add(convertedStatus);
                        }
                    }
                }

                donkeyController.resetStatistics(channelMap, statusesToReset);
            }
        }
    }
}