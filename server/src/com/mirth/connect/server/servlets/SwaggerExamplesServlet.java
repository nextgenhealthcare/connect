package com.mirth.connect.server.servlets;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirth.connect.client.core.Version;
import com.mirth.connect.connectors.file.FileDispatcherProperties;
import com.mirth.connect.connectors.file.FileReceiverProperties;
import com.mirth.connect.connectors.http.HttpDispatcherProperties;
import com.mirth.connect.connectors.jdbc.Column;
import com.mirth.connect.connectors.jdbc.Table;
import com.mirth.connect.connectors.jms.JmsConnectorProperties;
import com.mirth.connect.connectors.smtp.SmtpDispatcherProperties;
import com.mirth.connect.connectors.tcp.TcpDispatcherProperties;
import com.mirth.connect.connectors.vm.VmDispatcherProperties;
import com.mirth.connect.connectors.vm.VmReceiverProperties;
import com.mirth.connect.connectors.ws.DefinitionServiceMap;
import com.mirth.connect.connectors.ws.DefinitionServiceMap.DefinitionPortMap;
import com.mirth.connect.connectors.ws.DefinitionServiceMap.PortInformation;
import com.mirth.connect.connectors.ws.WebServiceDispatcherProperties;
import com.mirth.connect.donkey.model.channel.DeployedState;
import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.model.ApiProvider;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ChannelGroup;
import com.mirth.connect.model.ChannelHeader;
import com.mirth.connect.model.ChannelStatistics;
import com.mirth.connect.model.ChannelStatus;
import com.mirth.connect.model.ChannelSummary;
import com.mirth.connect.model.Connector;
import com.mirth.connect.model.ConnectorMetaData;
import com.mirth.connect.model.DashboardChannelInfo;
import com.mirth.connect.model.DashboardStatus;
import com.mirth.connect.model.DashboardStatus.StatusType;
import com.mirth.connect.model.ExtensionLibrary;
import com.mirth.connect.model.MessageImportResult;
import com.mirth.connect.model.MetaData;
import com.mirth.connect.model.PluginClass;
import com.mirth.connect.model.PluginMetaData;
import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.model.ServerEvent.Level;
import com.mirth.connect.model.ServerEvent.Outcome;
import com.mirth.connect.model.SystemInfo;
import com.mirth.connect.model.SystemStats;
import com.mirth.connect.model.alert.AlertActionGroup;
import com.mirth.connect.model.alert.AlertInfo;
import com.mirth.connect.model.alert.AlertModel;
import com.mirth.connect.model.alert.AlertStatus;
import com.mirth.connect.model.alert.DefaultTrigger;
import com.mirth.connect.model.codetemplates.CodeTemplate;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrary;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrarySaveResult;
import com.mirth.connect.model.codetemplates.CodeTemplateSummary;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrarySaveResult.CodeTemplateUpdateResult;
import com.mirth.connect.model.codetemplates.CodeTemplateLibrarySaveResult.LibraryUpdateResult;
import com.mirth.connect.model.converters.ObjectJSONSerializer;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.model.filters.EventFilter;
import com.mirth.connect.model.filters.MessageFilter;
import com.mirth.connect.model.filters.elements.ContentSearchElement;
import com.mirth.connect.model.filters.elements.MetaDataSearchElement;
import com.mirth.connect.model.purged.PurgedDocument;
import com.mirth.connect.plugins.dashboardstatus.ConnectionLogItem;
import com.mirth.connect.plugins.serverlog.ServerLogItem;
import com.mirth.connect.util.ConnectionTestResponse;

public class SwaggerExamplesServlet extends HttpServlet {
	
