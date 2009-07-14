/* 
 * $Header: /home/projects/mule/scm/mule/providers/jms/src/java/org/mule/providers/jms/JmsTransactionFactory.java,v 1.7 2005/06/03 01:20:34 gnt Exp $
 * $Revision: 1.7 $
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

import org.mule.umo.TransactionException;
import org.mule.umo.UMOTransaction;
import org.mule.umo.UMOTransactionFactory;

/**
 * <p>
 * <code>JmsTransactionFactory</code> Creates a Jms local transaction
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @author Guillaume Nodet
 * @version $Revision: 1.7 $
 */
public class JmsTransactionFactory implements UMOTransactionFactory
{
    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.UMOTransactionFactory#beginTransaction(org.mule.umo.provider.UMOMessageDispatcher)
     */
    public UMOTransaction beginTransaction() throws TransactionException
    {
        JmsTransaction tx = new JmsTransaction();
        tx.begin();
        return tx;
    }

    public boolean isTransacted()
    {
        return true;
    }

}
