/*
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE-MULE.txt file.
 */

package org.mule.util.queue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.doomdark.uuid.UUIDGenerator;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.2 $
 */
public class MemoryPersistenceStrategy implements QueuePersistenceStrategy {

	private UUIDGenerator gen = UUIDGenerator.getInstance();

	private Map map = Collections.synchronizedMap(new HashMap());

	protected Object getId(Object obj) {
		return gen.generateRandomBasedUUID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.transaction.xa.queue.QueuePersistenceStrategy#store(java.lang.Object)
	 */
	public Object store(String queue, Object obj) throws IOException {
		if (obj == null) {
			throw new IllegalArgumentException();
		}
		Object id = getId(obj);
		map.put(id, obj);
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.transaction.xa.queue.QueuePersistenceStrategy#load(java.lang.Object)
	 */
	public Object load(String queue, Object id) throws IOException {
		return map.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.transaction.xa.queue.QueuePersistenceStrategy#remove(java.lang.Object)
	 */
	public void remove(String queue, Object id) throws IOException {
		map.remove(id);
	}

	public void removeQueue(String queue) throws IOException {
		map.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.transaction.xa.queue.QueuePersistenceStrategy#restore()
	 */
	public List restore() throws IOException {
		return new ArrayList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.transaction.xa.queue.QueuePersistenceStrategy#open()
	 */
	public void open() throws IOException {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.transaction.xa.queue.QueuePersistenceStrategy#close()
	 */
	public void close() throws IOException {}

}
