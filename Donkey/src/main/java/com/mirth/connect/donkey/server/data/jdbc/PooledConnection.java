/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.data.jdbc;

import java.sql.Connection;

public class PooledConnection {
    private Connection connection;
    private Connection internalConnection;

    public PooledConnection(Connection connection, Connection internalConnection) {
        this.connection = connection;
        this.internalConnection = internalConnection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getInternalConnection() {
        return internalConnection;
    }

    public void setInternalConnection(Connection internalConnection) {
        this.internalConnection = internalConnection;
    }
}
