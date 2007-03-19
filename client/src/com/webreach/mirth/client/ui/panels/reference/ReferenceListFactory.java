package com.webreach.mirth.client.ui.panels.reference;

import java.util.ArrayList;

import com.webreach.mirth.client.ui.CodeSnippetType;

public class ReferenceListFactory
{
    public enum ListType
    {
        ALL, CONVERSION, LOGGING_AND_ALERTS, DATABASE, MESSAGE, XML, HL7, MAP, UTILITY
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
        }
        return null;
    }
    
    private ArrayList<ReferenceListItem> getAllItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();
        variablelistItems.addAll(getConversionItems());
        variablelistItems.addAll(getLoggingAndAlertsItems());
        variablelistItems.addAll(getDatabaseItems());
        variablelistItems.addAll(getMessageItems());
      //  variablelistItems.addAll(getXMLItems());
      //  variablelistItems.addAll(getHL7Items());
        variablelistItems.addAll(getMapItems());
        variablelistItems.addAll(getUtilityItems());

        return variablelistItems;
    }

    private ArrayList<ReferenceListItem> getConversionItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();
        
        variablelistItems.add(new ReferenceListItem("Convert HL7 to XML", "Converts an encoded HL7 string to XML", "serializerFactory.getHL7Serializer(useStrictParser).toXML(message);", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Convert XML to HL7", "Converts an XML string to HL7", "serializerFactory.getHL7Serializer(useStrictParser).fromXML(message);", CodeSnippetType.FUNCTION));

        variablelistItems.add(new ReferenceListItem("Convert X12 to XML", "Converts an encoded X12 string to XML", "serializerFactory.getX12Serializer(inferDelimiters).toXML(message);", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Convert XML to X12", "Converts an XML string to X12", "serializerFactory.getX12Serializer(inferDelimiters).fromXML(message);", CodeSnippetType.FUNCTION));

        variablelistItems.add(new ReferenceListItem("Convert EDI to XML", "Converts an encoded EDI string to XML", "serializerFactory.getEDISerializer(segmentDelim, elementDelim, subelementDelim).toXML(message);", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Convert XML to EDI", "Converts an XML string to EDI", "serializerFactory.getEDISerializer(segmentDelim, elementDelim, subelementDelim).fromXML(message);", CodeSnippetType.FUNCTION));

        return variablelistItems;
    }

    private ArrayList<ReferenceListItem> getLoggingAndAlertsItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();
        variablelistItems.add(new ReferenceListItem("Log an Info Statement", "Outputs the message to the system info log.", "logger.info('message');", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Log an Error Statement", "Outputs the message to the system error log.", "logger.error('message');", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Send an Email", "Sends an alert email using the alert SMTP properties.", "var smtpConn = SMTPConnectionFactory.createSMTPConnection();\nsmtpConn.send('to', 'cc', 'from', 'subject', 'body');", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Trigger an Alert", "Trigger a custom alert for the current channel.", "alert.sendAlert('message');", CodeSnippetType.FUNCTION));

    
        return variablelistItems;
    }

    private ArrayList<ReferenceListItem> getDatabaseItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();
        variablelistItems.add(new ReferenceListItem("Perform Database Query", "Performs a database query and returns the rowset.", "var dbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\nvar result = dbConn.executeCachedQuery('expression');\ndbConn.close();", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Perform Database Update", "Performs a database update.", "var dbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\nvar result = dbConn.executeUpdate('expression');\ndbConn.close();", CodeSnippetType.FUNCTION));
 
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
        variablelistItems.add(new ReferenceListItem("Lookup value in all maps", "Returns the value of the key if it exists in any map.", "$('key')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Get Connector Variable Map", "The variable map that will be sent to the connector.", "connectorMap.get('')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Get Channel Variable Map", "The variable map that can be used anywhere in the channel.", "channelMap.get('')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Get Global Variable Map", "The variable map that persists values between channels.", "globalMap.get('')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Get Response Variable Map", "The variable map that stores responses.", "responseMap.get('')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Put Connector Variable Map", "The variable map that will be sent to the connector.", "connectorMap.put('','')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Put Channel Variable Map", "The variable map that can be used anywhere in the channel.", "channelMap.put('','')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Put Global Variable Map", "The variable map that persists values between channels.", "globalMap.put('','')", CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Put Response Variable Map", "The variable map that stores responses.", "responseMap.put('','')", CodeSnippetType.VARIABLE));
                
        return variablelistItems;
    }
    
    private ArrayList<ReferenceListItem> getUtilityItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();
        variablelistItems.add(new ReferenceListItem("Use Java Class", "Access any Java class in the current classpath", "var object = Packages.[fully-qualified name];", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Generate Unique ID", "Generate a Universally Unique Identifier", "var uuid = UUIDGenerator.getUUID();", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Call System Function", "Execute a command on server system. Must have proper security enabled.", "java.lang.Runtime.getRuntime().exec(\"system_command\");", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Read File As String)", "Read file contents into string", "var contents = fileUtil.read('filename');", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Read File As Bytes)", "Read file contents into byte array", "var contents = fileUtil.readBytes('filename');", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Write String to File", "Write string to file", "fileUtil.write('filename', append(true/false), stringData);", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Write Bytes to File", "Write bytes to file", "fileUtil.write('filename', append(true/false), byteData);", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("BASE-64 Encode Data", "Encode a byte array to a BASE-64 string", "fileUtil.encode(data);", CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Decode BASE-64 Data", "Decode a BASE-64 string to a byte array", "fileUtil.decode(data);", CodeSnippetType.FUNCTION));
        
        return variablelistItems;
    }
    
}
