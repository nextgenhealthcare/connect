/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util.javascript;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;

public class StoppableContext extends Context {
    private final static int INSTRUCTION_THRESHOLD = 1;
    
    private final AtomicBoolean running = new AtomicBoolean(true);
    private Logger logger = Logger.getLogger(this.getClass());

    public StoppableContext(ContextFactory contextFactory) {
        super(contextFactory);
        setInstructionObserverThreshold(INSTRUCTION_THRESHOLD);
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }
    
    @Override
    protected void observeInstructionCount(int count) {
        if (!running.get()) {
            logger.debug("Halting JavaScript execution");
            throw new Error();
        }
    }
}
