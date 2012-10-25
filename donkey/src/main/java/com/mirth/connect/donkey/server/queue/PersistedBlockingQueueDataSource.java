/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.donkey.server.queue;

import java.util.List;

public interface PersistedBlockingQueueDataSource<T> {
    public int getSize();

    public List<T> getItems(int offset, int limit);
}
