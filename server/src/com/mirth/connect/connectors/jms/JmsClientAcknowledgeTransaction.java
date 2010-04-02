/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.mule.config.i18n.Messages;
import org.mule.transaction.AbstractSingleResourceTransaction;
import org.mule.transaction.IllegalTransactionStateException;
import org.mule.umo.TransactionException;

/**
 * <p>
 * <code>JmsClientAcknowledgeTransaction</code> is a transaction implementation
 * of performing a message acknowledgement. There is no notion of rollback with
 * client acknowledgement, but this transaction can be useful for controlling
 * how messages are consumed from a destination.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author Guillaume Nodet
 * @version $Revision: 1.6 $
 */
public class JmsClientAcknowledgeTransaction extends AbstractSingleResourceTransaction {
    private Message message;

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.AbstractSingleResourceTransaction#doBegin()
     */
    protected void doBegin() throws TransactionException {
    // nothing to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.AbstractSingleResourceTransaction#doCommit()
     */
    protected void doCommit() throws TransactionException {
        try {
            if (message == null) {
                throw new IllegalTransactionStateException(new org.mule.config.i18n.Message("jms", 6));
            }
            message.acknowledge();
        } catch (JMSException e) {
            throw new IllegalTransactionStateException(new org.mule.config.i18n.Message(Messages.TX_COMMIT_FAILED), e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.transaction.AbstractSingleResourceTransaction#doRollback()
     */
    protected void doRollback() throws TransactionException {
        // If a message has been bound, rollback is forbidden
        if (message != null) {
            throw new UnsupportedOperationException("Jms Client Acknowledge doesn't support rollback");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.UMOTransaction#bindResource(java.lang.Object,
     * java.lang.Object)
     */
    public void bindResource(Object key, Object resource) throws TransactionException {
        if (key instanceof Message) {
            this.message = (Message) key;
            return;
        }
        if (!(key instanceof Connection) || !(resource instanceof Session)) {
            throw new IllegalTransactionStateException(new org.mule.config.i18n.Message(Messages.TX_CAN_ONLY_BIND_TO_X_TYPE_RESOURCES, "javax.jms.Connection/javax.jms.Session"));
        }
        Session session = (Session) resource;
        try {
            if (session.getTransacted()) {
                throw new IllegalTransactionStateException(new org.mule.config.i18n.Message("jms", 5));
            }
        } catch (JMSException e) {
            throw new IllegalTransactionStateException(new org.mule.config.i18n.Message(Messages.TX_CANT_READ_STATE), e);
        }
        super.bindResource(key, resource);
    }
}
