/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util.javascript;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

public class StoppableContextFactory extends ContextFactory {
    @Override
    protected Context makeContext() {
        return new StoppableContext(this);
    }
}
