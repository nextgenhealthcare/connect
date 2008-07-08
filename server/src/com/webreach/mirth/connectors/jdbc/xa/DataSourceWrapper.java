/*
 * $Header: /home/projects/mule/scm/mule/providers/jdbc/src/java/org/mule/providers/jdbc/xa/DataSourceWrapper.java,v 1.4 2005/06/03 01:20:34 gnt Exp $
 * $Revision: 1.4 $
 * $Date: 2005/06/03 01:20:34 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.jdbc.xa;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a> $Revision: 1.4 $
 */
public class DataSourceWrapper implements DataSource
{

    private XADataSource xads;
    private TransactionManager tm;

    public DataSourceWrapper()
    {
    }

    public DataSourceWrapper(XADataSource xads, TransactionManager tm)
    {
        this.xads = xads;
        this.tm = tm;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#getLoginTimeout()
     */
    public int getLoginTimeout() throws SQLException
    {
        return xads.getLoginTimeout();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#setLoginTimeout(int)
     */
    public void setLoginTimeout(int seconds) throws SQLException
    {
        xads.setLoginTimeout(seconds);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#getLogWriter()
     */
    public PrintWriter getLogWriter() throws SQLException
    {
        return xads.getLogWriter();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#setLogWriter(java.io.PrintWriter)
     */
    public void setLogWriter(PrintWriter out) throws SQLException
    {
        xads.setLogWriter(out);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#getConnection()
     */
    public Connection getConnection() throws SQLException
    {
        return new ConnectionWrapper(xads.getXAConnection(), tm);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#getConnection(java.lang.String,
     *      java.lang.String)
     */
    public Connection getConnection(String username, String password) throws SQLException
    {
        return new ConnectionWrapper(xads.getXAConnection(username, password), tm);
    }

    /**
     * @return Returns the transaction manager.
     */
    public TransactionManager getTransactionManager()
    {
        return tm;
    }

    /**
     * @param tm The transaction manager to set.
     */
    public void setTransactionManager(TransactionManager tm)
    {
        this.tm = tm;
    }

    /**
     * @return Returns the underlying XADataSource.
     */
    public XADataSource getXaDataSource()
    {
        return xads;
    }

    /**
     * @param xads The XADataSource to set.
     */
    public void setXaDataSource(XADataSource xads)
    {
        this.xads = xads;
    }
}
