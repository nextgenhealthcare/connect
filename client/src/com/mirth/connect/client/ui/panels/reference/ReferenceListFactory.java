/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.panels.reference;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import com.mirth.connect.client.ui.LoadedExtensions;
import com.mirth.connect.client.ui.PlatformUI;
import com.mirth.connect.client.ui.panels.connectors.ConnectorSettingsPanel;
import com.mirth.connect.model.CodeTemplate;
import com.mirth.connect.model.CodeTemplate.CodeSnippetType;
import com.mirth.connect.model.CodeTemplate.ContextType;
import com.mirth.connect.model.util.DefaultMetaData;
import com.mirth.connect.plugins.CodeTemplatePlugin;

public class ReferenceListFactory {

    public enum ListType {

        ALL("All"), CONVERSION("Conversion Functions"), LOGGING_AND_ALERTS("Logging and Alerts"), DATABASE(
                "Database Functions"), UTILITY("Utility Functions"), DATE("Date Functions"), MESSAGE(
                "Message Functions"), RESPONSE("Response Transformer"), MAP("Map Functions"), CHANNEL(
                "Channel Functions"), POSTPROCESSOR("Postprocessor Functions");
        private String value;

        ListType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static final String USER_TEMPLATE_VARIABLES = "User Defined Variables";
    public static final String USER_TEMPLATE_CODE = "User Defined Code";
    public static final String USER_TEMPLATE_FUNCTIONS = "User Defined Functions";
    private static ReferenceListFactory instance;
    private LinkedHashMap<String, ArrayList<CodeTemplate>> references;

    private ReferenceListFactory() {
        references = new LinkedHashMap<String, ArrayList<CodeTemplate>>();
        if (PlatformUI.MIRTH_FRAME != null) { // null parent check to let forms load in NetBeans
            setup();
        }
    }

    public static ReferenceListFactory getInstance() {
        synchronized (ReferenceListFactory.class) {
            if (instance == null) {
                instance = new ReferenceListFactory();
            }

            return instance;
        }
    }

    public LinkedHashMap<String, ArrayList<CodeTemplate>> getReferences() {
        return references;
    }

    public void setup() {
        references.put(ListType.CONVERSION.getValue(), setupConversionItems());
        references.put(ListType.DATABASE.getValue(), setupDatabaseItems());
        references.put(ListType.LOGGING_AND_ALERTS.getValue(), setupLoggingAndAlertsItems());
        references.put(ListType.MESSAGE.getValue(), setupMessageItems());
        references.put(ListType.RESPONSE.getValue(), setupResponseItems());
        references.put(ListType.CHANNEL.getValue(), setupChannelItems());
        references.put(ListType.MAP.getValue(), setupMapItems());
        references.put(ListType.UTILITY.getValue(), setupUtilityItems());
        references.put(ListType.DATE.getValue(), setupDateItems());
        references.put(ListType.POSTPROCESSOR.getValue(), setupPostprocessorItems());

        for (Entry<String, ConnectorSettingsPanel> connectorEntry : LoadedExtensions.getInstance().getConnectors().entrySet()) {
            ArrayList<CodeTemplate> items = connectorEntry.getValue().getReferenceItems();
            if (items.size() > 0) {
                references.put(connectorEntry.getKey() + " Functions", items);
            }
        }

        for (Entry<String, CodeTemplatePlugin> codeTemplatePluginEntry : LoadedExtensions.getInstance().getCodeTemplatePlugins().entrySet()) {
            ArrayList<CodeTemplate> items = codeTemplatePluginEntry.getValue().getReferenceItems();
            if (items.size() > 0) {
                references.put(codeTemplatePluginEntry.getKey() + " Functions", items);
            }
        }

        updateUserTemplates();
    }

    public void updateUserTemplates() {
        ArrayList<CodeTemplate> variables = new ArrayList<CodeTemplate>();
        ArrayList<CodeTemplate> functions = new ArrayList<CodeTemplate>();
        ArrayList<CodeTemplate> code = new ArrayList<CodeTemplate>();
        for (CodeTemplate template : PlatformUI.MIRTH_FRAME.codeTemplates) {
            if (template.getType() == CodeSnippetType.VARIABLE) {
                variables.add(template);
            }

            if (template.getType() == CodeSnippetType.FUNCTION) {
                functions.add(template);
            }

            if (template.getType() == CodeSnippetType.CODE) {
                code.add(template);
            }
        }

        references.put(USER_TEMPLATE_VARIABLES, variables);
        references.put(USER_TEMPLATE_CODE, code);
        references.put(USER_TEMPLATE_FUNCTIONS, functions);
    }

