/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package org.mule.util.queue;

/**
 * <code>Queue</code> TODO
 * 
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.3 $
 */
public interface Queue
{

    /**
     * Returns the number of elements in this queue.
     * 
     * @return
     */
    int size();

    /**
     * Puts a new object in this queue and wait if necessary.
     * 
     * @param o the object to put
     */
    void put(Object o) throws Exception;

    /**
     * Blocks and retrieves an object from this queue.
     * 
     * @return an object.
     */
    Object take() throws InterruptedException;
    
    void remove(Object id) throws Exception;

    Object peek() throws InterruptedException;

    Object poll(long timeout) throws InterruptedException;

    boolean offer(Object o, long timeout) throws Exception;

    void delete() throws Exception;
    
    public Object removeTop() throws Exception;
}
