/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import java.util.List;

import com.mirth.commons.encryption.Encryptor;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.server.controllers.ChannelController;
import com.mirth.connect.model.filters.MessageFilter;

public class DefaultMessageController {
    private static DefaultMessageController instance = null;

    public static DefaultMessageController create() {
        synchronized (DefaultMessageController.class) {
            if (instance == null) {
                instance = new DefaultMessageController();
            }

            return instance;
        }
    }

    private DefaultMessageController() {}

    public List<ConnectorMessage> getMessagesByPageLimit(int page, int pageSize, int maxMessages, String uid, MessageFilter filter) {
        // TODO: implement along with the new message browser
        return null;
    }

    public int removeMessages(MessageFilter filter) {
        // TODO: implement along with the new message browser
        return 0;
    }

    public void clearMessages(String channelId) {
        ChannelController.getInstance().deleteAllMessages(channelId);
    }

    public void reprocessMessages(MessageFilter filter, boolean replace, List<String> destinations) {
        // TODO: implement along with the new message browser
        
//        SqlSession session = SqlConfig.getInstance().getSqlSessionFactory().openSession();
//
//        // TODO: pass query parameters and implement queries
//        // TODO: allow for reprocessing source and all destinations, source and selected destinations, or just selected destinations and not the source
//        if (replace) {
//            session.update("MessageMapper.updateMessagesForReprocessing");
//        } else {
//            session.update("MessageMapper.copyMessagesForReprocessing");
//        }
//
//        session.commit();
//        session.close();
    }

    public void importMessage(Message message) {
        // TODO implement, imports a message that was exported, generate a new message id
    }

    public int pruneMessages(List<String> channelIds, int limit) throws MessagePrunerException {
        int rowCount;
        int totalRowCount = 0;
//        SqlSession session = sqlSessionFactory.openSession();
//
//        for (String channelId : channelIds) {
//            do {
//                rowCount = pruneMessages(session, channelId, limit);
//                totalRowCount += rowCount;
//            } while (rowCount > limit);
//        }
//
//        // TODO: delete unused attachments?
//
//        session.commit();
//        session.close();

        return totalRowCount;
    }

//    private int pruneMessages(SqlSession session, String channelId, int limit) throws MessagePrunerException {
//        int retryCount = 0;
//
//        // TODO: get max_message_age for the channel
//        //int numDays = Integer.parseInt(channels.get(0).getProperties().getProperty("max_message_age"));
//        int numDays = 100;
//
//        Calendar endDate = Calendar.getInstance();
//        endDate.set(Calendar.DATE, endDate.get(Calendar.DATE) - numDays);
//
//        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("localChannelId", com.mirth.connect.donkey.controllers.ChannelController.getInstance().getLocalChannelId(channelId));
//        params.put("endDate", endDate);
//        params.put("ignoreQueued", true);
//
//        if (limit > 0) {
//            params.put("limit", limit);
//        }
//
//        do {
//            try {
//                // TODO: implement MessageMapper.pruneMessages query
//                return session.delete("MessageMapper.pruneMessages", params);
//            } catch (Exception e) {
//                if (retryCount >= Constants.PRUNE_RETRY_COUNT) {
//                    throw new MessagePrunerException("Failed to prune messages", e);
//                } else {
//                    retryCount++;
//                }
//            }
//        } while (retryCount > 0);
//
//        // execution should never reach this point
//        throw new MessagePrunerException("Failed to prune messages", null);
//    }

    public void decryptMessage(Message message, Encryptor encryptor) {
        // TODO Auto-generated method stub
        
    }

//    private Map<String, Object> getFilterMap(MessageObjectFilter filter, String uid) {
//        Map<String, Object> parameterMap = new HashMap<String, Object>();
//
//        if (uid != null) {
//            parameterMap.put("uid", uid);
//        }
//
//        parameterMap.put("id", filter.getId());
//        parameterMap.put("correlationId", filter.getCorrelationId());
//        parameterMap.put("channelId", filter.getChannelId());
//        parameterMap.put("status", filter.getStatus());
//        parameterMap.put("type", filter.getType());
//        parameterMap.put("status", filter.getStatus());
//        parameterMap.put("connectorName", filter.getConnectorName());
//        parameterMap.put("protocol", filter.getProtocol());
//        parameterMap.put("source", filter.getSource());
//        parameterMap.put("searchCriteria", filter.getSearchCriteria());
//        parameterMap.put("searchRawData", filter.isSearchRawData());
//        parameterMap.put("searchTransformedData", filter.isSearchTransformedData());
//        parameterMap.put("searchEncodedData", filter.isSearchEncodedData());
//        parameterMap.put("searchErrors", filter.isSearchErrors());
//        parameterMap.put("quickSearch", filter.getQuickSearch());
//        parameterMap.put("ignoreQueued", filter.isIgnoreQueued());
//        parameterMap.put("channelIdList", filter.getChannelIdList());
//
//        if (filter.getStartDate() != null) {
//            parameterMap.put("startDate", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", filter.getStartDate()));
//        }
//
//        if (filter.getEndDate() != null) {
//            parameterMap.put("endDate", String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", filter.getEndDate()));
//        }
//
//        return parameterMap;
//    }
}
