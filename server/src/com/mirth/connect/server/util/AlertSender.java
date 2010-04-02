/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.server.util;

import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.ControllerFactory;

public class AlertSender {
	private AlertController alertController = ControllerFactory.getFactory().createAlertController();
	private String channelId;
	
	public AlertSender(String channelId) {
		this.channelId = channelId;
	}
	
	public void sendAlert(String errorMessage) {
		alertController.sendAlerts(channelId, Constants.ERROR_302, errorMessage, null);
	}
}
