/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.transaction.AbstractSingleResourceTransaction;
import org.mule.transaction.TransactionRollbackException;
import org.mule.umo.TransactionException;

/**
 * @author Guillaume Nodet
 * @version $Revision: 1.5 $
 */
public class JdbcTransaction extends AbstractSingleResourceTransaction {

    public JdbcTransaction() {}

    public Object getResource(Object key) {
        return key != null && this.key != null && this.key.equals(key) ? this.resource : null;
    }

    public boolean hasResource(Object key) {
        return key != null && this.key != null && this.key.equals(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.UMOTransaction#bindResource(java.lang.Object,
     * java.lang.Object)
     */
    public void bindResource(Object key, Object resource) throws TransactionException {
        // if (((key != null)&&!(key instanceof DataSource)) || !(resource
        // instanceof Connection)) {
        // throw new IllegalTransactionStateException(new
        // Message(Messages.TX_CAN_ONLY_BIND_TO_X_TYPE_RESOURCES,
        // "javax.sql.DataSource/java.sql.Connection"));
        // }
        Connection con = (Connection) resource;

        try {
            if (con.getAutoCommit()) {
                con.setAutoCommit(false);
            }
        } catch (SQLException e) {
            throw new TransactionException(new Message(Messages.TX_SET_AUTO_COMMIT_FAILED), e);
        }
        super.bindResource(key, resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.AbstractSingleResourceTransaction#doBegin()
     */
    protected void doBegin() throws TransactionException {
    // Do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.AbstractSingleResourceTransaction#doCommit()
     */
    protected void doCommit() throws TransactionException {
        try {
            ((Connection) resource).commit();
            ((Connection) resource).close();
        } catch (SQLException e) {
            throw new TransactionException(new Message(Messages.TX_COMMIT_FAILED), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.AbstractSingleResourceTransaction#doRollback()
     */
    protected void doRollback() throws TransactionException {
        try {
            ((Connection) resource).rollback();
            ((Connection) resource).close();
        } catch (SQLException e) {
            throw new TransactionRollbackException(new Message(Messages.TX_ROLLBACK_FAILED), e);
        }
    }

}
