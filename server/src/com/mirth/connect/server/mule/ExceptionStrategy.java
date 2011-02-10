/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.mule.impl.DefaultComponentExceptionStrategy;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.ConnectException;
import org.mule.providers.FatalConnectException;

import com.mirth.connect.model.Event;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;

public class ExceptionStrategy extends DefaultComponentExceptionStrategy {

	// ast: call to checkForConnectException
	protected void defaultHandler(Throwable t) {
		checkForConnectException(t);
		super.defaultHandler(t);
	}

	// ast: if the exception is a ConnectException, and the object is a
	// receiver, then, the component is stopped.
	protected void checkForConnectException(Object obj) {
		if (obj instanceof ConnectException) {
			ConnectException connectException = (ConnectException) obj;
			checkForConnectException(connectException.getComponent());
		} else if (obj instanceof AbstractMessageReceiver) {
			AbstractMessageReceiver abstractMessageReceiver = (AbstractMessageReceiver) obj;
			
			try {
				logger.error("Stopping channel " + abstractMessageReceiver);
				abstractMessageReceiver.getComponent().stop();
			} catch (Throwable t2) {
				logger.error("Error stopping channel " + abstractMessageReceiver + " \n" + t2);
			}
		} else if (obj instanceof FatalConnectException) {
			FatalConnectException fatalConnectException = (FatalConnectException) obj;
			checkForConnectException(fatalConnectException.getComponent());
		}
	}

	protected void logException(Throwable t) {
		EventController eventController = ControllerFactory.getFactory().createEventController();
		Event event = new Event("Exception occured in channel.");
		event.getAttributes().put(Event.ATTR_EXCEPTION, ExceptionUtils.getStackTrace(t));
		eventController.addEvent(event);
	}
}
