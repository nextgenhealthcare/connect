package com.webreach.mirth.server.controllers;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;
import org.mule.umo.provider.UMOConnector;

import com.webreach.mirth.model.ExtensionPoint;
import com.webreach.mirth.model.ExtensionPointDefinition;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.plugins.ConnectorStatusPlugin;

public class MonitoringController {
	public enum Event {
		CONNECTED, DISCONNECTED, INITIALIZED, BUSY, DONE, ATTEMPTING_TO_CONNECT
	};

	public enum ConnectorType {
		LISTENER, SENDER, READER, WRITER
	};

	private static MonitoringController instance = null;
	private Logger logger = Logger.getLogger(this.getClass());
	private Map<String, ConnectorStatusPlugin> loadedPlugins;

	private MonitoringController() {

	}

	public static MonitoringController getInstance() {
		synchronized (MonitoringController.class) {
			if (instance == null) {
				instance = new MonitoringController();
			}
			return instance;
		}
	}

	public void initialize() {
		initPlugins();
	}

	public void updateStatus(String connectorName, ConnectorType type, Event event, Socket socket) {
		for (ConnectorStatusPlugin plugin : loadedPlugins.values()) {
			try {
				plugin.updateStatus(connectorName, type, event, socket);
			} catch (Exception e) {
				logger.error(e);
			}
		}
	}

	public void updateStatus(UMOConnector connector, ConnectorType type, Event event) {
		updateStatus(connector.getName(), type, event, null);
	}

	public void updateStatus(UMOConnector connector, ConnectorType type, Event event, Socket socket) {
		updateStatus(connector.getName(), type, event, socket);
	}

	// Extension point for ExtensionPoint.Type.SERVER_PLUGIN
	@ExtensionPointDefinition(mode = ExtensionPoint.Mode.SERVER, type = ExtensionPoint.Type.SERVER_CONNECTOR_STATUS)
	public void initPlugins() {
		loadedPlugins = new HashMap<String, ConnectorStatusPlugin>();
		try {
			Map<String, PluginMetaData> plugins = ExtensionController.getInstance().getPluginMetaData();
			for (PluginMetaData metaData : plugins.values()) {
				if (metaData.isEnabled()) {
					for (ExtensionPoint extensionPoint : metaData.getExtensionPoints()) {
						try {
							if (extensionPoint.getMode().equals(ExtensionPoint.Mode.SERVER) && extensionPoint.getType().equals(ExtensionPoint.Type.SERVER_CONNECTOR_STATUS) && extensionPoint.getClassName() != null && extensionPoint.getClassName().length() > 0) {
                                String pluginName = extensionPoint.getName();
                                Class clazz = Class.forName(extensionPoint.getClassName());
                                Constructor[] constructors = clazz.getDeclaredConstructors();
                                for (int i=0; i < constructors.length; i++) {
                                    Class parameters[];
                                    parameters = constructors[i].getParameterTypes();
                                    // load plugin if the number of parameters is 0.
                                    if (parameters.length == 0) {
                                        ConnectorStatusPlugin statusPlugin = (ConnectorStatusPlugin) constructors[i].newInstance(new Object[] {});
                                        loadedPlugins.put(pluginName, statusPlugin);
                                        i = constructors.length;
                                    }
                                }
							}
						} catch (Exception e) {
							logger.error(e);
						}
					}
				}

			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
