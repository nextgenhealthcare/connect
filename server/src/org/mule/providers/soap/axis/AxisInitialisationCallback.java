/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/axis/AxisInitialisationCallback.java,v 1.6 2005/06/03 01:20:35 gnt Exp $
 * $Revision: 1.6 $
 * $Date: 2005/06/03 01:20:35 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.soap.axis;

import org.apache.axis.handlers.soap.SOAPService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.impl.InitialisationCallback;
import org.mule.umo.lifecycle.InitialisationException;

/**
 * <code>AxisInitialisationCallback</code> is invoked when an Axis component
 * is created from its descriptor.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.6 $
 */
public class AxisInitialisationCallback implements InitialisationCallback
{
    /**
     * logger used by this class
     */
    protected static transient Log logger = LogFactory.getLog(AxisInitialisationCallback.class);

    private SOAPService service;
    private boolean invoked = false;

    public AxisInitialisationCallback(SOAPService service)
    {
        this.service = service;
    }

    public void initialise(Object component) throws InitialisationException
    {
        // only call this once
        if (invoked)
            return;
        if (component instanceof AxisInitialisable) {
            if (logger.isDebugEnabled()) {
                logger.debug("Calling axis initialisation for component: " + component.getClass().getName());
            }
            ((AxisInitialisable) component).initialise(service);
        }
        invoked = true;
    }
}
