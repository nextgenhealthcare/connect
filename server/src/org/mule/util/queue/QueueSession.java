/* 
 * $Header: /home/projects/mule/scm/mule/mule/src/java/org/mule/util/queue/QueueSession.java,v 1.2 2005/06/03 01:20:30 gnt Exp $
 * $Revision: 1.2 $
 * $Date: 2005/06/03 01:20:30 $
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
package org.mule.util.queue;

import org.mule.util.xa.ResourceManagerException;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.2 $
 */
public interface QueueSession
{

    Queue getQueue(String name);
    
    Queue resyncQueue(String name);
    
    void begin() throws ResourceManagerException;

    void commit() throws ResourceManagerException;

    void rollback() throws ResourceManagerException;
    
    void deleteQueue(String name) throws Exception;
}
