/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util.javascript;

import java.util.concurrent.Callable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import com.mirth.connect.donkey.util.ThreadUtils;

public abstract class JavaScriptTask<T> implements Callable<T> {
    private Context context;
    
    protected Context getContext() {
        return context;
    }

    public Object executeScript(Script compiledScript, Scriptable scope) throws InterruptedException {
        // if the executor is halting this task, we don't want to initialize the context yet
        synchronized (this) {
            ThreadUtils.checkInterruptedStatus();
            context = JavaScriptScopeUtil.getContext();

            if (context instanceof StoppableContext) {
                ((StoppableContext) context).setRunning(true);
            }
        }

        return compiledScript.exec(context, scope);
    }
}
