/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/glue/GlueInitialisable.java,v 1.4 2005/06/03 01:20:35 gnt Exp $
 * $Revision: 1.4 $
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

import org.mule.umo.lifecycle.InitialisationException;

import electric.glue.context.ServiceContext;
import electric.service.IService;

/**
 * <code>GlueInitialisable</code> can be implemented by a Mule component that
 * will be used as an Glue Soap service to customise the Glue Service object
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.4 $
 */
public interface GlueInitialisable
{
    public void initialise(IService service, ServiceContext context) throws InitialisationException;
}