	private Calendar dateNow;
	private Calendar dateTomorrow;
	private SimpleDateFormat dateFormat;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
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
		} else if (exampleRequested.equals("attachment")) {
            requestedObject = getAttachmentExample();
        } else if (exampleRequested.equals("attachment_list")) {
            requestedObject = getAttachmentListExample();
        } else if (exampleRequested.equals("boolean")) {
            requestedObject = new Boolean(true);
        } else if (exampleRequested.equals("calendar")) {
            requestedObject = getCalendarExample();
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
		} else if (exampleRequested.equals("code_template")) {
		    requestedObject = getCodeTemplateExample(true);
		} else if (exampleRequested.equals("code_template_list")) {
		    requestedObject = getCodeTemplateListExample(true);
		} else if (exampleRequested.equals("code_template_library")) {
		    requestedObject = getCodeTemplateLibraryExample(false);
		} else if (exampleRequested.equals("code_template_library_full_templates")) {
            requestedObject = getCodeTemplateLibraryExample(true);
		} else if (exampleRequested.equals("code_template_library_list")) {
            requestedObject = getCodeTemplateLibraryListExample(false);
		} else if (exampleRequested.equals("code_template_library_list_full_templates")) {
            requestedObject = getCodeTemplateLibraryListExample(true);
		} else if (exampleRequested.equals("code_template_library_saved_result")) {
		    requestedObject = getCodeTemplateLibrarySavedResultExample();
		} else if (exampleRequested.equals("code_template_summary_list_revision_changed")) {
		    requestedObject = getCodeTemplateSummaryListExample(true);
		} else if (exampleRequested.equals("code_template_summary_list")) {
            requestedObject = getCodeTemplateSummaryListExample(false);
		} else if (exampleRequested.equals("connector_map")) {
		    requestedObject = getConnectorMapExample(true);
		} else if (exampleRequested.equals("connector_message")) {
            requestedObject = getConnectorMessageExample();
        } else if (exampleRequested.equals("connector_metadata")) {
            requestedObject = getConnectorMetaDataExample();
        } else if (exampleRequested.equals("connector_metadata_map")) {
            requestedObject = getConnectorMetaDataMapExample();
        } else if (exampleRequested.equals("start_connector_map")) {
		    requestedObject = getConnectorMapExample(false);
		} else if (exampleRequested.equals("connection_log_item_linked_list")) {
		    requestedObject = getConnectionLogItemLinkedListExample();
		} else if (exampleRequested.equals("connection_test_response_file")) {
		    requestedObject = getFileConnectionTestResponseExample();
		} else if (exampleRequested.equals("connection_test_response_http")) {
		    requestedObject = getHttpConnectionTestResponseExample();
		} else if (exampleRequested.equals("connection_test_response_smtp")) {
		    requestedObject = getSmtpConnectionTestResponseExample();
		} else if (exampleRequested.equals("connection_test_response_tcp")) {
		    requestedObject = getTcpConnectionTestResponseExample();
		} else if (exampleRequested.equals("connection_test_response_ws")) {
            requestedObject = getWsConnectionTestResponseExample();
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
		} else if (exampleRequested.equals("dashboard_channel_state_map")) {
		    requestedObject = getDashboardChannelStateMapExample();
		} else if (exampleRequested.equals("dashboard_connector_state_map")) {
		    requestedObject = getDashboardConnectorStateMapExample();
		} else if (exampleRequested.equals("data_pruner_status_map")) {
		    requestedObject = getDataPrunerStatusMapExample();
		} else if (exampleRequested.equals("definition_service_map")) {
		    requestedObject = getDefinitionServiceMapExample();
		} else if (exampleRequested.equals("event_filter")) {
            requestedObject = getEventFilterExample();
		} else if (exampleRequested.equals("file_dispatcher_properties")) {
		    requestedObject = getFileDispatcherPropertiesExample();
		} else if (exampleRequested.equals("file_receiver_properties")) {
		    requestedObject = getFileReceiverPropertiesExample();
		} else if (exampleRequested.equals("generate_envelope")) {
		    requestedObject = getGenerateEnvelopeExample();
        } else if (exampleRequested.equals("generic_map")) {
            requestedObject = getGenericMapExample();
        } else if (exampleRequested.equals("global_map")) {
            requestedObject = getGlobalMapExample();
        } else if (exampleRequested.equals("global_maps")) {
            requestedObject = getGlobalMapsExample();
        } else if (exampleRequested.equals("guid_to_int_map")) {
            requestedObject = getGuidToIntMapExample();
        } else if (exampleRequested.equals("guid_to_name_map")) {
			requestedObject = getGuidToNameMapExample();
		} else if (exampleRequested.equals("guid_set")) {
			requestedObject = getGuidSetExample();
		} else if (exampleRequested.equals("http_dispatcher_properties")) {
		    requestedObject = getHttpDispatcherPropertiesExample();
		} else if (exampleRequested.equals("jms_template_name_set")) {
		    requestedObject = getJmsTemplateNameSetExample();
		} else if (exampleRequested.equals("jms_connector_properties")) {
		    requestedObject = getJmsConnectorPropertiesExample();
		} else if (exampleRequested.equals("jms_connector_properties_map")) {
            requestedObject = getJmsConnectorPropertiesMapExample();
		} else if (exampleRequested.equals("library_list")) {
            requestedObject = getLibraryListExample();
        } else if (exampleRequested.equals("long")) {
            requestedObject = getLongExample();
        } else if (exampleRequested.equals("message")) {
            requestedObject = getMessageExample();
        } else if (exampleRequested.equals("message_list")) {
            requestedObject = getMessageListExample();
        } else if (exampleRequested.equals("message_filter")) {
            requestedObject = getMessageFilterExample();
        } else if (exampleRequested.equals("message_import_result")) {
            requestedObject = getMessageImportResultExample();
        } else if (exampleRequested.equals("metadatacolumn_list")) {
			requestedObject = getMetaDataColumnListExample();
        } else if (exampleRequested.equals("null")) {
            requestedObject = null;
		} else if (exampleRequested.equals("plugin_metadata_map")) {
            requestedObject = getPluginMetaDataMapExample();
        } else if (exampleRequested.equals("properties")) {
            requestedObject = getPropertiesExample();
        } else if (exampleRequested.equals("purged_document")) {
            requestedObject = getPurgedDocumentExample();
        } else if (exampleRequested.equals("raw_message")) {
            requestedObject = getRawMessageExample();
        } else if (exampleRequested.equals("server_event")) {
		    requestedObject = getServerEventExample();
		} else if (exampleRequested.equals("server_event_list")) {
            requestedObject = getServerEventListExample();
        } else if (exampleRequested.equals("server_log_item_list")) {
            requestedObject = getServerLogItemListExample();
        } else if (exampleRequested.equals("smtp_dispatcher_properties")) {
            requestedObject = getSmtpDispatcherPropertiesExample("none");
        } else if (exampleRequested.equals("smtp_dispatcher_properties_ssl")) {
            requestedObject = getSmtpDispatcherPropertiesExample("SSL");
        } else if (exampleRequested.equals("smtp_dispatcher_properties_tls")) {
            requestedObject = getSmtpDispatcherPropertiesExample("TLS");
        } else if (exampleRequested.equals("system_info")) {
            requestedObject = getSystemInfoExample();
        } else if (exampleRequested.equals("system_stats")) {
            requestedObject = getSystemStatsExample();
        } else if (exampleRequested.equals("table_set")) {
            requestedObject = getTableSetExample();
        } else if (exampleRequested.equals("tcp_dispatcher_properties")) {
            requestedObject = getTcpDispatcherPropertiesExample();
        } else if (exampleRequested.equals("ws_dispatcher_properties")) {
            requestedObject = getWsDispatcherPropertiesExample();
        }
		
		resp.setContentType("application/json");
		if (req.getPathInfo().endsWith("_json")) {
	        String serializedObject = jsonSerialize(requestedObject);
	        String returnString = "{\"summary\": \"" + exampleRequested + "\", \"value\": " + serializedObject + "}";
	        resp.getWriter().write(returnString);
		} else if (req.getPathInfo().endsWith("_xml")) {
			String serializedObject = xmlSerialize(requestedObject);    
	        Map<String, Object> params = new HashMap<>();
	        params.put("summary", exampleRequested);
	        params.put("value", serializedObject);
	        String oasExample = new ObjectMapper().writeValueAsString(params);
	        resp.getWriter().write(oasExample);
		} else if (req.getPathInfo().endsWith("_txt")) {
		    String serializedObject = (String)requestedObject;
		    Map<String, Object> params = new HashMap<>();
            params.put("summary", exampleRequested);
            params.put("value", serializedObject);
            String oasExample = new ObjectMapper().writeValueAsString(params);
            resp.getWriter().write(oasExample);
		}
	}
	
	private String jsonSerialize(Object object) throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectJSONSerializer.getInstance().serialize(object, baos);
        return baos.toString("UTF-8");
	}
	
	private String xmlSerialize(Object object) throws UnsupportedEncodingException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, "UTF-8");
        ObjectXMLSerializer.getInstance().serialize(object, writer);
        return baos.toString("UTF-8");
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
	
	private Attachment getAttachmentExample() {
	    Attachment attachment = new Attachment("attachmentId", "Example content".getBytes(), MediaType.TEXT_PLAIN);
	    attachment.setEncrypted(false);
	    return attachment;
	}
	
	private List<Attachment> getAttachmentListExample() {
	    List<Attachment> attachments = new ArrayList<>();
	    attachments.add(getAttachmentExample());
	    return attachments;
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
	
	private CodeTemplate getCodeTemplateExample(boolean includeFullTemplates) {
	    if (includeFullTemplates) {
	        return CodeTemplate.getDefaultCodeTemplate("Template 1");
	    } else {
	        return new CodeTemplate(UUID.randomUUID().toString());
	    }
	}
	
	private List<CodeTemplate> getCodeTemplateListExample(boolean includeFullTemplates) {
	    List<CodeTemplate> list = new ArrayList<>();
	    list.add(getCodeTemplateExample(includeFullTemplates));
	    return list;
	}
	
	private CodeTemplateLibrary getCodeTemplateLibraryExample(boolean includeFullTemplates) {
	    CodeTemplateLibrary library = new CodeTemplateLibrary();
        library.setName("Library Name");
        library.setDescription("Library Description");
        library.setRevision(1);
        library.setLastModified(dateNow);
        Set<String> disabledChannelIds = new HashSet<>();
        disabledChannelIds.add(UUID.randomUUID().toString());
        library.setDisabledChannelIds(disabledChannelIds);
        library.setCodeTemplates(getCodeTemplateListExample(includeFullTemplates));
        return library;
	}
	
	private List<CodeTemplateLibrary> getCodeTemplateLibraryListExample(boolean includeFullTemplates) {
	    List<CodeTemplateLibrary> list = new ArrayList<>();
	    list.add(getCodeTemplateLibraryExample(includeFullTemplates));
	    return list;
	}
	
	private CodeTemplateLibrarySaveResult getCodeTemplateLibrarySavedResultExample() {
	    CodeTemplateLibrarySaveResult savedResult = new CodeTemplateLibrarySaveResult();
	    savedResult.setOverrideNeeded(true);
	    savedResult.setLibrariesSuccess(true);
	    
	    CodeTemplateUpdateResult templateUpdateResult = new CodeTemplateUpdateResult();
	    templateUpdateResult.setNewLastModified(dateNow);
	    templateUpdateResult.setNewRevision(2);
	    templateUpdateResult.setSuccess(true);
	    Map<String, CodeTemplateUpdateResult> codeTemplateResults = new HashMap<String, CodeTemplateUpdateResult>();
	    codeTemplateResults.put(UUID.randomUUID().toString(), templateUpdateResult);
	    savedResult.setCodeTemplateResults(codeTemplateResults);
	    
	    LibraryUpdateResult libraryUpdateResult = new LibraryUpdateResult();
	    libraryUpdateResult.setNewLastModified(dateNow);
	    libraryUpdateResult.setNewRevision(2);
	    Map<String, LibraryUpdateResult> libraryResults = new HashMap<String, LibraryUpdateResult>();
	    libraryResults.put(UUID.randomUUID().toString(), libraryUpdateResult);
	    savedResult.setLibraryResults(libraryResults);
	    
	    return savedResult;
	}
	
	private CodeTemplateSummary getCodeTemplateSummary() {
        return new CodeTemplateSummary(UUID.randomUUID().toString(), getCodeTemplateExample(true));
	}
	
	private List<CodeTemplateSummary> getCodeTemplateSummaryListExample(boolean revisionChanged) {
	    List<CodeTemplateSummary> list = new ArrayList<>();
	    if (revisionChanged) {
	        list.add(getCodeTemplateSummary());
	    }
	    return list;
	}
	
	private Map<String, List<Integer>> getConnectorMapExample(boolean includeNull) {
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
	
	private ConnectionLogItem getConnectionLogItemExample() {
	    ConnectionLogItem logItem = new ConnectionLogItem(1L, UUID.randomUUID().toString(), UUID.randomUUID().toString(), 0L, dateFormat.format(dateNow.getTime()), "Channel 1", "Source: Channel Reader (HL7V2 -> JSON)", "Idle", "");
	    return logItem;
	}
	
	private LinkedList<ConnectionLogItem> getConnectionLogItemLinkedListExample() {
	    LinkedList<ConnectionLogItem> logItems = new LinkedList<>();
	    logItems.add(getConnectionLogItemExample());
	    return logItems;
	}
	
	private ConnectionTestResponse getFileConnectionTestResponseExample() {
        return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Successfully connected to: /some_folder");
	}
	
	private ConnectionTestResponse getHttpConnectionTestResponseExample() {
	    return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Successfully connected to host: 0.0.0.0:54551 -> 1.1.1.1:9000", "0.0.0.0:54551 -> 1.1.1.1:9000");
	}
	
	private ConnectionTestResponse getSmtpConnectionTestResponseExample() {
	    return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Sucessfully sent test email to: " + "test@example.com");
	}
	
	private ConnectionTestResponse getTcpConnectionTestResponseExample() {
	    return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Successfully connected to host: 0.0.0.0:53930 -> 1.1.1.1:6661", "0.0.0.0:53930 -> 1.1.1.1:6661");
	}
	
    private ConnectionTestResponse getWsConnectionTestResponseExample() {
        return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Successfully connected to host: 0.0.0.0:53930 -> 1.1.1.1:8081", "0.0.0.0:53930 -> 1.1.1.1:8081");
    }
	
	private ConnectorMetaData getConnectorMetaDataExample() {
        ConnectorMetaData metaData = new ConnectorMetaData();
        configureMetaData(metaData);
        metaData.setServerClassName("com.example.package.ServerClass");
        metaData.setSharedClassName("com.example.package.SharedClass");
        metaData.setClientClassName("com.example.package.ClientClass");
        metaData.setTransformers("");
        metaData.setProtocol("protocol");
        metaData.setType(ConnectorMetaData.Type.DESTINATION);
        return metaData;
    }
	
	private void configureMetaData(MetaData metaData) {
        List<ApiProvider> apiProviders = new ArrayList<>();
        ApiProvider apiProvider = new ApiProvider();
        apiProvider.setType(ApiProvider.Type.SERVLET_INTERFACE);
        apiProvider.setName("com.example.package.ServletInterface");
        apiProviders.add(apiProvider);
        
        List<ExtensionLibrary> extensionLibraries = new ArrayList<>();
        ExtensionLibrary extensionLibrary = new ExtensionLibrary();
        extensionLibrary.setPath("client.jar");
        extensionLibrary.setType(ExtensionLibrary.Type.CLIENT);
        extensionLibraries.add(extensionLibrary);
        
        metaData.setPath("path");
        metaData.setName("Name");
        metaData.setAuthor("Author");
        metaData.setMirthVersion(Version.getLatest().toString());
        metaData.setPluginVersion(Version.getLatest().toString());
        metaData.setUrl("http://exampleurl.com");
        metaData.setDescription("Example description.");
        metaData.setApiProviders(apiProviders);
        metaData.setLibraries(extensionLibraries);
    }
	
	private ConnectorMessage getConnectorMessageExample() {
	    ConnectorMessage connectorMessage = new ConnectorMessage(UUID.randomUUID().toString(), "Channel 1", 1L, 0, UUID.randomUUID().toString(), dateNow, Status.SENT);
	    return connectorMessage;
	}
	
	private Map<String, ConnectorMetaData> getConnectorMetaDataMapExample() {
	    Map<String, ConnectorMetaData> connectorMetaDataMap = new HashMap<>();
	    connectorMetaDataMap.put("Name", getConnectorMetaDataExample());
	    return connectorMetaDataMap;
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
	
	private DefinitionServiceMap getDefinitionServiceMapExample() {
	    DefinitionServiceMap definitionMap = new DefinitionServiceMap();
	    Map<String, DefinitionPortMap> portMap = definitionMap.getMap();
	    DefinitionPortMap definitionPortMap = new DefinitionPortMap();
	    Map<String, PortInformation> portInformationMap = definitionPortMap.getMap();
	    
	    List<String> operationList = new ArrayList<>();
	    operationList.add("acceptMessage");
	    List<String> actionList = new ArrayList<>();
	    actionList.add("SomeAction");
	    PortInformation portInformation = new PortInformation(operationList, actionList, "http://example.com:8081/services/SomeService");
	    
	    portInformationMap.put("{http://ws.connectors.connect.mirth.com/}DefaultAcceptMessagePort", portInformation);
	    
	    portMap.put("{http://ws.connectors.connect.mirth.com/}DefaultAcceptMessageService", definitionPortMap);
	    
	    return definitionMap;
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
	
	private FileReceiverProperties getFileReceiverPropertiesExample() {
	    FileReceiverProperties receiverProperties = new FileReceiverProperties();
	    receiverProperties.setHost("/some_folder");
	    return receiverProperties;
	}
	
	private FileDispatcherProperties getFileDispatcherPropertiesExample() {
	    FileDispatcherProperties dispatcherProperties = new FileDispatcherProperties();
	    dispatcherProperties.setHost("/some_folder");
	    dispatcherProperties.setOutputPattern("some_file.ext");
	    return dispatcherProperties;
	}
	
	private String getGenerateEnvelopeExample() {
	    String envelope = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://ws.connectors.connect.mirth.com/\">\n" + 
	            "   <soapenv:Header/>\n" + 
	            "   <soapenv:Body>\n" + 
	            "      <ws:acceptMessage/>\n" + 
	            "   </soapenv:Body>\n" + 
	            "</soapenv:Envelope>";
	    return envelope;
	}
	
	private Map<String, String> getGenericMapExample() {
        Map<String, String> genericMap = new HashMap<>();
        genericMap.put("exampleKey", "exampleValue");
        return genericMap;
    }
	
	private String getGenericMapStringExample() {
	    try {
	        return xmlSerialize(getGenericMapExample());
	    } catch (UnsupportedEncodingException e) {}
	    
	    return "";
	}
	
	private Map<String, String> getGlobalMapExample() {
	    Map<String, String> globalMap = new HashMap<>();
	    globalMap.put(null, getGenericMapStringExample());
	    return globalMap;
	}
	
	private Map<String, Map<String, String>> getGlobalMapsExample() {
	    Map<String, Map<String, String>> globalMaps = new HashMap<>();
	    globalMaps.put(UUID.randomUUID().toString(), getGlobalMapExample());
	    return globalMaps;
	}

	private Set<String> getGuidSetExample() {
		Set<String> stringSet = new HashSet<>();
		stringSet.add(UUID.randomUUID().toString());
		stringSet.add(UUID.randomUUID().toString());
		return stringSet;
	}
	
	private Map<String, Integer> getGuidToIntMapExample() {
	    Map<String, Integer> guidToIntMap = new HashMap<>();
	    guidToIntMap.put(UUID.randomUUID().toString(), 1);
	    return guidToIntMap;
	}
	
	private Map<String, String> getGuidToNameMapExample() {
		Map<String, String> guidToNameMap = new HashMap<>();
		guidToNameMap.put(UUID.randomUUID().toString(), "Name 1");
		guidToNameMap.put(UUID.randomUUID().toString(), "Name 2");
		return guidToNameMap;
	}
	
	private HttpDispatcherProperties getHttpDispatcherPropertiesExample() {
	    HttpDispatcherProperties dispatcherProperties = new HttpDispatcherProperties();
	    dispatcherProperties.setHost("http://example.com:9000");
	    return dispatcherProperties;
	}
	
	private Set<String> getJmsTemplateNameSetExample() {
	    Set<String> templateSet = new HashSet<>();
	    templateSet.add("Template 1");
	    templateSet.add("Template 2");
	    return templateSet;
	}
	
	private JmsConnectorProperties getJmsConnectorPropertiesExample() {
        JmsConnectorProperties properties = new JmsConnectorProperties();
        properties.setUseJndi(false);
        properties.setConnectionFactoryClass("com.some.connection.FactoryClass");
        properties.getConnectionProperties().put("property1", "value1");
        properties.getConnectionProperties().put("property2", "value2");
        return properties;
	}
	
	private Map<String, JmsConnectorProperties> getJmsConnectorPropertiesMapExample() {
	    Map<String, JmsConnectorProperties> map = new LinkedHashMap<>();
        map.put("Template 1", getJmsConnectorPropertiesExample());
        return map;
	}
	
	private List<String> getLibraryListExample() {
	    List<String> libraryList = new ArrayList<>();
	    libraryList.add("library1.jar");
	    libraryList.add("library2.jar");
	    return libraryList;
	}
	
	private Long getLongExample() {
	    return 2L;
	}
	
	private Message getMessageExample() {
	    Message message = new Message();
	    message.setServerId(UUID.randomUUID().toString());
	    message.setChannelId(UUID.randomUUID().toString());
	    message.setReceivedDate(dateNow);
	    message.setProcessed(true);
	    message.getConnectorMessages().put(1, getConnectorMessageExample());
	    return message;
	}
	
	private List<Message> getMessageListExample() {
	    List<Message> messages = new ArrayList<>();
	    messages.add(getMessageExample());
	    return messages;
	}
	
	private MessageFilter getMessageFilterExample() {
	    List<ContentSearchElement> contentSearch = new ArrayList<>();
	    List<String> searches = new ArrayList<>();
	    searches.add("keyword");
	    contentSearch.add(new ContentSearchElement(1, searches));
	    
	    List<Integer> excludedMetaDataIds = new ArrayList<>();
	    excludedMetaDataIds.add(2);
	    
	    List<Integer> includedMetaDataIds = new ArrayList<>();
	    includedMetaDataIds.add(0);
	    includedMetaDataIds.add(1);
	    
	    List<MetaDataSearchElement> metaDataSearch = new ArrayList<>();
	    metaDataSearch.add(new MetaDataSearchElement("Column 1", "operator", "example value", true));
	    
	    Set<Status> statuses = new HashSet<>();
	    statuses.add(Status.ERROR);
	    
	    MessageFilter messageFilter = new MessageFilter();
	    messageFilter.setAttachment(false);
	    messageFilter.setContentSearch(contentSearch);
	    messageFilter.setEndDate(dateTomorrow);
	    messageFilter.setError(false);
	    messageFilter.setImportIdLower(1L);
	    messageFilter.setIncludedMetaDataIds(includedMetaDataIds);
	    messageFilter.setMaxMessageId(200L);
	    messageFilter.setMetaDataSearch(metaDataSearch);
	    messageFilter.setMinMessageId(5L);
	    messageFilter.setOriginalIdUpper(25L);
	    messageFilter.setSendAttemptsLower(0);
	    messageFilter.setServerId(UUID.randomUUID().toString());
	    messageFilter.setStartDate(dateNow);
	    messageFilter.setStatuses(statuses);
	    messageFilter.setTextSearch("keyword");
	    return messageFilter;
	}
	
	private MessageImportResult getMessageImportResultExample() {
	    MessageImportResult result = new MessageImportResult(10, 8);
	    return result;
	}
	
	private List<MetaDataColumn> getMetaDataColumnListExample() {
		List<MetaDataColumn> metaDataColumns = new ArrayList<>();
		metaDataColumns.add(new MetaDataColumn("SOURCE", MetaDataColumnType.STRING, "mirth_source"));
		metaDataColumns.add(new MetaDataColumn("TYPE", MetaDataColumnType.STRING, "mirth_type"));
		return metaDataColumns;
	}
	
	private PluginMetaData getPluginMetaDataExample() {
	    PluginMetaData metaData = new PluginMetaData();
	    configureMetaData(metaData);
	    
	    List<PluginClass> serverClasses = new ArrayList<>();
	    PluginClass serverClass = new PluginClass();
	    serverClass.setName("com.example.package.ServerPlugin");
	    serverClasses.add(serverClass);
	    metaData.setServerClasses(serverClasses);
	    
	    List<PluginClass> clientClasses = new ArrayList<>();
	    PluginClass clientClass = new PluginClass();
        clientClass.setName("com.example.package.ClientPlugin");
        clientClasses.add(clientClass);
	    metaData.setClientClasses(clientClasses);
	    
	    return metaData;
	}
	
	private Map<String, PluginMetaData> getPluginMetaDataMapExample() {
	    Map<String, PluginMetaData> pluginMetaDataMap = new HashMap<>();
	    pluginMetaDataMap.put("Name", getPluginMetaDataExample());
	    return pluginMetaDataMap;
	}
	
	private Properties getPropertiesExample() {
	    Properties properties = new Properties();
	    properties.setProperty("exampleKey1", "exampleValue1");
	    properties.setProperty("exampleKey2", "exampleValue2");
	    return properties;
	}
	
	private PurgedDocument getPurgedDocumentExample() {
	    PurgedDocument purgedDocument = new PurgedDocument();
	    purgedDocument.setServerId(UUID.randomUUID().toString());
	    purgedDocument.setMirthVersion(Version.getLatest().toString());
	    purgedDocument.setUsers(10);
	    return purgedDocument;
	}
	
	private RawMessage getRawMessageExample() {
	    String rawData = "Example raw data.";
	    Collection<Integer> destinationMetaDataIds = new HashSet<>();
	    destinationMetaDataIds.add(1);
	    Map<String, Object> sourceMap = new HashMap<>();
	    sourceMap.put("exampleKey", "exampleValue");
	    List<Attachment> attachments = getAttachmentListExample();
	    
	    RawMessage rawMessage = new RawMessage(rawData, destinationMetaDataIds, sourceMap, attachments);
	    return rawMessage;
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
	    List<ServerLogItem> serverLogList = new ArrayList<>();
	    serverLogList.add(getServerLogItemExample());
	    return serverLogList;
	}
	
	private SmtpDispatcherProperties getSmtpDispatcherPropertiesExample(String encryption) {
	    SmtpDispatcherProperties props = new SmtpDispatcherProperties();
	    if ("SSL".equalsIgnoreCase(encryption)) {
	        props.setSmtpPort("465");
            props.setEncryption("SSL");
            props.setAuthentication(true);
            props.setUsername("username@example.com");
            props.setPassword("your_password");
	    } else if ("TLS".equalsIgnoreCase(encryption)) {
	        props.setSmtpPort("587");
            props.setEncryption("TLS");
            props.setAuthentication(true);
            props.setUsername("username@example.com");
            props.setPassword("your_password");
	    } else {
	        props.setSmtpPort("25");
	        props.setEncryption("none");
	    }
        
	    props.setSmtpHost("smtp.example.com");
	    props.setTo("test@example.com");
	    props.setFrom("you@test.com");
	    
	    return props;
	}
	
	private SystemInfo getSystemInfoExample() {
	    SystemInfo systemInfo = new SystemInfo();
	    systemInfo.setJvmVersion("1.8.0_172");
	    systemInfo.setOsName("Mac OS X");
	    systemInfo.setOsVersion("10.14.5");
	    systemInfo.setOsArchitecture("x86_64");
	    systemInfo.setDbName("PostgreSQL");
	    systemInfo.setDbVersion("9.6.15");
	    return systemInfo;
	}
	
	private SystemStats getSystemStatsExample() {
	    SystemStats systemStats = new SystemStats();
	    systemStats.setTimestamp(dateNow);
	    systemStats.setCpuUsagePct(50.0);
	    systemStats.setAllocatedMemoryBytes(300_000_000L);
	    systemStats.setFreeMemoryBytes(200_000_000L);
	    systemStats.setMaxMemoryBytes(500_000_000L);
	    systemStats.setDiskFreeBytes(70_000_000_000L);
	    systemStats.setDiskTotalBytes(500_000_000_000L);
	    return systemStats;
	}
	
	private Set<Table> getTableSetExample() {
	    Set<Table> tableSet = new TreeSet<>();
	    List<Column> columns = new ArrayList<>();
	    columns.add(new Column("id", "bpchar", 36));
	    columns.add(new Column("name", "varchar", 255));
	    tableSet.add(new Table("table_name", columns));
	    return tableSet;
	}
	
	private TcpDispatcherProperties getTcpDispatcherPropertiesExample() {
	    TcpDispatcherProperties props = new TcpDispatcherProperties();
	    props.setRemoteAddress("example.com");
	    props.setRemotePort("6661");
	    return props;
	}
	
	private WebServiceDispatcherProperties getWsDispatcherPropertiesExample() {
	    WebServiceDispatcherProperties props = new WebServiceDispatcherProperties();
	    props.setWsdlUrl("http://example.com:8081/services/SomeService?wsdl");
	    props.setLocationURI("http://example.com:8081/services/SomeService");
	    return props;
	}
}
