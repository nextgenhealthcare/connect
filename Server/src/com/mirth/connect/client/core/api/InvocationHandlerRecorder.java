/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.core.api;

import java.lang.reflect.Method;

public interface InvocationHandlerRecorder {

    public void recordInvocation(Method method, Object[] args, Object result, Throwable t);
}
