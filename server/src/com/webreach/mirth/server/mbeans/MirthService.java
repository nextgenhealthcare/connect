package com.webreach.mirth.server.mbeans;

import java.util.Properties;

import com.webreach.mirth.server.Command;
import com.webreach.mirth.server.CommandQueue;
import com.webreach.mirth.server.Mirth;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.ExtensionController;

public class MirthService implements MirthServiceMBean {
	private Mirth mirthServer = null;
	private final String EXTENSION_NAME = "Extension Manager";
	
	public void start()	{		
		if (mirthServer == null || !mirthServer.isAlive()) {
			createProperties();
			mirthServer = new Mirth();
			mirthServer.start();
		} 
	}

	public void stop() {
		CommandQueue queue = CommandQueue.getInstance();
		queue.addCommand(new Command(Command.Operation.SHUTDOWN_SERVER));
	}
	
	private void createProperties() {
		ExtensionController extensionController = ControllerFactory.getFactory().createExtensionController();
		Properties props = null;
		
		try {
			props = extensionController.getPluginProperties(EXTENSION_NAME);
		} catch (ControllerException e1) {
		}
		if (props == null) {
			props = new Properties();
		}
		props.setProperty("disableInstall", "true");
		
		try {
			extensionController.setPluginProperties(EXTENSION_NAME, props);
		} catch (ControllerException e) {
		}
	}
}
