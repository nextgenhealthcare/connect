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

public class CachedPreparedStatementSource implements PreparedStatementSource {
    private Map<Long, Map<String, PreparedStatement>> statements = new HashMap<Long, Map<String, PreparedStatement>>();
    private QuerySource querySource;
    private Connection connection;

    public CachedPreparedStatementSource(Connection connection, QuerySource querySource) {
        this.connection = connection;
        this.querySource = querySource;
    }

    @Override
    public PreparedStatement getPreparedStatement(String queryId, Long localChannelId) throws SQLException {
        PreparedStatement statement = null;
        Map<String, PreparedStatement> channelStatements = statements.get(localChannelId);

        if (channelStatements == null) {
            channelStatements = new HashMap<String, PreparedStatement>();
            statements.put(localChannelId, channelStatements);
        } else {
            statement = channelStatements.get(queryId);
        }

        if (statement == null || statement.isClosed()) {
            statement = null;

            if (localChannelId == null) {
                String query = querySource.getQuery(queryId);

                if (query != null) {
                    statement = connection.prepareStatement(query);
                }
            } else {
                Map<String, Object> values = new HashMap<String, Object>();
                values.put("localChannelId", localChannelId);

                String query = querySource.getQuery(queryId, values);

                if (query != null) {
                    statement = connection.prepareStatement(query);
                }
            }

            channelStatements.put(queryId, statement);
        }

        return statement;
    }
}
