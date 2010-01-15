/*
 * $Header: /home/projects/mule/scm/mule/providers/soap/src/java/org/mule/providers/soap/SoapServiceFinder.java,v 1.3 2005/06/03 01:20:34 gnt Exp $
 * $Revision: 1.3 $
 * $Date: 2005/06/03 01:20:34 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package com.webreach.mirth.connectors.soap;

import org.mule.providers.service.ConnectorFactory;
import org.mule.providers.service.ConnectorFactoryException;
import org.mule.providers.service.ConnectorServiceDescriptor;
import org.mule.providers.service.ConnectorServiceFinder;
import org.mule.providers.service.ConnectorServiceNotFoundException;
import org.mule.util.ClassHelper;

/**
 * <code>SoapServiceFinder</code> finds a the connector service to use by
 * checking the classpath for jars required for each of the soap connector
 * implementations
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.3 $
 */
public class SoapServiceFinder implements ConnectorServiceFinder {
	public static final String AXIS_CLASS = "org.apache.axis.AxisEngine";

	public ConnectorServiceDescriptor findService(String service) throws ConnectorFactoryException {
		try {
			ClassHelper.loadClass(AXIS_CLASS, getClass());
			return ConnectorFactory.getServiceDescriptor("axis");
		} catch (ClassNotFoundException e) {
			throw new ConnectorServiceNotFoundException("Could not find Axis on the classpath");
		}
	}
}
