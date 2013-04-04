/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.messagepruner;

import java.sql.Timestamp;
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

public class ArchiverMessageList extends PaginatedList<Message> {
    private Map<String, Object> params;
    private DonkeyDaoFactory daoFactory = Donkey.getInstance().getDaoFactory();
    private String channelId;

    public ArchiverMessageList(String channelId, int pageSize, Map<String, Object> params) {
        this.channelId = channelId;
        this.params = new HashMap<String, Object>();

        // do a shallow copy of params, because we will be adding offset/limit later in getItems()
        for (Entry<String, Object> entry : params.entrySet()) {
            this.params.put(entry.getKey(), entry.getValue());
        }

        setPageSize(pageSize);
    }

    @Override
    public Long getItemCount() {
        return null;
    }

    @Override
    protected List<Message> getItems(int offset, int limit) throws Exception {
        List<Map<String, Object>> maps;
        SqlSession session = SqlConfig.getSqlSessionManager().openSession();
        params.put("offset", offset);
        params.put("limit", limit);

        try {
            maps = session.selectList("Message.prunerSelectMessagesToArchive", params);
        } finally {
            session.close();
        }

        DonkeyDao dao = daoFactory.getDao();

        try {
            List<Message> messages = new ArrayList<Message>();

            for (Map<String, Object> map : maps) {
                Long messageId = (Long) map.get("id");

                Calendar receivedDate = Calendar.getInstance();
                receivedDate.setTimeInMillis(((Timestamp) map.get("received_date")).getTime());

                Map<Integer, ConnectorMessage> connectorMessages = null;
                connectorMessages = dao.getConnectorMessages(channelId, messageId);

                Message message = new Message();
                message.setMessageId(messageId);
                message.setChannelId(channelId);
                message.setReceivedDate(receivedDate);
                message.setProcessed((Boolean) map.get("processed"));
                message.setServerId((String) map.get("server_id"));
                message.setImportId((Long) map.get("import_id"));
                message.getConnectorMessages().putAll(connectorMessages);

                messages.add(message);
            }

            return messages;
        } finally {
            dao.close();
        }
    }
}
