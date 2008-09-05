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
