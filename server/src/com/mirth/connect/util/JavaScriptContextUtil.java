/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.util;

import org.mozilla.javascript.Context;

public class JavaScriptContextUtil {

    /*
     * Retrieves the Context for the current Thread. The context must be cleaned up with
     * Context.exit() when it is no longer needed.
     */
    public static Context getGlobalContextForValidation() {
        Context context = Context.enter();
        context.setOptimizationLevel(-1);
        return context;
    }
}
