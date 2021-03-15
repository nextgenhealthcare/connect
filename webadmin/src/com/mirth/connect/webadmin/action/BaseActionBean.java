/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.webadmin.action;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;

public abstract class BaseActionBean implements ActionBean {
    private BaseActionBeanContext context;

    public void setContext(ActionBeanContext context) {
        this.context = (BaseActionBeanContext) context;
    }

    /** Gets the ActionBeanContext set by Stripes during initialization. */
    public BaseActionBeanContext getContext() {
        return this.context;
    }
    
    public String getSlashedContextPath(String contextPath) {
		// Add a starting slash if one does not exist
		if (!contextPath.startsWith("/")) {
		    contextPath = "/" + contextPath;
		}

		// Remove a trailing slash if one exists
		if (contextPath.endsWith("/")) {
		    contextPath = contextPath.substring(0, contextPath.length() - 1);
		}
		return contextPath;
	}
}