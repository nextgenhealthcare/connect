package com.webreach.mirth.client.ui.panels.reference;

import java.util.ArrayList;
import java.util.Date;

import com.webreach.mirth.client.ui.CodeSnippetType;
import com.webreach.mirth.model.MessageObject;

public class ReferenceListFactory
{
    public enum ListType
    {
        ALL, CONVERSION, LOGGING_AND_ALERTS, DATABASE, MESSAGE, XML, HL7, MAP, UTILITY, DATE
    };

    public ArrayList<ReferenceListItem> getVariableListItems(ListType itemName)
    {
        switch (itemName)
        {
        case ALL:
            return getAllItems();
        case CONVERSION:
            return getConversionItems();
        case LOGGING_AND_ALERTS:
            return getLoggingAndAlertsItems();
        case DATABASE:
            return getDatabaseItems();
        case MESSAGE:
            return getMessageItems();
       // case XML:
       //     return getXMLItems();
      //  case HL7:
      //      return getHL7Items();
        case MAP:
            return getMapItems();
        case UTILITY:
            return getUtilityItems();
    	case DATE:
             return getDateItems();
         }
        return null;
    }
    
    private ArrayList<ReferenceListItem> getAllItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();
        variablelistItems.addAll(getUtilityItems());
        variablelistItems.addAll(getDateItems());
        variablelistItems.addAll(getConversionItems());
        variablelistItems.addAll(getLoggingAndAlertsItems());
        variablelistItems.addAll(getDatabaseItems());
        variablelistItems.addAll(getMessageItems());
      //  variablelistItems.addAll(getXMLItems());
      //  variablelistItems.addAll(getHL7Items());
        
        variablelistItems.addAll(getMapItems());
        