    public ArrayList<CodeTemplate> getVariableListItems(String itemName, int context) {
        if (PlatformUI.MIRTH_FRAME != null) { // null parent check to let forms load in NetBeans
            updateUserTemplates();
        }

        if (itemName == ListType.ALL.getValue()) {
            return getAllItems(context);
        } else {
            return getItems(itemName, context);
        }
    }

    private ArrayList<CodeTemplate> getItems(String reference, int context) {
        ArrayList<CodeTemplate> variablelistItems = new ArrayList<CodeTemplate>();

        if (references.get(reference) == null) {
            return new ArrayList<CodeTemplate>();
        }

        for (CodeTemplate item : references.get(reference)) {
            if (context >= item.getScope()) {
                variablelistItems.add(item);
            }
        }

        return variablelistItems;
    }

    private ArrayList<CodeTemplate> getAllItems(int context) {
        ArrayList<CodeTemplate> variablelistItems = new ArrayList<CodeTemplate>();

        for (ArrayList<CodeTemplate> items : references.values()) {
            for (CodeTemplate item : items) {
                if (context >= item.getScope()) {
                    variablelistItems.add(item);
                }
            }
        }

        return variablelistItems;
    }

    private ArrayList<CodeTemplate> setupConversionItems() {
        ArrayList<CodeTemplate> variablelistItems = new ArrayList<CodeTemplate>();

        variablelistItems.add(new CodeTemplate("Get Serializer", "Creates and returns a data type serializer with the specified serialization and deserialization properties. " + getDataTypesToolTipText(), "var dataType = 'HL7V2';\nvar serializationProperties = SerializerFactory.getDefaultSerializationProperties(dataType);\nvar deserializationProperties = SerializerFactory.getDefaultDeserializationProperties(dataType);\nvar serializer = SerializerFactory.getSerializer(dataType, serializationProperties, deserializationProperties);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));

        return variablelistItems;
    }

    private String getDataTypesToolTipText() {
        StringBuilder builder = new StringBuilder("The available data type keys are:<br/><br/>");
        for (String key : LoadedExtensions.getInstance().getDataTypePlugins().keySet()) {
            builder.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            builder.append(key);
            builder.append("<br/>");
        }
        return builder.toString();
    }

    private ArrayList<CodeTemplate> setupLoggingAndAlertsItems() {
        ArrayList<CodeTemplate> variablelistItems = new ArrayList<CodeTemplate>();
        variablelistItems.add(new CodeTemplate("Log an Info Statement", "Outputs the message to the system info log.", "logger.info('message');", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Log an Error Statement", "Outputs the message to the system error log.", "logger.error('message');", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Send an Email", "Sends an alert email using the alert SMTP properties.", "var smtpConn = SMTPConnectionFactory.createSMTPConnection();\nsmtpConn.send('to', 'cc', 'from', 'subject', 'body', 'charset');", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Trigger an Alert", "Trigger a custom alert for the current channel.", "alerts.sendAlert('message');", CodeSnippetType.CODE, ContextType.CHANNEL_CONTEXT.getContext()));

        return variablelistItems;
    }

    private ArrayList<CodeTemplate> setupDatabaseItems() {
        ArrayList<CodeTemplate> variablelistItems = new ArrayList<CodeTemplate>();
        variablelistItems.add(new CodeTemplate("Perform Database Query", "Performs a database query and returns the rowset.", "var dbConn;\nvar result;\n\ntry {\n\tdbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\n\tresult = dbConn.executeCachedQuery('expression');\n} finally {\n\tif (dbConn) {\n\t\tdbConn.close();\n\t}\n}", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Perform Parameterized Database Query", "Performs a database query with a (Java) list of parameters.", "var dbConn;\nvar result;\n\ntry {\n\tdbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\n\tresult = dbConn.executeCachedQuery('expression', paramList);\n} finally {\n\tif (dbConn) {\n\t\tdbConn.close();\n\t}\n}", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Perform Database Update", "Performs a database update.", "var dbConn;\nvar result;\n\ntry {\n\tdbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\n\tresult = dbConn.executeUpdate('expression');\n} finally {\n\tif (dbConn) {\n\t\tdbConn.close();\n\t}\n}", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Perform Parameterized Database Update", "Performs a database update with a (Java) list of parameters.", "var dbConn;\nvar result;\n\ntry {\n\tdbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\n\tresult = dbConn.executeUpdate('expression', paramList);\n} finally {\n\tif (dbConn) {\n\t\tdbConn.close();\n\t}\n}", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Postgres Connection Template", "String template for Postgres database connection.", "\"jdbc:postgresql://host:port/dbname\"", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("MySQL Connection Template", "String template for MySQL database connection.", "\"jdbc:mysql://host:port/dbname\"", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("SQL Server Connection Template", "String template for SQL Server database connection.", "\"jdbc:jtds:sqlserver://host:port/dbname\"", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Oracle Connection Template", "String template for Oracle database connection.", "\"jdbc:oracle:thin:@host:port:dbname\"", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Postgres Driver", "String used for Postgres database driver.", "\"org.postgresql.Driver\"", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("MySQL Driver", "String used for MySQL database driver.", "\"com.mysql.jdbc.Driver\"", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("SQL Server Driver", "String used for SQL Server database driver.", "\"net.sourceforge.jtds.jdbc.Driver\"", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Oracle Driver", "String used for Oracle database driver.", "\"oracle.jdbc.OracleDriver\"", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Initialize Driver", "Initialize the specified JDBC driver. (Same as calling Class.forName)", "DatabaseConnectionFactory.initializeDriver('driver');", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));

        return variablelistItems;
    }

    private ArrayList<CodeTemplate> setupMessageItems() {
        ArrayList<CodeTemplate> variablelistItems = new ArrayList<CodeTemplate>();

        variablelistItems.add(new CodeTemplate("Incoming Message", "The original message received.", "connectorMessage.getRawData()", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Incoming Message (XML)", "The original message as XML", "msg", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Message Source", "The message source (sending facility)", "$('" + DefaultMetaData.SOURCE_VARIABLE_MAPPING + "')", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Message Type", "The message type", "$('" + DefaultMetaData.TYPE_VARIABLE_MAPPING + "')", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Message Version", "The message version", "$('" + DefaultMetaData.VERSION_VARIABLE_MAPPING + "')", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Message ID", "The ID of the overall message being processed", "connectorMessage.getMessageId()", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Metadata ID", "The ID of the connector the message is currently being processed through", "connectorMessage.getMetaDataId()", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Message Inbound Data Type", "The inbound data type for this connector message", "connectorMessage.getRaw().getDataType()", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Iterate Over Segment", "Iterates over a segment that repeats in a message.  Replace SEG with your segment name (i.e. OBX)", "for each (seg in msg..SEG) {\n\tvar sample_value = seg['SEG.1']['SEG.1.1'].toString();\n}\n", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Iterate Over All Segments", "Iterates over all segments in a message.  The if-statement checks for only segments named \"SEG\".", "for each (seg in msg.children()) {\n\tif (seg.name().toString() == \"SEG\") {\n\t\tvar sample_value = seg['SEG.1']['SEG.1.1'].toString();\n\t}\n}\n", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Create Segment (individual)", "Create a new segment that can be used in any message", "createSegment('segmentName')", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Create Segment (in message)", "Create a new segment in specified message (msg or tmp)", "createSegment('segmentName', msg)", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Create Segment (in message, index)", "Create a new segment in specified message (msg or tmp) at segment index i", "createSegment('segmentName', msg, i)", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Create Segment After Segment", "Create a new segment and insert it after the target segment", "createSegmentAfter('insertSegmentName', afterThisSegment)", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Delete Segment", "Delete a segment from the message", "delete msg['segment']", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Remove one or more from Destination Set", "Stop one or more destinations from being processed for this message. Only available in the preprocessor or source filter/transformer.", "destinationSet.remove([metaDataIdOrConnectorNames]);", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Remove all except specified from Destination Set", "Stop all except the specified destinations from being processed for this message. Only available in the preprocessor or source filter/transformer.", "destinationSet.removeAllExcept([metaDataIdOrConnectorNames]);", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Remove all from Destination Set", "Stop all destinations from being processed for this message. Only available in the preprocessor or source filter/transformer.", "destinationSet.removeAll();", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));

        return variablelistItems;
    }

    private ArrayList<CodeTemplate> setupResponseItems() {
        ArrayList<CodeTemplate> variablelistItems = new ArrayList<CodeTemplate>();

        variablelistItems.add(new CodeTemplate("Set Response Status to SENT", "Indicates message was successfully SENT.", "responseStatus = SENT;", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Set Response Status to QUEUED", "Indicates message should be QUEUED. If queuing is disabled, the message status will be set to ERROR.", "responseStatus = QUEUED;", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Set Response Status to ERROR", "Indicates message should have its status set to ERROR.", "responseStatus = ERROR;", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Set Response Status Message", "Sets the status message of the response.", "responseStatusMessage = '';", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Set Response Error Message", "Sets the error message of the response.", "responseErrorMessage = '';", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));

        return variablelistItems;
    }

    private ArrayList<CodeTemplate> setupChannelItems() {
        ArrayList<CodeTemplate> variablelistItems = new ArrayList<CodeTemplate>();

        variablelistItems.add(new CodeTemplate("Channel ID", "The message channel id", "channelId", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CHANNEL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Channel Name", "The message channel name", "var channelName = ChannelUtil.getDeployedChannelName(channelId);", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CHANNEL_CONTEXT.getContext()));

        return variablelistItems;
    }

    private ArrayList<CodeTemplate> setupMapItems() {
        ArrayList<CodeTemplate> variablelistItems = new ArrayList<CodeTemplate>();
        variablelistItems.add(new CodeTemplate("Lookup Value in All Maps", "Returns the value of the key if it exists in any map.", "$('key')", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Get Configuration Variable Map", "The variable map containing server specific settings.", "configurationMap.get('key')", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Get Global Variable Map", "The variable map that persists values between channels.", "globalMap.get('key')", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Put Global Variable Map", "The variable map that persists values between channels.", "globalMap.put('key','value')", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Get Global Channel Variable Map", "The variable map that persists values between messages in a single channel.", "globalChannelMap.get('key')", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Put Global Channel Variable Map", "The variable map that persists values between messages in a single channel.", "globalChannelMap.put('key','value')", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Get Connector Variable Map", "The variable map that will be sent to the connector.", "connectorMap.get('key')", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Put Connector Variable Map", "The variable map that will be sent to the connector.", "connectorMap.put('key','value')", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Get Channel Variable Map", "The variable map that can be used anywhere in the channel.", "channelMap.get('key')", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Put Channel Variable Map", "The variable map that can be used anywhere in the channel.", "channelMap.put('key','value')", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Get Source Variable Map", "The variable map containing metadata about the original message. This map is read-only.", "sourceMap.get('key')", CodeSnippetType.VARIABLE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Get Response Variable Map", "The variable map that stores responses.", "responseMap.get('key')", CodeSnippetType.VARIABLE, ContextType.CHANNEL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Put Sent Response Variable", "Places a successful response in the response variable map.", "responseMap.put('key', ResponseFactory.getSentResponse('message'))", CodeSnippetType.VARIABLE, ContextType.CHANNEL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Put Error Response Variable", "Places an unsuccessful response in the response variable map.", "responseMap.put('key', ResponseFactory.getErrorResponse('message'))", CodeSnippetType.VARIABLE, ContextType.CHANNEL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Create Sent Response", "Creates a successful response object.", "ResponseFactory.getSentResponse('message')", CodeSnippetType.VARIABLE, ContextType.CHANNEL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Create Error Response", "Creates an unsuccessful response object.", "ResponseFactory.getErrorResponse('message')", CodeSnippetType.VARIABLE, ContextType.CHANNEL_CONTEXT.getContext()));

        return variablelistItems;
    }

    private ArrayList<CodeTemplate> setupUtilityItems() {
        ArrayList<CodeTemplate> variablelistItems = new ArrayList<CodeTemplate>();
        variablelistItems.add(new CodeTemplate("Use Java Class", "Access any Java class in the current classpath", "var object = Packages.[fully-qualified name];", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Generate Unique ID", "Generate a Universally Unique Identifier", "var uuid = UUIDGenerator.getUUID();", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Call System Function", "Execute a command on server system. Must have proper security enabled.", "java.lang.Runtime.getRuntime().exec(\"system_command\");", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Read File As String", "Read file contents into string", "var contents = FileUtil.read('filename');", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Read File As Bytes", "Read file contents into byte array", "var contents = FileUtil.readBytes('filename');", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Write String to File", "Write string to file", "FileUtil.write('filename', append(true/false), stringData);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Write Bytes to File", "Write bytes to file", "FileUtil.write('filename', append(true/false), byteData);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("BASE-64 Encode Data", "Encode a byte array to a BASE-64 string", "FileUtil.encode(data);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Decode BASE-64 Data", "Decode a BASE-64 string to a byte array", "FileUtil.decode(data);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Route Message to Channel", "Sends the specified data to a different channel.", "router.routeMessage('channelName', 'message');", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Route Message by Channel ID", "Sends the specified data to a different channel.", "router.routeMessageByChannelId('channelId', 'message');", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Perform Message Object Value Replacement", "Returns a string that has been run through Velocity replacer with a connectorMessage context", "var results = replacer.replaceValues(template, connectorMessage);", CodeSnippetType.CODE, ContextType.MESSAGE_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Perform Map Value Replacement", "Returns a string that has been run through Velocity replacer with a map context", "var results = replacer.replaceValues(template, map);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Format Overpunch NCPDP Number", "Returns number with decimal points and correct sign", "var number = NCPDPUtil.formatNCPDPNumber('number', decimalpoints);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Convert DICOM to Encoded Image as a BASE-64 string", "Converts and returns an image as a BASE-64 string from an uncompressed DICOM image (imagetype: either TIF, JPEG, BMP, PNG, or RAW). If the slice index is left out, it will default to 1.", "DICOMUtil.convertDICOM('imagetype', connectorMessage, sliceIndex)", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Convert DICOM to Encoded Image as a byte array", "Converts and returns and image as a byte array from an uncompressed DICOM image (imagetype: either TIF,JPEG, BMP, PNG, or RAW). If the slice index is left out, it will default to 1.", "DICOMUtil.convertDICOMToByteArray('imagetype', connectorMessage, sliceIndex)", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Get DICOM image slice count", "Returns the number of image slices within the uncompressed DICOM image", "DICOMUtil.getSliceCount(connectorMessage)", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Get DICOM message", "Gets the full DICOM messages with image data", "DICOMUtil.getDICOMMessage(connectorMessage)", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Add Attachment", "Add attachment (String or byte[]) to message", "addAttachment(data, type)", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Get Attachments", "Get List of Attachments associated with this message.  This will get all attachments that have been added in the source and destination(s).", "getAttachments()", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Strip Namespaces", "Remove namespaces from an XML string", "var newMessage = message.replace(/xmlns:?[^=]*=[\"\"][^\"\"]*[\"\"]/g, '');\n", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Parse HTTP Headers", "Takes the string of an HTTP Response and returns it represented as a map for easy access.", "var headers = HTTPUtil.parseHeaders(header);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Remove Illegal XML Characters", "Removes illegal XML characters like control characters that cause a parsing error in e4x (\\x00-\\x1F besides TAB, LF, and CR)", "var newMessage = message.replace(/[\\x00-\\x08]|[\\x0B-\\x0C]|[\\x0E-\\x1F]/g, '');\n", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext())); // MIRTH-1202

        return variablelistItems;
    }

    private ArrayList<CodeTemplate> setupDateItems() {
        ArrayList<CodeTemplate> variablelistItems = new ArrayList<CodeTemplate>();
        variablelistItems.add(new CodeTemplate("Get Date Object From Pattern", "Parse a date according to specified pattern", "var date = DateUtil.getDate(pattern, date);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Format Date Object", "Formats a date object based on specified format", "var dateString = DateUtil.formatDate(pattern, date);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Convert Date String", "Parse a date and return a newly formatted date", "var datestring = DateUtil.convertDate(inpattern, outpattern, date);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Get Current Date", "Returns the current date/time in specified format", "var dateString = DateUtil.getCurrentDate(pattern);", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));

        return variablelistItems;
    }

    private ArrayList<CodeTemplate> setupPostprocessorItems() {
        ArrayList<CodeTemplate> variablelistItems = new ArrayList<CodeTemplate>();
        variablelistItems.add(new CodeTemplate("Completed Message Object", "The final message object, which contains all processed source and destination connector messages.", "message", CodeSnippetType.VARIABLE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Get Merged Connector Message", "Returns a connector message that has a channel map and response map which are merged from all connector messages.\nThis also includes the raw and processed raw content from the source connector.", "message.getMergedConnectorMessage()", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Get Source Connector Message", "Returns the source connector message contained in the final message object.", "message.getConnectorMessages().get(0)", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        variablelistItems.add(new CodeTemplate("Get Destination Connector Message", "Returns a specific destination connector message contained in the final message object.", "message.getConnectorMessages().get(metaDataId)", CodeSnippetType.CODE, ContextType.GLOBAL_CONTEXT.getContext()));
        return variablelistItems;
    }
}