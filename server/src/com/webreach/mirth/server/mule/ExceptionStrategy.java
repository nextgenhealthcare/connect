/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */


package com.webreach.mirth.server.mule;

import org.mule.impl.DefaultComponentExceptionStrategy;

import com.webreach.mirth.model.SystemEvent;
import com.webreach.mirth.server.controllers.SystemLogger;
import com.webreach.mirth.server.util.StackTracePrinter;

public class ExceptionStrategy extends DefaultComponentExceptionStrategy {
	protected void defaultHandler(Throwable t) {
		super.defaultHandler(t);
	}
	
	protected void logException(Throwable t) {
		SystemLogger systemLogger = new SystemLogger();
		StackTracePrinter stackTracePrinter = new StackTracePrinter();
		SystemEvent event = new SystemEvent("Exception occured in channel.");
		event.setDescription(stackTracePrinter.stackTraceToString(t));
		systemLogger.logSystemEvent(event);
	}
}
