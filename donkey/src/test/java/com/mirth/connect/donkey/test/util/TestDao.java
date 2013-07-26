/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.test.util;

import java.sql.Connection;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.data.DonkeyDaoException;
import com.mirth.connect.donkey.server.data.jdbc.JdbcDao;
import com.mirth.connect.donkey.server.data.jdbc.PreparedStatementSource;
import com.mirth.connect.donkey.server.data.jdbc.QuerySource;
import com.mirth.connect.donkey.util.Serializer;

public class TestDao extends JdbcDao {
    private int errorPct = 0;
    private int hangPct = 0;
    private int hangMillis = 0;

    public TestDao(Connection connection, QuerySource querySource, PreparedStatementSource statementSource, Serializer serializer, String statsServerId, int errorPct, int hangPct, int hangMillis) {
        super(connection, querySource, statementSource, serializer, false, true, statsServerId);
        this.errorPct = errorPct;
        this.hangPct = hangPct;
        this.hangMillis = hangMillis;
    }

    @Override
    public void updateStatus(ConnectorMessage connectorMessage, Status previousStatus) {
        throwRandomError();
        super.updateStatus(connectorMessage, previousStatus);
    }

    @Override
    public void updateMaps(ConnectorMessage connectorMessage) {
        throwRandomError();
        super.updateMaps(connectorMessage);
    }

    @Override
    public void storeMessageContent(MessageContent messageContent) {
        throwRandomError();
        randomHang();
        super.storeMessageContent(messageContent);
    }

    @Override
    public void markAsProcessed(String channelId, long messageId) {
        throwRandomError();
        super.markAsProcessed(channelId, messageId);
    }

    private void throwRandomError() {
        if (errorPct > 0 && (Math.random() * 100) < errorPct) {
            System.err.println("DONKEY TEST: throwing a random exception");
            throw new DonkeyDaoException("random exception");
        }
    }

    private void randomHang() {
        if (hangPct > 0 && (Math.random() * 100) < hangPct) {
            System.err.println("DONKEY TEST: hanging randomly for " + hangMillis + "ms");

            try {
                Thread.sleep(hangMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("DONKEY TEST: interrupted");
            }
        }
    }
}
