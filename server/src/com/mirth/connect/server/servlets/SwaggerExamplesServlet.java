package com.mirth.connect.server.servlets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirth.connect.connectors.vm.VmDispatcherProperties;
import com.mirth.connect.connectors.vm.VmReceiverProperties;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.DashboardChannelInfo;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DashboardStatus.StatusType;
import com.mirth.connect.model.alert.AlertActionGroup;
import com.mirth.connect.model.alert.AlertInfo;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.model.alert.DefaultTrigger;
import com.mirth.connect.model.converters.ObjectJSONSerializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;

public class SwaggerExamplesServlet extends HttpServlet {
	
	private Calendar dateNow;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		System.out.println("Init");
		super.init(servletConfig);
		dateNow = Calendar.getInstance();
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String exampleRequested = "";
		try {
			 exampleRequested = req.getPathInfo().substring(1, req.getPathInfo().lastIndexOf("_"));
		} catch (Exception e) {
			resp.setContentType("text/plain");
			resp.getWriter().write("No Example Found");
			return;
		}
		
		Object requestedObject = null;
		
		if (exampleRequested.equals("alert")) {
			requestedObject = getAlertExample();
		} else if (exampleRequested.equals("alert_info")) {
			requestedObject = getAlertInfoExample();
		} else if (exampleRequested.equals("alert_list")) {
			requestedObject = getAlertListExample();
		} else if (exampleRequested.equals("alert_protocol_options")) {
			requestedObject = getAlertProtocolOptions();
		} else if (exampleRequested.equals("alert_status_list")) {
			requestedObject = getAlertStatusListExample();
		} else if (exampleRequested.equals("channel")) {
			requestedObject = getChannelExample();
		} else if (exampleRequested.equals("channel_header_map")) {
			requestedObject = getChannelHeaderMapExample();
		} else if (exampleRequested.equals("channel_list")) {
			requestedObject = getChannelListExample();
		} else if (exampleRequested.equals("channel_group_list")) {
		    requestedObject = getChannelGroupListExample();
		} else if (exampleRequested.equals("channel_statistics")) {
		    requestedObject = getChannelStatisticsExample();
		} else if (exampleRequested.equals("channel_statistics_list")) {
		    requestedObject = getChannelStatisticsListExample();
		} else if (exampleRequested.equals("connector_map")) {
		    requestedObject = getConnectorMap(true);
		} else if (exampleRequested.equals("start_connector_map")) {
		    requestedObject = getConnectorMap(false);
		} else if (exampleRequested.equals("connector_name_map")) {
			requestedObject = getConnectorNameMapExample();
		} else if (exampleRequested.equals("channel_summary_list")) {
			requestedObject = getChannelSummaryListExample();
		} else if (exampleRequested.equals("dashboard_channel_info")) {
		    requestedObject = getDashboardChannelInfoExample();
		} else if (exampleRequested.equals("dashboard_status")) {
		    requestedObject = getDashboardStatusExample();
		} else if (exampleRequested.equals("dashboard_status_list")) {
		    requestedObject = getDashboardStatusListExample();
		} else if (exampleRequested.equals("guid_to_name_map")) {
			requestedObject = getGuidToNameMapExample();
		} else if (exampleRequested.equals("guid_set")) {
			requestedObject = getGuidSetExample();
		} else if (exampleRequested.equals("metadatacolumn_list")) {
			requestedObject = getMetaDataColumnListExample();
		}
		
