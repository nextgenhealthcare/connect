/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package com.webreach.mirth.connectors.jdbc;

import org.mule.umo.TransactionException;
import org.mule.umo.UMOTransaction;
import org.mule.umo.UMOTransactionFactory;

/**
 * @author Guillaume Nodet
 * @version $Revision: 1.5 $
 */
public class JdbcTransactionFactory implements UMOTransactionFactory {

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.UMOTransactionFactory#beginTransaction()
     */
    public UMOTransaction beginTransaction() throws TransactionException {
        JdbcTransaction tx = new JdbcTransaction();
        tx.begin();
        return tx;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.mule.umo.UMOTransactionFactory#isTransacted()
     */
    public boolean isTransacted() {
        return true;
    }

}
