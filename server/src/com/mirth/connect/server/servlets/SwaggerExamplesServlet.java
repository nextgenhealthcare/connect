package com.mirth.connect.server.servlets;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.ServerEvent.Level;
import com.mirth.connect.model.ServerEvent.Outcome;
import com.mirth.connect.model.alert.AlertActionGroup;
import com.mirth.connect.model.alert.AlertInfo;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.model.alert.DefaultTrigger;
import com.mirth.connect.model.converters.ObjectJSONSerializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.plugins.dashboardstatus.ConnectionLogItem;
import com.mirth.connect.plugins.serverlog.ServerLogItem;

public class SwaggerExamplesServlet extends HttpServlet {
	
	private Calendar dateNow;
	private Calendar dateTomorrow;
	private SimpleDateFormat dateFormat;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		System.out.println("Init");
		super.init(servletConfig);
		dateNow = Calendar.getInstance();
		dateTomorrow = Calendar.getInstance();
		dateTomorrow.add(Calendar.DAY_OF_MONTH, 1);
		dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
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
		} else if (exampleRequested.equals("calendar")) {
            requestedObject = getCalendarExample();
        } else if (exampleRequested.equals("channel")) {
			requestedObject = getChannelExample();
		} else if (exampleRequested.equals("channel_header_map")) {
			requestedObject = getChannelHeaderMapExample();
		} else if (exampleRequested.equals("channel_list")) {
			requestedObject = getChannelListExample();
		} else if (exampleRequested.equals("connection_log_item_linked_list")) {
		    requestedObject = getConnectionLogItemLinkedListExample();
		} else if (exampleRequested.equals("connector_name_map")) {
			requestedObject = getConnectorNameMapExample();
		} else if (exampleRequested.equals("channel_summary_list")) {
			requestedObject = getChannelSummaryListExample();
		} else if (exampleRequested.equals("dashboard_channel_state_map")) {
		    requestedObject = getDashboardChannelStateMapExample();
		} else if (exampleRequested.equals("dashboard_connector_state_map")) {
		    requestedObject = getDashboardConnectorStateMapExample();
		} else if (exampleRequested.equals("data_pruner_status_map")) {
		    requestedObject = getDataPrunerStatusMapExample();
		} else if (exampleRequested.equals("event_filter")) {
            requestedObject = getEventFilterExample();
        } else if (exampleRequested.equals("guid_to_name_map")) {
			requestedObject = getGuidToNameMapExample();
		} else if (exampleRequested.equals("guid_set")) {
			requestedObject = getGuidSetExample();
		} else if (exampleRequested.equals("library_list")) {
            requestedObject = getLibraryListExample();
        } else if (exampleRequested.equals("metadatacolumn_list")) {
			requestedObject = getMetaDataColumnListExample();
		} else if (exampleRequested.equals("server_event")) {
		    requestedObject = getServerEventExample();
		} else if (exampleRequested.equals("server_event_list")) {
            requestedObject = getServerEventListExample();
        } else if (exampleRequested.equals("server_log_item_list")) {
            requestedObject = getServerLogItemListExample();
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
	
	private Calendar getCalendarExample() {
	    return dateNow;
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
	
	private List<ChannelSummary> getChannelSummaryListExample() {
		List<ChannelSummary> channelSummaries = new ArrayList<>();
		ChannelSummary channelSummary = new ChannelSummary(UUID.randomUUID().toString());
		channelSummary.setChannelStatus(getChannelStatusExample());
		channelSummaries.add(channelSummary);
		return channelSummaries;
	}
	
	private ChannelStatus getChannelStatusExample() {
		ChannelStatus channelStatus = new ChannelStatus();
		channelStatus.setLocalChannelId(1L);
		channelStatus.setDeployedRevisionDelta(0);
		channelStatus.setDeployedDate(dateNow);
		return channelStatus;
	}
	
	private ConnectionLogItem getConnectionLogItemExample() {
	    ConnectionLogItem logItem = new ConnectionLogItem(1L, UUID.randomUUID().toString(), UUID.randomUUID().toString(), 0L, dateFormat.format(dateNow.getTime()), "Channel 1", "Source: Channel Reader (HL7V2 -> JSON)", "Idle", "");
	    return logItem;
	}
	
	private LinkedList<ConnectionLogItem> getConnectionLogItemLinkedListExample() {
	    LinkedList<ConnectionLogItem> logItems = new LinkedList<>();
	    logItems.add(getConnectionLogItemExample());
	    return logItems;
	}
	
	private Map<Integer, String> getConnectorNameMapExample() {
		Map<Integer, String> connectorNameMap = new LinkedHashMap<>();
		connectorNameMap.put(0, "Source");
		connectorNameMap.put(1, "Destination 1");
		connectorNameMap.put(2, "Destination 2");
		return connectorNameMap;
	}
	
	private Map<String, String> getDashboardChannelStateMapExample() {
        Map<String, String> stateMap = new HashMap<>();
        stateMap.put(UUID.randomUUID().toString(), "Idle");
        return stateMap;
    }
	
	private Map<String, Object[]> getDashboardConnectorStateMapExample() {
	    Map<String, Object[]> stateMap = new HashMap<>();
	    String channelId = UUID.randomUUID().toString();
	    Object[] states = new Object[2];
	    states[0] = new Color(255, 255, 0, 255);
	    states[1] = "Idle";
	    stateMap.put(channelId + "_0", states);
	    stateMap.put(channelId + "_1", states);
	    return stateMap;
	}
	
	private Map<String, String> getDataPrunerStatusMapExample() {
	    Map<String, String> statusMap = new HashMap<>();
	    statusMap.put("lastProcess", "-");
	    statusMap.put("isRunning", "false");
	    statusMap.put("currentState", "Not running");
	    statusMap.put("nextProcess", "Scheduled Monday, Jan 1, 1:00:00 AM");
	    statusMap.put("currentProcess", "-");
	    return statusMap;
	}
	
	private EventFilter getEventFilterExample() {
	    EventFilter eventFilter = new EventFilter();
	    eventFilter.setMaxEventId(2);
	    eventFilter.setMinEventId(1);
	    eventFilter.setId(1);
	    Set<Level> levels = new HashSet<>();
	    levels.add(Level.INFORMATION);
	    eventFilter.setLevels(levels);
	    eventFilter.setStartDate(dateNow);
	    eventFilter.setEndDate(dateTomorrow);
	    eventFilter.setOutcome(Outcome.SUCCESS);
	    eventFilter.setUserId(1);
	    eventFilter.setIpAddress("0:0:0:0:0:0:0:1");
	    eventFilter.setServerId(UUID.randomUUID().toString());
	    return eventFilter;
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
	
	private List<String> getLibraryListExample() {
	    List<String> libraryList = new ArrayList<>();
	    libraryList.add("library1.jar");
	    libraryList.add("library2.jar");
	    return libraryList;
	}
	
	private List<MetaDataColumn> getMetaDataColumnListExample() {
		List<MetaDataColumn> metaDataColumns = new ArrayList<>();
		metaDataColumns.add(new MetaDataColumn("SOURCE", MetaDataColumnType.STRING, "mirth_source"));
		metaDataColumns.add(new MetaDataColumn("TYPE", MetaDataColumnType.STRING, "mirth_type"));
		return metaDataColumns;
	}
	
	private ServerEvent getServerEventExample() {
	    ServerEvent serverEvent = new ServerEvent();
	    serverEvent.setName("Name 1");
	    serverEvent.addAttribute("key", "value");
	    serverEvent.setIpAddress("0:0:0:0:0:0:0:1");
	    serverEvent.setServerId(UUID.randomUUID().toString());
	    return serverEvent;
	}
	
	private List<ServerEvent> getServerEventListExample() {
	    List<ServerEvent> serverEventList = new ArrayList<>();
	    serverEventList.add(getServerEventExample());
	    return serverEventList;
	}
	
	private ServerLogItem getServerLogItemExample() {
	    return new ServerLogItem(UUID.randomUUID().toString(), 1L, "INFO", dateNow.getTime(), "Main Server Thread", "com.mirth.connect.server.Mirth", "1", "Example message", "Example throwable information");
	}
	
	private List<ServerLogItem> getServerLogItemListExample() {
	    List<ServerLogItem> serverLogList = new ArrayList();
	    serverLogList.add(getServerLogItemExample());
	    return serverLogList;
	}
}