        return variablelistItems;
    }

    private ArrayList<ReferenceListItem> getConversionItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();
        
        variablelistItems.add(new ReferenceListItem("Convert HL7 to XML", "Converts an encoded HL7 string to XML", "SerializerFactory.getHL7Serializer(useStrictParser, useStrictValidation).toXML(message);\ndefault xml namespace = new Namespace('urn:hl7-org:v2xml');\n", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Convert XML to HL7", "Converts an XML string to HL7", "SerializerFactory.getHL7Serializer(useStrictParser).fromXML(message);", CodeSnippetType.FUNCTION));

        variablelistItems.add(new ReferenceListItem("Convert X12 to XML", "Converts an encoded X12 string to XML", "SerializerFactory.getX12Serializer(inferDelimiters).toXML(message);\ndefault xml namespace = new Namespace('urn:mirthproject-org:x12:xml');\n", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Convert XML to X12", "Converts an XML string to X12", "SerializerFactory.getX12Serializer(inferDelimiters).fromXML(message);", CodeSnippetType.FUNCTION));

        variablelistItems.add(new ReferenceListItem("Convert EDI to XML", "Converts an encoded EDI string to XML", "SerializerFactory.getEDISerializer(segmentDelim, elementDelim, subelementDelim).toXML(message);\ndefault xml namespace = new Namespace('urn:mirthproject-org:edi:xml');\n", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Convert XML to EDI", "Converts an XML string to EDI", "SerializerFactory.getEDISerializer(segmentDelim, elementDelim, subelementDelim).fromXML(message);", CodeSnippetType.FUNCTION));

        return variablelistItems;
    }

    private ArrayList<ReferenceListItem> getLoggingAndAlertsItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();
        variablelistItems.add(new ReferenceListItem("Log an Info Statement", "Outputs the message to the system info log.", "logger.info('message');", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Log an Error Statement", "Outputs the message to the system error log.", "logger.error('message');", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Send an Email", "Sends an alert email using the alert SMTP properties.", "var smtpConn = SMTPConnectionFactory.createSMTPConnection();\nsmtpConn.send('to', 'cc', 'from', 'subject', 'body');", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Trigger an Alert", "Trigger a custom alert for the current channel.", "alerts.sendAlert('message');", CodeSnippetType.FUNCTION));

    
        return variablelistItems;
    }

    private ArrayList<ReferenceListItem> getDatabaseItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();
        variablelistItems.add(new ReferenceListItem("Perform Database Query", "Performs a database query and returns the rowset.", "var dbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\nvar result = dbConn.executeCachedQuery('expression');\ndbConn.close();", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Perform Parameterized Database Query", "Performs a database query with a (Java) list of parameters.", "var dbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\nvar result = dbConn.executeCachedQuery('expression', paramList);\ndbConn.close();", CodeSnippetType.FUNCTION));
        
        variablelistItems.add(new ReferenceListItem("Perform Database Update", "Performs a database update.", "var dbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\nvar result = dbConn.executeUpdate('expression');\ndbConn.close();", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Perform Parameterized Database Update", "Performs a database update with a (Java) list of parameters.", "var dbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\nvar result = dbConn.executeUpdate('expression', paramList);\ndbConn.close();", CodeSnippetType.FUNCTION));

        return variablelistItems;
    }

    private ArrayList<ReferenceListItem> getMessageItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();

        variablelistItems.add(new ReferenceListItem("Incoming Message", "The original message received.", "messageObject.getRawData()", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Incoming Message (XML)", "The original message as XML", "msg", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Message Type", "The message type", "messageObject.getType()", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Message Source", "The message source (sending facility)", "messageObject.getSource()", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Message Version", "The message version", "messageObject.getVersion()", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Message ID", "The unique id of the message in Mirth", "messageObject.getId()", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Message Protocol", "The message protocol", "messageObject.getProtocol().toString()", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Channel ID", "The message channel id", "messageObject.getChannelId()", CodeSnippetType.VARIABLE));
        

        return variablelistItems;
    }
    
/* FOR FUTURE USE
    private ArrayList<ReferenceListItem> getXMLItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();

        return variablelistItems;
    }

    private ArrayList<ReferenceListItem> getHL7Items()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();

        return variablelistItems;
    }
  */ 
    
    private ArrayList<ReferenceListItem> getMapItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();
        variablelistItems.add(new ReferenceListItem("Lookup Value in All Maps", "Returns the value of the key if it exists in any map.", "$('key')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Get Connector Variable Map", "The variable map that will be sent to the connector.", "connectorMap.get('')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Get Channel Variable Map", "The variable map that can be used anywhere in the channel.", "channelMap.get('')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Get Global Variable Map", "The variable map that persists values between channels.", "globalMap.get('')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Get Response Variable Map", "The variable map that stores responses.", "responseMap.get('')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Put Connector Variable Map", "The variable map that will be sent to the connector.", "connectorMap.put(key,'')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Put Channel Variable Map", "The variable map that can be used anywhere in the channel.", "channelMap.put(key,'')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Put Global Variable Map", "The variable map that persists values between channels.", "globalMap.put(key,'')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Put Success Response Variable Map", "The variable map that stores responses.", "responseMap.put(connector, ResponseFactory.getSuccessResponse('message'))", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Put Error Response Variable", "The variable map that stores responses.", "responseMap.put(connector, ResponseFactory.getFailureResponse('message'))", CodeSnippetType.VARIABLE));
               
        return variablelistItems;
    }
    
    private ArrayList<ReferenceListItem> getUtilityItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();
        variablelistItems.add(new ReferenceListItem("Use Java Class", "Access any Java class in the current classpath", "var object = Packages.[fully-qualified name];", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Generate Unique ID", "Generate a Universally Unique Identifier", "var uuid = UUIDGenerator.getUUID();", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Call System Function", "Execute a command on server system. Must have proper security enabled.", "java.lang.Runtime.getRuntime().exec(\"system_command\");", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Read File As String", "Read file contents into string", "var contents = FileUtil.read('filename');", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Read File As Bytes", "Read file contents into byte array", "var contents = FileUtil.readBytes('filename');", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Write String to File", "Write string to file", "FileUtil.write('filename', append(true/false), stringData);", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Write Bytes to File", "Write bytes to file", "FileUtil.write('filename', append(true/false), byteData);", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("BASE-64 Encode Data", "Encode a byte array to a BASE-64 string", "FileUtil.encode(data);", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Decode BASE-64 Data", "Decode a BASE-64 string to a byte array", "FileUtil.decode(data);", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Route Message to Channel", "Sends the specified data to a different channel", "router.routeMessage(channelName, 'message');", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Perform Message Object Value Replacement", "Returns a string that has been run through Velocity replacer with a messageObject context", "var results = replacer.replaceValues(template, messageObject);", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Perform Map Value Replacement", "Returns a string that has been run through Velocity replacer with a map context", "var results = replacer.replaceValues(template, map);", CodeSnippetType.FUNCTION));
        
        return variablelistItems;
    }
    private ArrayList<ReferenceListItem> getDateItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();
        variablelistItems.add(new ReferenceListItem("Get Date Object From Pattern", "Parse a date according to specified pattern", "var date = DateUtil.getDate(pattern, date);", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Format Date Object", "Formats a date object based on specified format", "var dateString = DateUtil.formatDate(pattern, date);", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Convert Date String", "Parse a date and return a newly formatted date", "var datestring = DateUtil.convertDate(inpattern, outpattern, date);", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Get Current Date", "Returns the current date/time in specified format", "var dateString = DateUtil.getCurrentDate(pattern);", CodeSnippetType.FUNCTION));
        
    
        return variablelistItems;
    }

}
