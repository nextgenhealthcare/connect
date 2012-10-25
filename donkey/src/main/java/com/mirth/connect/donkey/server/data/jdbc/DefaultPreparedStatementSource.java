/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DefaultPreparedStatementSource implements PreparedStatementSource {
    private Connection connection;
    private QuerySource querySource;

    public DefaultPreparedStatementSource(Connection connection, QuerySource querySource) {
        this.connection = connection;
        this.querySource = querySource;
    }

    @Override
    public PreparedStatement getPreparedStatement(String queryId, Long localChannelId) throws SQLException {
        if (localChannelId == null) {
            return connection.prepareStatement(querySource.getQuery(queryId));
        } else {
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("localChannelId", localChannelId);
            return connection.prepareStatement(querySource.getQuery(queryId, values));
        }
    }

    @Override
    public PreparedStatement getPreparedStatement(String queryId, Long localChannelId, Map<String, Object> values) throws SQLException {
        if (localChannelId != null) {
            values.put("localChannelId", localChannelId);
        }
        return connection.prepareStatement(querySource.getQuery(queryId, values));
    }
}
