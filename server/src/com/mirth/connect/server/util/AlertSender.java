/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import com.mirth.connect.server.ErrorConstants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;

public class AlertSender {
	private AlertController alertController = ControllerFactory.getFactory().createAlertController();
	private String channelId;
	
	public AlertSender(String channelId) {
		this.channelId = channelId;
	}
	
	public void sendAlert(String errorMessage) {
		alertController.sendAlerts(channelId, ErrorConstants.ERROR_302, errorMessage, null);
	}
}
