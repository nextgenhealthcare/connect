/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package org.mule.providers;

import com.webreach.mirth.model.QueuedMessage;

public interface QueueEnabledMessageDispatcher {

	public boolean sendPayload(QueuedMessage thePayload) throws Exception;
	
	public void doDispose();
}
