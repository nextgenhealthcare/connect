/*
 * $Header: /home/projects/mule/scm/mule/mule/src/java/org/mule/management/mbeans/ConnectorServiceMBean.java,v 1.2 2005/08/01 16:50:14 aperepel Exp $
 * $Revision: 1.2 $
 * $Date: 2005/08/01 16:50:14 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.management.mbeans;

import org.mule.umo.UMOException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.lifecycle.RecoverableException;
import org.mule.umo.provider.UMOMessageDispatcherFactory;

import java.beans.ExceptionListener;

/**
 * @author <a href="mailto:aperepel@itci.com">Andrew Perepelytsya</a>
 * 
 * $Id: ConnectorServiceMBean.java,v 1.2 2005/08/01 16:50:14 aperepel Exp $
 */
public interface ConnectorServiceMBean {

	boolean isStarted();

	boolean isDisposed();

	boolean isDisposing();

	String getName();

	String getProtocol();

	ExceptionListener getExceptionListener();

	UMOMessageDispatcherFactory getDispatcherFactory();

	void startConnector() throws UMOException;

	void stopConnector() throws UMOException;

	void dispose();

	void initialise() throws InitialisationException, RecoverableException;
	
	String getStatusMode();
	
	String getStatusMessage();
}
