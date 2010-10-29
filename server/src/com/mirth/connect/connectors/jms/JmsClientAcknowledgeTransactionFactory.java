/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.mirth.connect.connectors.jms;

import org.mule.umo.TransactionException;
import org.mule.umo.UMOTransaction;
import org.mule.umo.UMOTransactionFactory;

/**
 * <code>JmsClientAcknowledgeTransactionFactory</code> creates a Jms Client
 * Acknowledge transaction using a Jms message.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author Guillaume Nodet
 * @version $Revision: 1.6 $
 */

public class JmsClientAcknowledgeTransactionFactory implements UMOTransactionFactory {
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.mule.umo.UMOTransactionFactory#beginTransaction(java.lang.Object)
     */
    public UMOTransaction beginTransaction() throws TransactionException {
        JmsClientAcknowledgeTransaction tx = new JmsClientAcknowledgeTransaction();
        tx.begin();
        return tx;
    }

    public boolean isTransacted() {
        return false;
    }
}
