package com.webreach.mirth.client.ui;

import java.util.ArrayList;

public class FunctionListBuilder {

	public ArrayList<FunctionListItem> getVariableListItems() {
		ArrayList<FunctionListItem> variablelistItems = new ArrayList<FunctionListItem>();
		variablelistItems.add(new FunctionListItem("Local Variable Map",
				"The local variable map that will be sent to the connector.",
				"localMap.get('')", CodeSnippetType.VARIABLE));
		variablelistItems
				.add(new FunctionListItem(
						"Global Variable Map",
						"The global variable map that persists values between channels.",
						"globalMap.get('')",CodeSnippetType.VARIABLE));
		variablelistItems.add(new FunctionListItem("Incoming Message",
				"The original incoming ER7 or XML string as received.",
				"messageObject.getRawData()",CodeSnippetType.VARIABLE));
		variablelistItems.add(new FunctionListItem("Message Version",
				"The HL7 or EDI message version",
				"messageObject.getVersion()",CodeSnippetType.VARIABLE));
		variablelistItems.add(new FunctionListItem("Message ID",
				"The unique id of the message in Mirth",
				"messageObject.getId()",CodeSnippetType.VARIABLE));
		variablelistItems.add(new FunctionListItem("Incoming Message (XML)",
				"The original incoming ER7 or XML string as XML.", "msg['']",CodeSnippetType.VARIABLE));
		variablelistItems.add(new FunctionListItem("Log an Info Statement",
				"Outputs the message to the system info log.",
				"logger.info('message');",CodeSnippetType.FUNCTION));
		variablelistItems.add(new FunctionListItem("Log an Error Statement",
				"Outputs the message to the system error log.",
				"logger.error('message');",CodeSnippetType.FUNCTION));
		variablelistItems
				.add(new FunctionListItem(
						"Send an Email",
						"Sends an alert email using the alert SMTP properties.",
						"var smtpConn = SMTPConnectionFactory.createSMTPConnection();\nsmtpConn.send('to', 'cc', 'from', 'subject', 'body');",CodeSnippetType.FUNCTION));
		variablelistItems
				.add(new FunctionListItem(
						"Perform Database Query",
						"Performs a database query and returns the rowset.",
						"var dbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\nvar result = dbConn.executeCachedQuery('expression');\ndbConn.close();",CodeSnippetType.FUNCTION));
		variablelistItems
				.add(new FunctionListItem(
						"Perform Database Update",
						"Performs a database update.",
						"var dbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\nvar result = dbConn.executeUpdate('expression');\ndbConn.close();",CodeSnippetType.FUNCTION));
		variablelistItems.add(new FunctionListItem("Convert to HL7-XML",
				"Converts an ER7 encoded HL7 string to XML",
				"serializer.toXML(message);",CodeSnippetType.FUNCTION));
		variablelistItems.add(new FunctionListItem("Convert to HL7-ER7",
				"Converts an XML encoded HL7 string to ER7",
				"serializer.fromXML(message);",CodeSnippetType.FUNCTION));
		variablelistItems.add(new FunctionListItem("Use JAVA Class",
				"Access any Java class in the current classpath",
				"var object = Packages.[fully-qualified name];",CodeSnippetType.FUNCTION));
		variablelistItems.add(new FunctionListItem("Generate Unique ID",
				"Generate a Universally Unique Identifier",
				"var uuid = UUIDGenerator.getUUID();",CodeSnippetType.FUNCTION));

		
		/*Removed due to HL7 versioning issues
		variablelistItems.add(new FunctionListItem("Get Message Version",
				"Extracts the HL7 message version as a numeric value",
				"var version = parseFloat(msg['MSH']['MSH.12'].text());",CodeSnippetType.HL7HELPER));
		variablelistItems.add(new FunctionListItem("Get HL7 Message Type",
				"Extracts the HL7 message type from the incoming message and stores in a Javascript variable",
				"var messageType = msg['MSH']['MSH.9']['MSG.1'].text() + msg['MSH']['MSH.9']['MSG.2'].text();",CodeSnippetType.HL7HELPER));
		variablelistItems.add(new FunctionListItem("Get Patient Name",
				"Extracts the patient first name and last name into Javascript variables",
				"var pid = msg..PID['PID.5'];\nvar version = parseFloat(msg['MSH']['MSH.12'].text());\nif (version < 2.3){\n	var patientLastName = pid['PN.1'].text();\n	var patientFirstName = pid['PN.2'].text();	\n}\nelse{\n	var patientLastName = pid['XPN.1']['FN.1'].text();\n	var patientFirstName = pid['XPN.2'].text();\n}",CodeSnippetType.HL7HELPER));
		variablelistItems.add(new FunctionListItem("Get Sending Facility and System",
				"Extracts the sending facility and system into Javascript variables",
				"var version = parseFloat(msg['MSH']['MSH.12'].text());\nif (version < 2.3){\n	var sendingSystem = msg..MSH['MSH.3'].text();\n	var sendingFacility = msg..MSH['MSH.4'].text();\n}\nelse{\n	var sendingSystem = msg..MSH['MSH.3']['HD.1'].text();\n	var sendingFacility = msg..MSH['MSH.4']['HD.1'].text();\n}\n",CodeSnippetType.HL7HELPER));

		*/
		
		
		return variablelistItems;
	}
}
