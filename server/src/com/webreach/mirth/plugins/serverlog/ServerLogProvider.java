package com.webreach.mirth.plugins.serverlog;

import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.webreach.mirth.model.converters.ObjectCloner;
import com.webreach.mirth.model.converters.ObjectClonerException;
import com.webreach.mirth.plugins.ServerPlugin;

/**
 * Created by IntelliJ IDEA.
 * User: chrisr
 * Date: Oct 26, 2007
 * Time: 12:41:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerLogProvider implements ServerPlugin
{
    private Logger logger = Logger.getLogger(this.getClass());
    private static final String GET_SERVER_LOGS = "getMirthServerLogs";
    private static final String REMOVE_SESSIONID = "removeSessionId";
    private static LinkedList<String[]> serverLogs = new LinkedList<String[]>();
    private static int LOG_SIZE = 100;   // maximum number of log entries.  not the number of lines.
    private static long logId = 1;
    private ConcurrentHashMap<String, Long> lastDisplayedServerLogIdBySessionId;

    private void initialize() {
    	// add the new appender
    	Appender arrayAppender = new ArrayAppender();
    	Layout patternLayout = new PatternLayout("[%d]  %-5p (%c:%L): %m%n");
    	arrayAppender.setLayout(patternLayout);
    	patternLayout.activateOptions();
    	Logger.getRootLogger().addAppender(arrayAppender);

    	this.lastDisplayedServerLogIdBySessionId = new ConcurrentHashMap<String, Long>();
    }

    public void init(Properties properties) {
        initialize();
    }

    public void newServerLogReceived(String logMessage) {        
        synchronized(this) {
            if (serverLogs.size() == LOG_SIZE) {
                serverLogs.removeLast();
            }
            serverLogs.addFirst(new String[] { String.valueOf(logId), logMessage });
            logId++;
        }
    }

    public synchronized Object invoke(String method, Object object, String sessionId) {
        if (method.equals(GET_SERVER_LOGS)) {
            if (lastDisplayedServerLogIdBySessionId.containsKey(sessionId)) {
                // client exist with the sessionId.
                // -> only display new log entries.
                long lastDisplayedServerLogId = lastDisplayedServerLogIdBySessionId.get(sessionId);

                LinkedList<String[]> newServerLogEntries = new LinkedList<String[]>();
                // FYI, channelLog.size() will never be larger than LOG_SIZE = 100.
                for (String[] aServerLog : serverLogs) {
                    if (lastDisplayedServerLogId < Long.parseLong(aServerLog[0])) {
                        newServerLogEntries.addLast(aServerLog);
                    }
                }

                if (newServerLogEntries.size() > 0) {
                    // put the lastDisplayedLogId into the HashMap. index 0 is the most recent entry, and index0 of that entry contains the logId.
                    lastDisplayedServerLogIdBySessionId.put(sessionId, Long.parseLong(newServerLogEntries.get(0)[0]));
                }

                try {
                    return ObjectCloner.deepCopy(newServerLogEntries);
                } catch (ObjectClonerException oce) {
                    logger.error("Error: ServerLogProvider.java", oce);
                }
            } else {
                // brand new client. i.e. brand new session id, and all log entries are new.
                // -> display all log entries.
                if (serverLogs.size() > 0) {
                    lastDisplayedServerLogIdBySessionId.put(sessionId, Long.parseLong(serverLogs.get(0)[0]));
                } else {
                    // no log exist at all. put the currentLogId-1, which is the very latest logId.
                    lastDisplayedServerLogIdBySessionId.put(sessionId, logId-1);
                }
                try {
                    return ObjectCloner.deepCopy(serverLogs);
                } catch (ObjectClonerException oce) {
                    logger.error("Error: ServerLogProvider.java", oce);
                }
            }
        } else if (method.equals(REMOVE_SESSIONID)) {
            // client shut down, or user logged out -> remove everything involving this sessionId.
            if (lastDisplayedServerLogIdBySessionId.containsKey(sessionId)) {
                // client exist with the sessionId. remove it.
                lastDisplayedServerLogIdBySessionId.remove(sessionId);
                return true;    // sessionId found and successfully removed.
            }
            return false;   // no sessionId found.
        }
        return null;
	}

    public void start() {

    }

    public void update(Properties properties) {

    }

    public void onDeploy() {

    }

    public void stop() {

    }

    public Properties getDefaultProperties() {
        return null;
    }
}
