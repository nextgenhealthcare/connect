/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util.javascript;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class MirthJavaScriptThread extends Thread {

    private Context context;
    private Scriptable scope;

    MirthJavaScriptThread(ThreadGroup group, Runnable target, String name, long stackSize) {
        super(group, target, name, stackSize);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Scriptable getScope() {
        return scope;
    }

    public void setScope(Scriptable scope) {
        this.scope = scope;
    }
}