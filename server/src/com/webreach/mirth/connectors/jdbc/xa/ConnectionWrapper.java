/*
 * $Header: /home/projects/mule/scm/mule/providers/jdbc/src/java/org/mule/providers/jdbc/xa/ConnectionWrapper.java,v 1.4 2005/06/03 01:20:34 gnt Exp $
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;

import javax.sql.XAConnection;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a> $Revision: 1.4 $
 */
public class ConnectionWrapper implements Connection
{

    private XAConnection xaCon;
    private Connection con;
    private TransactionManager tm;
    private Transaction tx;

    public ConnectionWrapper(XAConnection xaCon, TransactionManager tm) throws SQLException
    {
        this.xaCon = xaCon;
        this.con = xaCon.getConnection();
        this.tm = tm;
        this.tx = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#getHoldability()
     */
    public int getHoldability() throws SQLException
    {
        return con.getHoldability();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#getTransactionIsolation()
     */
    public int getTransactionIsolation() throws SQLException
    {
        return con.getTransactionIsolation();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#clearWarnings()
     */
    public void clearWarnings() throws SQLException
    {
        con.clearWarnings();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#close()
     */
    public void close() throws SQLException
    {
        con.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#commit()
     */
    public void commit() throws SQLException
    {
        con.commit();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#rollback()
     */
    public void rollback() throws SQLException
    {
        con.rollback();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#getAutoCommit()
     */
    public boolean getAutoCommit() throws SQLException
    {
        return con.getAutoCommit();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#isClosed()
     */
    public boolean isClosed() throws SQLException
    {
        return con.isClosed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#isReadOnly()
     */
    public boolean isReadOnly() throws SQLException
    {
        return con.isReadOnly();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#setHoldability(int)
     */
    public void setHoldability(int holdability) throws SQLException
    {
        con.setHoldability(holdability);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#setTransactionIsolation(int)
     */
    public void setTransactionIsolation(int level) throws SQLException
    {
        con.setTransactionIsolation(level);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#setAutoCommit(boolean)
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException
    {
        con.setAutoCommit(autoCommit);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#setReadOnly(boolean)
     */
    public void setReadOnly(boolean readOnly) throws SQLException
    {
        con.setReadOnly(readOnly);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#getCatalog()
     */
    public String getCatalog() throws SQLException
    {
        return con.getCatalog();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#setCatalog(java.lang.String)
     */
    public void setCatalog(String catalog) throws SQLException
    {
        con.setCatalog(catalog);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#getMetaData()
     */
    public DatabaseMetaData getMetaData() throws SQLException
    {
        return con.getMetaData();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#getWarnings()
     */
    public SQLWarning getWarnings() throws SQLException
    {
        return con.getWarnings();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#setSavepoint()
     */
    public Savepoint setSavepoint() throws SQLException
    {
        return con.setSavepoint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
     */
    public void releaseSavepoint(Savepoint savepoint) throws SQLException
    {
        con.releaseSavepoint(savepoint);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#rollback(java.sql.Savepoint)
     */
    public void rollback(Savepoint savepoint) throws SQLException
    {
        con.rollback();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#createStatement()
     */
    public Statement createStatement() throws SQLException
    {
        Statement st = con.createStatement();
        return (Statement) Proxy.newProxyInstance(Statement.class.getClassLoader(),
                                                  new Class[] { Statement.class },
                                                  new StatementInvocationHandler(st));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#createStatement(int, int)
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
    {
        Statement st = con.createStatement(resultSetType, resultSetConcurrency);
        return (Statement) Proxy.newProxyInstance(Statement.class.getClassLoader(),
                                                  new Class[] { Statement.class },
                                                  new StatementInvocationHandler(st));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#createStatement(int, int, int)
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException
    {
        Statement st = con.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        return (Statement) Proxy.newProxyInstance(Statement.class.getClassLoader(),
                                                  new Class[] { Statement.class },
                                                  new StatementInvocationHandler(st));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#getTypeMap()
     */
    public Map getTypeMap() throws SQLException
    {
        return con.getTypeMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#setTypeMap(java.util.Map)
     */
    public void setTypeMap(Map map) throws SQLException
    {
        con.setTypeMap(map);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#nativeSQL(java.lang.String)
     */
    public String nativeSQL(String sql) throws SQLException
    {
        return con.nativeSQL(sql);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#prepareCall(java.lang.String)
     */
    public CallableStatement prepareCall(String sql) throws SQLException
    {
        CallableStatement cs = con.prepareCall(sql);
        return (CallableStatement) Proxy.newProxyInstance(CallableStatement.class.getClassLoader(),
                                                          new Class[] { CallableStatement.class },
                                                          new StatementInvocationHandler(cs));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
     */
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
        CallableStatement cs = con.prepareCall(sql, resultSetType, resultSetConcurrency);
        return (CallableStatement) Proxy.newProxyInstance(CallableStatement.class.getClassLoader(),
                                                          new Class[] { CallableStatement.class },
                                                          new StatementInvocationHandler(cs));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
     */
    public CallableStatement prepareCall(String sql,
                                         int resultSetType,
                                         int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException
    {
        CallableStatement cs = con.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        return (CallableStatement) Proxy.newProxyInstance(CallableStatement.class.getClassLoader(),
                                                          new Class[] { CallableStatement.class },
                                                          new StatementInvocationHandler(cs));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#prepareStatement(java.lang.String)
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        PreparedStatement ps = con.prepareStatement(sql);
        return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                                                          new Class[] { PreparedStatement.class },
                                                          new StatementInvocationHandler(ps));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#prepareStatement(java.lang.String, int)
     */
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
    {
        PreparedStatement ps = con.prepareStatement(sql, autoGeneratedKeys);
        return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                                                          new Class[] { PreparedStatement.class },
                                                          new StatementInvocationHandler(ps));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException
    {
        PreparedStatement ps = con.prepareStatement(sql, resultSetType, resultSetConcurrency);
        return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                                                          new Class[] { PreparedStatement.class },
                                                          new StatementInvocationHandler(ps));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#prepareStatement(java.lang.String, int, int,
     *      int)
     */
    public PreparedStatement prepareStatement(String sql,
                                              int resultSetType,
                                              int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException
    {
        PreparedStatement ps = con.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                                                          new Class[] { PreparedStatement.class },
                                                          new StatementInvocationHandler(ps));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
     */
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
    {
        PreparedStatement ps = con.prepareStatement(sql, columnIndexes);
        return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                                                          new Class[] { PreparedStatement.class },
                                                          new StatementInvocationHandler(ps));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#setSavepoint(java.lang.String)
     */
    public Savepoint setSavepoint(String name) throws SQLException
    {
        return con.setSavepoint(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.sql.Connection#prepareStatement(java.lang.String,
     *      java.lang.String[])
     */
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
    {
        PreparedStatement ps = con.prepareStatement(sql, columnNames);
        return (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                                                          new Class[] { PreparedStatement.class },
                                                          new StatementInvocationHandler(ps));
    }

    protected void enlist() throws Exception
    {
        if (tm != null && tx == null) {
            tx = tm.getTransaction();
            if (tx != null) {
                tx.enlistResource(xaCon.getXAResource());
            }
        }
    }

    protected class StatementInvocationHandler implements InvocationHandler
    {

        private Statement statement;

        public StatementInvocationHandler(Statement statement)
        {
            this.statement = statement;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
         *      java.lang.reflect.Method, java.lang.Object[])
         */
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if (method.getName().startsWith("execute")) {
                enlist();
            }
            try {
                return method.invoke(statement, args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }

    }

}
