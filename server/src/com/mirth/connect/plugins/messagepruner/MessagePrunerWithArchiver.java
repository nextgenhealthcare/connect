/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagepruner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.log4j.Logger;

import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.MessageUtils;
import com.mirth.connect.util.MessageUtils.MessageExportException;
import com.mirth.connect.util.messagewriter.MessageWriter;

public class MessagePrunerWithArchiver extends MessagePruner {
    /**
     * The maximum allowed list size for executing DELETE ... WHERE IN ([list]).
     * Oracle does not allow the list size to exceed 1000.
     */
    private final static int LIST_LIMIT = 1000;
    
    public enum Strategy {
        INCLUDE_LIST, EXCLUDE_LIST, INCLUDE_RANGES, EXCLUDE_RANGES;
    }

    private MessageWriter archiver;
    private int archiverPageSize;
    private Strategy strategy;
    private Logger logger = Logger.getLogger(this.getClass());

    public MessagePrunerWithArchiver(MessageWriter archiver) {
        this(archiver, 1000, null);
    }

    public MessagePrunerWithArchiver(MessageWriter archiver, int archiverPageSize) {
        this(archiver, archiverPageSize, null);
    }

    public MessagePrunerWithArchiver(MessageWriter archiver, int archiverPageSize, Strategy strategy) {
        this.archiver = archiver;
        this.archiverPageSize = archiverPageSize;
        this.strategy = strategy;
    }

    public MessageWriter getArchiver() {
        return archiver;
    }

    public void setArchiver(MessageWriter archiver) {
        this.archiver = archiver;
    }

    public int getArchiverPageSize() {
        return archiverPageSize;
    }

