/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/glue/GlueInitialisationCallback.java,v 1.5 2005/06/03 01:20:35 gnt Exp $
 * $Revision: 1.5 $
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
package org.mule.providers.soap.glue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.config.i18n.Message;
import org.mule.impl.InitialisationCallback;
import org.mule.umo.lifecycle.InitialisationException;

import electric.glue.context.ServiceContext;
import electric.registry.Registry;
import electric.registry.RegistryException;
import electric.service.IService;

/**
 * <code>GlueInitialisationCallback</code> is invoked when an Glue service
 * component is created from its descriptor.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.5 $
 */
public class GlueInitialisationCallback implements InitialisationCallback
{
    /**
     * logger used by this class
     */
    protected static transient Log logger = LogFactory.getLog(GlueInitialisationCallback.class);

    private IService service;
    private ServiceContext context;
    private String servicePath;
    private boolean invoked = false;

    public GlueInitialisationCallback(IService service, String path, ServiceContext context)
    {
        this.service = service;
        this.servicePath = path;
        this.context = context;
        if (context == null)
            this.context = new ServiceContext();
    }

    public void initialise(Object component) throws InitialisationException
    {
        // only call this once
        if (invoked)
            return;
        if (component instanceof GlueInitialisable) {
            if (logger.isDebugEnabled()) {
                logger.debug("Calling axis initialisation for component: " + component.getClass().getName());
            }
            ((GlueInitialisable) component).initialise(service, context);
        }
        invoked = true;
        try {
            Registry.publish(servicePath, service, context);
        } catch (RegistryException e) {
            throw new InitialisationException(new Message("soap", 3, component.getClass().getName()), e, this);
        }
    }
}
