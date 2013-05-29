/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datapruner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.ibatis.session.SqlSession;

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
    private boolean includeContent = true;

    public ArchiverMessageList(String channelId, int pageSize, Map<String, Object> params) {
        this.channelId = channelId;
        this.params = new HashMap<String, Object>();

        // do a shallow copy of params, because we will be adding offset/limit later in getItems()
        for (Entry<String, Object> entry : params.entrySet()) {
            this.params.put(entry.getKey(), entry.getValue());
        }

        setPageSize(pageSize);
    }

    public boolean isIncludeContent() {
        return includeContent;
    }

    public void setIncludeContent(boolean includeContent) {
        this.includeContent = includeContent;
    }

    @Override
    public Long getItemCount() {
        return null;
    }

    @Override
    protected List<Message> getItems(int offset, int limit) throws Exception {
        SqlSession session = SqlConfig.getSqlSessionManager().openSession();
        params.put("offset", offset);
        params.put("limit", limit);

        List<Message> messages;
        
        try {
            messages = session.selectList("Message.prunerSelectMessagesToArchive", params);
        } finally {
            session.close();
        }

        if (includeContent) {
            DonkeyDao dao = daoFactory.getDao();
    
            try {
                for (Message message : messages) {
                    message.setChannelId(channelId);
                    message.getConnectorMessages().putAll(dao.getConnectorMessages(channelId, message.getMessageId()));
                }
            } finally {
                dao.close();
            }
        }

        return messages;
    }
}
