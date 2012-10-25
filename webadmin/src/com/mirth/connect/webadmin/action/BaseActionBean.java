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
}