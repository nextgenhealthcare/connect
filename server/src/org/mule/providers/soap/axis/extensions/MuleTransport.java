/* 
* $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/axis/extensions/MuleTransport.java,v 1.1 2005/08/26 09:07:09 rossmason Exp $
* $Revision: 1.1 $
* $Date: 2005/08/26 09:07:09 $
* ------------------------------------------------------------------------------------------------------
* 
* Copyright (c) SymphonySoft Limited. All rights reserved.
* http://www.symphonysoft.com
* 
* The software in this package is published under the terms of the BSD
* style license a copy of which has been included with this distribution in
* the LICENSE.txt file. 
*
*/
package org.mule.providers.soap.axis.extensions;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.client.Transport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * todo document
 *
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.1 $
 */
public class MuleTransport extends Transport {

    /**
     * logger used by this class
     */
    protected transient Log log = LogFactory.getLog(getClass());

    public MuleTransport()
    {
        transportName = "MuleTransport";
    }


    /**
     * Set up any transport-specific derived properties in the message context.
     * @param context the context to set up
     * @param message the client service instance
     * @param engine the engine containing the registries
     * @throws org.apache.axis.AxisFault if service cannot be found
     */
    public void setupMessageContextImpl(MessageContext context,
                                        Call message,
                                        AxisEngine engine) throws AxisFault
    {
        if (log.isDebugEnabled()) {
            log.debug("Enter: MuleTransport::setupMessageContextImpl");
        }


        if (log.isDebugEnabled()) {
            log.debug("Exit: MuleTransport::setupMessageContextImpl");
        }
    }
}
