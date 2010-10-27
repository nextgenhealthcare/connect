/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package org.mule.util.queue;

import java.util.List;

import org.mule.util.xa.ResourceManagerSystemException;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.2 $
 */
public interface QueueManager
{

    void start() throws ResourceManagerSystemException;

    void stop() throws ResourceManagerSystemException;

    QueueSession getQueueSession();

    void close();

    void setDefaultQueueConfiguration(QueueConfiguration config);

    void setQueueConfiguration(String queueName, QueueConfiguration config);
    
    List<String> getAllQueueNames();
}
