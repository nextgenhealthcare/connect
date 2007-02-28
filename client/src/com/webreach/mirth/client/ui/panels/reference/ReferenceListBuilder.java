package com.webreach.mirth.client.ui.panels.reference;

import java.util.ArrayList;

import com.webreach.mirth.client.ui.CodeSnippetType;

public class ReferenceListBuilder
{
    public final String FUNCTIONS_AND_VARIABLES = "functionsAndVariables"; 
    
    public ArrayList<ReferenceListItem> getVariableListItems(String itemName)
    {
        if(itemName.equals(FUNCTIONS_AND_VARIABLES))
            return getFunctionsAndVariablesItems();
        else
            return null;
    }
    
    private ArrayList<ReferenceListItem> getFunctionsAndVariablesItems()
    {
        ArrayList<ReferenceListItem> variablelistItems = new ArrayList<ReferenceListItem>();
        variablelistItems.add(new ReferenceListItem("Local Variable Map",
                "The local variable map that will be sent to the connector.",
                "localMap.get('')", CodeSnippetType.VARIABLE));
        variablelistItems
                .add(new ReferenceListItem(
                "Global Variable Map",
                "The global variable map that persists values between channels.",
                "globalMap.get('')",CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Incoming Message",
                "The original incoming ER7 or XML string as received.",
                "messageObject.getRawData()",CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Message Type",
                "The HL7 or EDI message type (ex. ADT-A01)",
                "messageObject.getType()",CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Message Source",
                "The HL7 or EDI message source (sending facility)",
                "messageObject.getSource()",CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Message Version",
                "The HL7 or EDI message version",
                "messageObject.getVersion()",CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Message ID",
                "The unique id of the message in Mirth",
                "messageObject.getId()",CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Incoming Message (XML)",
                "The original incoming ER7 or XML string as XML.", "msg['']",CodeSnippetType.VARIABLE));
        variablelistItems.add(new ReferenceListItem("Log an Info Statement",
                "Outputs the message to the system info log.",
                "logger.info('message');",CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Log an Error Statement",
                "Outputs the message to the system error log.",
                "logger.error('message');",CodeSnippetType.FUNCTION));
        variablelistItems
                .add(new ReferenceListItem(
                "Send an Email",
                "Sends an alert email using the alert SMTP properties.",
                "var smtpConn = SMTPConnectionFactory.createSMTPConnection();\nsmtpConn.send('to', 'cc', 'from', 'subject', 'body');",CodeSnippetType.FUNCTION));
        variablelistItems
                .add(new ReferenceListItem(
                "Perform Database Query",
                "Performs a database query and returns the rowset.",
                "var dbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\nvar result = dbConn.executeCachedQuery('expression');\ndbConn.close();",CodeSnippetType.FUNCTION));
        variablelistItems
                .add(new ReferenceListItem(
                "Perform Database Update",
                "Performs a database update.",
                "var dbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\nvar result = dbConn.executeUpdate('expression');\ndbConn.close();",CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Convert to HL7-XML",
                "Converts an ER7 encoded HL7 string to XML",
                "serializer.toXML(message);",CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Convert to HL7-ER7",
                "Converts an XML encoded HL7 string to ER7",
                "serializer.fromXML(message);",CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Use JAVA Class",
                "Access any Java class in the current classpath",
                "var object = Packages.[fully-qualified name];",CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Generate Unique ID",
                "Generate a Universally Unique Identifier",
                "var uuid = UUIDGenerator.getUUID();",CodeSnippetType.FUNCTION));
        variablelistItems.add(new ReferenceListItem("Call System Function",
                "Execute a command on server system. Must have proper security enabled.",
                "java.lang.Runtime.getRuntime().exec(\"system_command\");",CodeSnippetType.FUNCTION));

        return variablelistItems;
    }
}