		if (req.getPathInfo().endsWith("_json")) {
			resp.setContentType("application/json");
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ObjectJSONSerializer.getInstance().serialize(requestedObject, baos);
	        
	        String serializedObject = baos.toString("UTF-8");
	        String returnString = "{\"summary\": \"" + exampleRequested + "\", \"value\": " + serializedObject + "}";
	        resp.getWriter().write(returnString);
		} else if (req.getPathInfo().endsWith("_xml")) {
			resp.setContentType("application/json");
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(baos, "UTF-8");
	        ObjectXMLSerializer.getInstance().serialize(requestedObject, writer);
	        
	        String serializedObject = baos.toString("UTF-8");
	        
	        Map<String, Object> params = new HashMap<>();
	        params.put("summary", exampleRequested);
	        params.put("value", serializedObject);
	        String oasExample = new ObjectMapper().writeValueAsString(params);
	        resp.getWriter().write(oasExample);
		}
	}
	
	private AlertModel getAlertExample() {
		DefaultTrigger trigger = new DefaultTrigger();
		AlertActionGroup group = new AlertActionGroup();
		AlertModel alert = new AlertModel(trigger, group);
		alert.setName("Alert Name");
		return alert;
	}
	
	private AlertInfo getAlertInfoExample() {
		AlertInfo info = new AlertInfo();
		info.setChangedChannels(getChannelSummaryListExample());
		info.setModel(getAlertExample());
		info.setProtocolOptions(getAlertProtocolOptions());
		return info;
	}
	
	private Map<String, Map<String, String>> getAlertProtocolOptions() {
		Map<String, Map<String, String>> protocolOptions = new LinkedHashMap<>();
		Map<String, String> protocolEntry = new HashMap<>();
		protocolEntry.put(UUID.randomUUID().toString(), "Channel Name");
		protocolOptions.put("Channel", protocolEntry);
		return protocolOptions;
	}
	
	private List<AlertModel> getAlertListExample() {
		List<AlertModel> alertList = new ArrayList<>();
		alertList.add(getAlertExample());
		return alertList;
	}
	
	private List<AlertStatus> getAlertStatusListExample() {
		AlertStatus status = new AlertStatus();
		status.setId(UUID.randomUUID().toString());
		status.setName("Alert 1");
		List<AlertStatus> list = new ArrayList<>();
		list.add(status);
		return list;
	}
	
	private Channel getChannelExample() {
		Channel channel = new Channel();
		channel.setId(UUID.randomUUID().toString());
		Connector sourceConnector = new Connector();
		sourceConnector.setProperties(new VmReceiverProperties());
		Connector destinationConnector = new Connector();
		destinationConnector.setProperties(new VmDispatcherProperties());
		channel.setSourceConnector(sourceConnector);
		channel.addDestination(destinationConnector);
		return channel;
	}
	
	private Map<String, ChannelHeader> getChannelHeaderMapExample() {
		Map<String, ChannelHeader> channelHeaders = new HashMap<>();
		channelHeaders.put(UUID.randomUUID().toString(), new ChannelHeader(0, dateNow, false));
		return channelHeaders;
	}
	
	private List<Channel> getChannelListExample() {
		List<Channel> channelList = new ArrayList<>();
		channelList.add(getChannelExample());
		return channelList;
	}
	
	private List<Channel> getMinimalChannelListExample() {
	    List<Channel> channelList = new ArrayList<>();
	    channelList.add(getMinimalChannelExample());
	    return channelList;
	}
	
	private Channel getMinimalChannelExample() {
	    Channel channel = new Channel(UUID.randomUUID().toString());
	    return channel;
	}
	
	private List<ChannelGroup> getChannelGroupListExample() {
	    List<ChannelGroup> groupList = new ArrayList<>();
	    ChannelGroup group = new ChannelGroup("Group Name", "Group Description");
	    group.setChannels(getMinimalChannelListExample());
	    groupList.add(group);
	    return groupList;
	}
	
	private List<ChannelSummary> getChannelSummaryListExample() {
		List<ChannelSummary> channelSummaries = new ArrayList<>();
		ChannelSummary channelSummary = new ChannelSummary(UUID.randomUUID().toString());
		channelSummary.setChannelStatus(getChannelStatusExample());
		channelSummaries.add(channelSummary);
		return channelSummaries;
	}
	
	private ChannelStatistics getChannelStatisticsExample() {
	    ChannelStatistics stats = new ChannelStatistics();
        stats.setServerId(UUID.randomUUID().toString());
        stats.setChannelId(UUID.randomUUID().toString());
        return stats;
	}
	
	private List<ChannelStatistics> getChannelStatisticsListExample() {
	    List<ChannelStatistics> channelStatisticsList = new ArrayList<>();
	    channelStatisticsList.add(getChannelStatisticsExample());
	    return channelStatisticsList;
	}
	
	private ChannelStatus getChannelStatusExample() {
		ChannelStatus channelStatus = new ChannelStatus();
		channelStatus.setLocalChannelId(1L);
		channelStatus.setDeployedRevisionDelta(0);
		channelStatus.setDeployedDate(dateNow);
		return channelStatus;
	}
	
	private Map<String, List<Integer>> getConnectorMap(boolean includeNull) {
	    Map<String, List<Integer>> connectorMap = new HashMap<>();
	    List<Integer> connectorList = new ArrayList<>();
	    if (includeNull) {
	        connectorList.add(null); // channel stats
	    }
	    connectorList.add(0); // source connector stats
	    connectorList.add(1); // destination 1 connector stats
	    connectorMap.put(UUID.randomUUID().toString(), connectorList);
	    return connectorMap;
	}
	
	private Map<Integer, String> getConnectorNameMapExample() {
		Map<Integer, String> connectorNameMap = new LinkedHashMap<>();
		connectorNameMap.put(0, "Source");
		connectorNameMap.put(1, "Destination 1");
		connectorNameMap.put(2, "Destination 2");
		return connectorNameMap;
	}
	
	private DashboardChannelInfo getDashboardChannelInfoExample() {
	    DashboardChannelInfo dashboardChannelInfo = new DashboardChannelInfo(getDashboardStatusListExample(), getGuidSetExample(), 0);
	    return dashboardChannelInfo;
	}
	
	private DashboardStatus getDashboardStatusExample() {
	    DashboardStatus status = new DashboardStatus();

	    Map<Status, Long> statistics = new LinkedHashMap<>();
	    for (Status s: Status.values()) {
	        statistics.put(s, 0L);
	    }
	    
	    status.setChannelId(UUID.randomUUID().toString());
	    status.setDeployedDate(dateNow);
	    status.setDeployedRevisionDelta(0);
	    status.setLifetimeStatistics(statistics);
	    status.setMetaDataId(0);
	    status.setName("Channel Name");
	    status.setQueueEnabled(false);
	    status.setQueued(0L);
	    status.setState(DeployedState.STARTED);
	    status.setStatistics(statistics);
	    status.setStatusType(StatusType.CHANNEL);
	    status.setWaitForPrevious(false);
	    return status;
	}
	
	private List<DashboardStatus> getDashboardStatusListExample() {
	    List<DashboardStatus> statusList = new ArrayList<>();
	    statusList.add(getDashboardStatusExample());
	    return statusList;
	}
	
	private Set<String> getGuidSetExample() {
		Set<String> stringSet = new HashSet<>();
		stringSet.add(UUID.randomUUID().toString());
		stringSet.add(UUID.randomUUID().toString());
		return stringSet;
	}
	
	private Map<String, String> getGuidToNameMapExample() {
		Map<String, String> guidToNameMap = new HashMap<>();
		guidToNameMap.put(UUID.randomUUID().toString(), "Name 1");
		guidToNameMap.put(UUID.randomUUID().toString(), "Name 2");
		return guidToNameMap;
	}
	
	private List<MetaDataColumn> getMetaDataColumnListExample() {
		List<MetaDataColumn> metaDataColumns = new ArrayList<>();
		metaDataColumns.add(new MetaDataColumn("SOURCE", MetaDataColumnType.STRING, "mirth_source"));
		metaDataColumns.add(new MetaDataColumn("TYPE", MetaDataColumnType.STRING, "mirth_type"));
		return metaDataColumns;
	}
}
