/* 
 * $Header: /home/projects/mule/scm/mule/providers/vm/src/java/org/mule/providers/vm/VMTransactionFactory.java,v 1.2 2005/06/03 01:20:36 gnt Exp $
 * $Revision: 1.2 $
 * $Date: 2005/06/03 01:20:36 $
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
package com.webreach.mirth.connectors.vm;

import org.mule.umo.TransactionException;
import org.mule.umo.UMOTransaction;
import org.mule.umo.UMOTransactionFactory;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.2 $
 */
public class VMTransactionFactory implements UMOTransactionFactory
{

    public UMOTransaction beginTransaction() throws TransactionException
    {
        VMTransaction tx = new VMTransaction();
        tx.begin();
        return tx;
    }

    public boolean isTransacted()
    {
        return true;
    }

}
