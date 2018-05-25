/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core;

import java.util.List;

public interface ListHandler {
    public List<?> getFirstPage() throws ListHandlerException;

    public List<?> getNextPage() throws ListHandlerException;

    public List<?> getPreviousPage() throws ListHandlerException;

    public int getSize() throws ListHandlerException;

    public void resetIndex();
}
