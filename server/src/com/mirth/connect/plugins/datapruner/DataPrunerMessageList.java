/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.ibatis.session.SqlSession;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.server.Donkey;
import com.mirth.connect.donkey.server.data.DonkeyDao;
import com.mirth.connect.donkey.server.data.DonkeyDaoFactory;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.PaginatedList;

public class DataPrunerMessageList extends PaginatedList<Message> {
    private Map<String, Object> params;
    private DonkeyDaoFactory daoFactory = Donkey.getInstance().getDaoFactory();
    private String channelId;
    private List<Long> messageIds = new ArrayList<Long>();
    private List<Long> contentMessageIds = new ArrayList<Long>();
    private long messageDateMillis;

    public DataPrunerMessageList(String channelId, int pageSize, Map<String, Object> params, Calendar messageDateThreshold) {
        this.channelId = channelId;
        this.params = new HashMap<String, Object>();

        // do a shallow copy of params, because we will be adding offset/limit later in getItems()
        for (Entry<String, Object> entry : params.entrySet()) {
            this.params.put(entry.getKey(), entry.getValue());
        }

        setPageSize(pageSize);
        messageDateMillis = messageDateThreshold.getTimeInMillis();
    }
    
    public List<Long> getMessageIds() {
        return messageIds;
    }
    
    public List<Long> getContentMessageIds() {
        return contentMessageIds;
    }

    @Override
    public Long getItemCount() {
        return null;
    }

    @Override
    protected List<Message> getItems(int offset, int limit) throws Exception {
        messageIds.clear();
        contentMessageIds.clear();
        
        List<Map<String, Object>> maps;
        SqlSession session = SqlConfig.getSqlSessionManager().openSession();
        params.put("offset", offset);
        params.put("limit", limit);

        try {
            maps = session.selectList("Message.getMessagesToPrune", params);
        } finally {
            session.close();
        }

        List<Message> messages = new ArrayList<Message>();
        DonkeyDao dao = daoFactory.getDao();

        try {
            for (Map<String, Object> map : maps) {
                Long messageId = (Long) map.get("id");
                long connectorReceivedDateMillis = ((Calendar) map.get("mm_received_date")).getTimeInMillis();

                Map<Integer, ConnectorMessage> connectorMessages = null;
                connectorMessages = dao.getConnectorMessages(channelId, messageId);

                Message message = new Message();
                message.setMessageId(messageId);
                message.setChannelId(channelId);
                message.setReceivedDate((Calendar) map.get("received_date"));
                message.setProcessed((Boolean) map.get("processed"));
                message.setServerId((String) map.get("server_id"));
                message.setImportId((Long) map.get("import_id"));
                message.getConnectorMessages().putAll(connectorMessages);

                messages.add(message);
                
                contentMessageIds.add(messageId);
                
                if (connectorReceivedDateMillis < messageDateMillis) {
                    messageIds.add(messageId);
                }
            }

            return messages;
        } finally {
            dao.close();
        }
    }
}
