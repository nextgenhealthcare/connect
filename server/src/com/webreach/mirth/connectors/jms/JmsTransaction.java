/* 
 * $Header: /home/projects/mule/scm/mule/providers/jms/src/java/org/mule/providers/jms/JmsTransaction.java,v 1.6 2005/06/03 01:20:34 gnt Exp $
 * $Revision: 1.6 $
 * $Date: 2005/06/03 01:20:34 $
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */

package com.webreach.mirth.connectors.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.transaction.AbstractSingleResourceTransaction;
import org.mule.transaction.IllegalTransactionStateException;
import org.mule.umo.TransactionException;

/**
 * <p>
 * <code>JmsTransaction</code> is a wrapper for a Jms local transaction. This
 * object holds the jms session and controls the when the transaction committed
 * or rolled back.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author Guillaume Nodet
 * @version $Revision: 1.6 $
 */
public class JmsTransaction extends AbstractSingleResourceTransaction
{

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.UMOTransaction#bindResource(java.lang.Object,
     *      java.lang.Object)
     */
    public void bindResource(Object key, Object resource) throws TransactionException
    {
        if (!(key instanceof Connection) || !(resource instanceof Session)) {
            throw new IllegalTransactionStateException(new Message(Messages.TX_CAN_ONLY_BIND_TO_X_TYPE_RESOURCES,
                                                                   "javax.jms.Connection/javax.jms.Session"));
        }
        Session session = (Session) resource;
        try {
            if (!session.getTransacted()) {
                throw new IllegalTransactionStateException(new Message("jms", 4));
            }
        } catch (JMSException e) {
            throw new IllegalTransactionStateException(new Message(Messages.TX_CANT_READ_STATE), e);
        }
        super.bindResource(key, resource);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.AbstractSingleResourceTransaction#doBegin()
     */
    protected void doBegin() throws TransactionException
    {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.AbstractSingleResourceTransaction#doCommit()
     */
    protected void doCommit() throws TransactionException
    {
        try {
            ((Session) resource).commit();
        } catch (JMSException e) {
            throw new TransactionException(new Message(Messages.TX_COMMIT_FAILED), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.AbstractSingleResourceTransaction#doRollback()
     */
    protected void doRollback() throws TransactionException
    {
        try {
            ((Session) resource).rollback();
        } catch (JMSException e) {
            throw new TransactionException(new Message(Messages.TX_ROLLBACK_FAILED), e);
        }
    }

}
