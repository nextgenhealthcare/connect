package com.webreach.mirth.server.controllers;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.mule.umo.UMOComponent;
import org.mule.umo.provider.UMOConnector;

import com.webreach.mirth.model.ExtensionPoint;
import com.webreach.mirth.model.ExtensionPointDefinition;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.plugins.ConnectorStatusPlugin;

public class MonitoringController {
	public enum Status {CONNECTED, DISCONNECTED, WAITING_FOR_CONNECTION, IDLE, WAITING, POLLING, PROCESSING, RETRYING};
	public enum Priority {VERY_LOw, LOW, NORMAL, HIGH, VERY_HIGH, CRITICAL};
	private static MonitoringController instance = null;
	private Logger logger = Logger.getLogger(this.getClass());
	private Map<String, ConnectorStatusPlugin> loadedPlugins;
	private MonitoringController() {
		initPlugins();
	}
	
	public static MonitoringController getInstance() {
		synchronized (MonitoringController.class) {
			if (instance == null) {
				instance = new MonitoringController();
			}
			return instance;
		}
	}   	
	
	public void updateStatus(String connectorName, Status status, Priority priority, String connectionId){
		for(ConnectorStatusPlugin plugin : loadedPlugins.values()){
			plugin.updateStatus(connectorName, status, priority, connectionId);
		}
	}
	public void updateStatus(UMOConnector connector, Status status){
		updateStatus(connector.getName(), status, Priority.NORMAL, null);
	}
	public void updateStatus(UMOConnector connector, Status status, Priority priority){
		updateStatus(connector.getName(), status, priority, null);
	}
	public void updateStatus(UMOConnector connector, Status status, Priority priority, String connectionId){
		updateStatus(connector.getName(), status, priority, connectionId);
	}
	//Extension point for ExtensionPoint.Type.SERVER_PLUGIN
    @ExtensionPointDefinition(mode=ExtensionPoint.Mode.SERVER, type=ExtensionPoint.Type.SERVER_CONNECTOR_STATUS)
    public void initPlugins()
    {
        loadedPlugins = new HashMap<String, ConnectorStatusPlugin>();
        try
        {
            Map<String, PluginMetaData> plugins = ExtensionController.getInstance().getPluginMetaData();
            for (PluginMetaData metaData : plugins.values())
            {
            	if (metaData.isEnabled()){
	            	for (ExtensionPoint extensionPoint : metaData.getExtensionPoints()){
	            		try{
			                if(extensionPoint.getMode().equals(ExtensionPoint.Mode.SERVER) && extensionPoint.getType().equals(ExtensionPoint.Type.SERVER_CONNECTOR_STATUS) && extensionPoint.getClassName() != null && extensionPoint.getClassName().length() > 0)
			                {
		                        String pluginName = extensionPoint.getName();
		                        ConnectorStatusPlugin statusPlugin = (ConnectorStatusPlugin) Class.forName(extensionPoint.getClassName()).getDeclaredConstructors()[0].newInstance(new Object[]{});
		                        loadedPlugins.put(pluginName, statusPlugin);
			                }
	            		} 
	            		catch (Exception e)
	            		{
	            			logger.error(e);
	            		}
	                }
	        	}
	           
            }
        }
        catch (Exception e)
        {
        	logger.error(e);
        }
    }
}
