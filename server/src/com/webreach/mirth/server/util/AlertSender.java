package com.webreach.mirth.server.util;

import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;

public class AlertSender {
	private AlertController alertController = new AlertController();
	private String channelId;
	
	public AlertSender(String channelId) {
		this.channelId = channelId;
	}
	
	public void sendAlert(String errorMessage) {
		alertController.sendAlerts(channelId, errorMessage);
	}
}
