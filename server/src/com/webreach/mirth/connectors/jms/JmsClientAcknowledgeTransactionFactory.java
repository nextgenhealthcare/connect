/*
 * $Header: /home/projects/mule/scm/mule/providers/jms/src/java/org/mule/providers/jms/JmsClientAcknowledgeTransactionFactory.java,v 1.6 2005/06/03 01:20:34 gnt Exp $
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
 */
package com.webreach.mirth.connectors.jms;

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

public class JmsClientAcknowledgeTransactionFactory implements UMOTransactionFactory
{
    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.UMOTransactionFactory#beginTransaction(java.lang.Object)
     */
    public UMOTransaction beginTransaction() throws TransactionException
    {
        JmsClientAcknowledgeTransaction tx = new JmsClientAcknowledgeTransaction();
        tx.begin();
        return tx;
    }

    public boolean isTransacted()
    {
        return false;
    }
}
