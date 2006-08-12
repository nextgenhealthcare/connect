package com.webreach.mirth.client.ui;

import java.util.ArrayList;

public class FunctionListBuilder {

	public ArrayList<FunctionListItem> getVariableListItems() {
		ArrayList<FunctionListItem> variablelistItems = new ArrayList<FunctionListItem>();
		variablelistItems.add(new FunctionListItem("Local Variable Map", "The local variable map that will be sent to the connector.", "localMap.get('')"));
		variablelistItems.add(new FunctionListItem("Global Variable Map", "The global variable map that persists values between channels.", "globalMap.get('')"));
		variablelistItems.add(new FunctionListItem("Incoming Message", "The original incoming ER7 or XML string as received.", "incomingMessage"));
		variablelistItems.add(new FunctionListItem("Incoming Message (XML)", "The original incoming ER7 or XML string as XML.", "msg['']"));
		variablelistItems.add(new FunctionListItem("Log an Info Statement", "Outputs the message to the system info log.", "logger.info('message');"));
		variablelistItems.add(new FunctionListItem("Log an Error Statement", "Outputs the message to the system error log.", "logger.error('message');"));
		variablelistItems.add(new FunctionListItem("Send an Email", "Sends an alert email using the alert SMTP properties.", "var smtpConn = SMTPConnectionFactory.createSMTPConnection();\nsmtpConn.send('to', 'cc', 'from', 'subject', 'body');"));
		variablelistItems.add(new FunctionListItem("Perform Database Query", "Performs a database query and returns the rowset.", "var dbConn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\nvar result = dbConn.executeCachedQuery('expression');\ndbConn.close();"));
		variablelistItems.add(new FunctionListItem("Perform Database Update", "Performs a database update.", "var conn = DatabaseConnectionFactory.createDatabaseConnection('driver', 'address', 'username', 'password');\nvar result = conn.executeUpdate('expression');\nconn.close();"));
		return variablelistItems;
	}
}
