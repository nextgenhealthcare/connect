/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.vm;

import org.mule.MuleManager;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.transaction.AbstractSingleResourceTransaction;
import org.mule.transaction.IllegalTransactionStateException;
import org.mule.umo.TransactionException;
import org.mule.util.queue.QueueManager;
import org.mule.util.queue.QueueSession;
import org.mule.util.xa.ResourceManagerException;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.2 $
 */
public class VMTransaction extends AbstractSingleResourceTransaction {

    public VMTransaction() throws TransactionException {
        QueueManager qm = MuleManager.getInstance().getQueueManager();
        QueueSession qs = qm.getQueueSession();
        bindResource(qm, qs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.UMOTransaction#bindResource(java.lang.Object,
     * java.lang.Object)
     */
    public void bindResource(Object key, Object resource) throws TransactionException {
        if (!(key instanceof QueueManager) || !(resource instanceof QueueSession)) {
            throw new IllegalTransactionStateException(new Message(Messages.TX_CAN_ONLY_BIND_TO_X_TYPE_RESOURCES, "QueueManager/QueueSession"));
        }
        super.bindResource(key, resource);
    }

    protected void doBegin() throws TransactionException {
        try {
            ((QueueSession) resource).begin();
        } catch (ResourceManagerException e) {
            throw new TransactionException(new Message(Messages.TX_CANT_START_X_TRANSACTION, "VMTransaction"), e);
        }
    }

    protected void doCommit() throws TransactionException {
        try {
            ((QueueSession) resource).commit();
        } catch (ResourceManagerException e) {
            throw new TransactionException(new Message(Messages.TX_COMMIT_FAILED), e);
        }
    }

    protected void doRollback() throws TransactionException {
        try {
            ((QueueSession) resource).rollback();
        } catch (ResourceManagerException e) {
            throw new TransactionException(new Message(Messages.TX_ROLLBACK_FAILED), e);
        }
    }

}
