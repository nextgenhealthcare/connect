package com.mirth.connect.client.ui.browsers.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;

public class MessageBrowserChannelModel {
	private String channelId;
	private String channelName;
	private Map<Integer, String> connectors;
	private List<MetaDataColumn> metaDataColumns;
	private List<Integer> selectedMetaDataIds;
	private boolean channelDeployed;
	
	public MessageBrowserChannelModel(String channelId) {
		this.channelId = channelId;
		selectedMetaDataIds = new ArrayList<>();
		channelDeployed = false;
	}
	
	public MessageBrowserChannelModel(String channelId, String channelName, Map<Integer, String> connectors,
			List<MetaDataColumn> metaDataColumns, List<Integer> selectedMetaDataIds, boolean channelDeployed) {
		this.channelId = channelId;
		this.channelName = channelName;
		this.connectors = connectors;
		this.metaDataColumns = metaDataColumns;
		this.selectedMetaDataIds = selectedMetaDataIds;
		this.channelDeployed = channelDeployed;
	}

	public String getChannelId() {
		return channelId;
	}
	
	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}
	
	public String getChannelName() {
		return channelName;
	}
	
	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
	
	public Map<Integer, String> getConnectors() {
		return connectors;
	}
	
	public void setConnectors(Map<Integer, String> connectors) {
		this.connectors = connectors;
	}
	
	public List<MetaDataColumn> getMetaDataColumns() {
		return metaDataColumns;
	}
	
	public void setMetaDataColumns(List<MetaDataColumn> metaDataColumns) {
		this.metaDataColumns = metaDataColumns;
	}
	
	public List<Integer> getSelectedMetaDataIds() {
		return selectedMetaDataIds;
	}
	
	public void setSelectedMetaDataIds(List<Integer> selectedMetaDataIds) {
		this.selectedMetaDataIds = selectedMetaDataIds;
	}
	
	public boolean isChannelDeployed() {
		return channelDeployed;
	}
	
	public void setChannelDeployed(boolean channelDeployed) {
		this.channelDeployed = channelDeployed;
	}
}