    public void setArchiverPageSize(int archiverPageSize) {
        this.archiverPageSize = archiverPageSize;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    @Override
    protected int[] prune(String channelId, Calendar messageDateThreshold, Calendar contentDateThreshold) throws InterruptedException, MessagePrunerException {
        List<Long> archivedMessageIds = archive(channelId, messageDateThreshold, contentDateThreshold);
        return pruneArchivedMessages(channelId, archivedMessageIds, messageDateThreshold, (contentDateThreshold != null));
    }

    private List<Long> archive(String channelId, Calendar messageDateThreshold, Calendar contentDateThreshold) throws InterruptedException, MessagePrunerException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));
        params.put("skipIncomplete", isSkipIncomplete());
        params.put("dateThreshold", (contentDateThreshold == null) ? messageDateThreshold : contentDateThreshold);

        if (getSkipStatuses().length > 0) {
            params.put("skipStatuses", getSkipStatuses());
        }

        try {
            logger.debug("Running archiver for channel: " + channelId);
            return MessageUtils.exportMessages(new ArchiverMessageList(channelId, archiverPageSize, params), archiver).getProcessedIds();
        } catch (MessageExportException e) {
            throw new MessagePrunerException("An error occurred when attempting to archive messages", e);
        }
    }

    private int[] pruneArchivedMessages(String channelId, List<Long> archivedMessageIds, Calendar messageDateThreshold, boolean pruneContent) throws MessagePrunerException {
        if (archivedMessageIds.size() == 0) {
            logger.debug("Not pruning since no messages were archived");
            return new int[] { 0, 0 };
        }

        Integer limit = getBlockSize();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("localChannelId", ChannelController.getInstance().getLocalChannelId(channelId));

        if (limit != null) {
            params.put("limit", limit);
        }

        List<Long> unarchivedMessageIds = getInvertedList(archivedMessageIds);
        List<long[]> includeRanges = null;
        List<long[]> excludeRanges = null;
        Strategy strategy = this.strategy;

        /*
         * If a query strategy hasn't been defined, automatically choose one. If the # of archived
         * messages is less than the # unarchived, then we want to prune using DELETE ... WHERE IN
         * ([archived message ids]). Otherwise, we want to delete by excluding the unarchived
         * messages: DELETE ... WHERE NOT IN ([unarchived message ids]) AND id BETWEEN min AND max.
         */
        switch (strategy) {
            case INCLUDE_RANGES:
                includeRanges = getRanges(archivedMessageIds);
                break;

            case EXCLUDE_RANGES:
                excludeRanges = getRanges(unarchivedMessageIds);
                break;

            default:
                if (archivedMessageIds.size() > LIST_LIMIT && unarchivedMessageIds.size() > LIST_LIMIT) {
                    includeRanges = getRanges(archivedMessageIds);
                    excludeRanges = getRanges(unarchivedMessageIds);

                    strategy = (includeRanges.size() < excludeRanges.size()) ? Strategy.INCLUDE_RANGES : Strategy.EXCLUDE_RANGES;
                } else {
                    strategy = (archivedMessageIds.size() < unarchivedMessageIds.size()) ? Strategy.INCLUDE_LIST : Strategy.EXCLUDE_LIST;
                }
                break;
        }

        switch (strategy) {
            case INCLUDE_LIST:
                params.put("includeMessageList", StringUtils.join(archivedMessageIds, ','));
                break;

            case EXCLUDE_LIST:
                params.put("minMessageId", archivedMessageIds.get(0));
                params.put("maxMessageId", archivedMessageIds.get(archivedMessageIds.size() - 1));

                if (unarchivedMessageIds.size() > 0) {
                    params.put("excludeMessageList", StringUtils.join(unarchivedMessageIds, ','));
                }
                break;

            case INCLUDE_RANGES:
                params.put("includeMessageRanges", includeRanges);
                break;

            case EXCLUDE_RANGES:
                List<long[]> ranges = excludeRanges;

                params.put("minMessageId", archivedMessageIds.get(0));
                params.put("maxMessageId", archivedMessageIds.get(archivedMessageIds.size() - 1));

                if (!ranges.isEmpty()) {
                    params.put("excludeMessageRanges", ranges);
                }
                break;
        }

        logger.debug("Pruning " + archivedMessageIds.size() + " messages in channel " + channelId + " with strategy: " + strategy);
        int numContentPruned = 0;
        int numMessagesPruned = 0;
        SqlSession session = SqlConfig.getSqlSessionManager().openSession();
        long startTime = System.currentTimeMillis();

        try {
            /*
             * Delete content for all messages that were archived if a content date threshold was
             * given.
             */
            if (pruneContent) {
                numContentPruned += runDelete(session, "Message.prunerDeleteMessageContent", params, limit);
            }

            if (messageDateThreshold != null) {
                /*
                 * If content was pruned separately, then we need to add an additional check for the
                 * message date threshold. If content was not pruned separately, then we are safe to
                 * delete all of the messages that were archived.
                 */
                if (pruneContent) {
                    params.put("dateThreshold", messageDateThreshold);
                }

                /*
                 * These manual "cascade" delete queries are only needed for databases that don't
                 * support cascade deletion with the Message.prunerDeleteMessages query.
                 */
                numContentPruned += runDelete(session, "Message.prunerCascadeDeleteMessageContent", params, limit);
                runDelete(session, "Message.prunerCascadeDeleteCustomMetadata", params, limit);
                runDelete(session, "Message.prunerCascadeDeleteAttachments", params, limit);
                runDelete(session, "Message.prunerCascadeDeleteConnectorMessages", params, limit);

                numMessagesPruned += runDelete(session, "Message.prunerDeleteMessages", params, limit);
            }

            session.commit();
            logger.debug("Pruning completed in " + (System.currentTimeMillis() - startTime) + "ms");
            return new int[] { numMessagesPruned, numContentPruned };
        } finally {
            session.close();
        }
    }

    private List<long[]> getRanges(List<Long> sortedValues) {
        List<long[]> ranges = new ArrayList<long[]>();
        int size = sortedValues.size();

        if (size == 0) {
            return ranges;
        }

        long start = sortedValues.get(0);
        long end = start;

        for (int i = 1; i < size; i++) {
            long value = sortedValues.get(i);

            if (value == (end + 1)) {
                end = value;
            } else {
                ranges.add(new long[] { start, end });
                start = value;
                end = start;
            }
        }

        ranges.add(new long[] { start, end });
        return ranges;
    }

    private List<Long> getInvertedList(List<Long> sortedValues) {
        List<Long> invertedList = new ArrayList<Long>();
        int numValues = sortedValues.size();

        if (numValues == 0) {
            return invertedList;
        }

        long lastValue = sortedValues.get(0);
        long currentValue;

        for (int i = 1; i < numValues; i++) {
            currentValue = sortedValues.get(i);

            /*
             * See if there is a gap at the current position in the list, if there is, then add all
             * of the gap values in the inverted list
             */
            if (currentValue > (lastValue + 1)) {
                for (long j = lastValue + 1; j < currentValue; j++) {
                    invertedList.add(j);
                }
            }

            lastValue = currentValue;
        }

        return invertedList;
    }
}
