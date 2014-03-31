/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import java.io.BufferedReader;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.HaltException;
import com.mirth.connect.donkey.server.StartException;
import com.mirth.connect.donkey.server.StopException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.PollConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.model.converters.DocumentSerializer;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;

public class DatabaseReceiver extends PollConnector {

    private DatabaseReceiverProperties connectorProperties;
    private DatabaseReceiverDelegate delegate;
    private EventController eventController = ControllerFactory.getFactory().createEventController();
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void onDeploy() throws DeployException {
        connectorProperties = (DatabaseReceiverProperties) getConnectorProperties();

        /*
         * A delegate object is used to handle the polling operations, since the polling logic is
         * very different depending on whether JavaScript is enabled or not
         */
        if (connectorProperties.isUseScript()) {
            delegate = new DatabaseReceiverScript(this);
        } else {
            delegate = new DatabaseReceiverQuery(this);
        }

        delegate.deploy();

        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onUndeploy() throws UndeployException {
        delegate.undeploy();
    }

    @Override
    public void onStart() throws StartException {
        delegate.start();
    }

    @Override
    public void onStop() throws StopException {
        delegate.stop();
    }

    @Override
    public void onHalt() throws HaltException {
        try {
            onStop();
        } catch (StopException e) {
            throw new HaltException(e);
        }
    }

    @Override
    public void handleRecoveredResponse(DispatchResult dispatchResult) {
        finishDispatch(dispatchResult);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void poll() throws InterruptedException {
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.POLLING));
        Object result = null;

        try {
            result = delegate.poll();

            if (isTerminated()) {
                return;
            }

            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.READING));

            // the result object will be a ResultSet or if JavaScript is used, we also allow the user to return a List<Map<String, Object>>
            if (result instanceof ResultSet) {
                processResultSet((ResultSet) result);
            } else if (result instanceof List) {
                // if the result object is a List, then assume it is a list of maps representing a row to process
                processResultList((List<Map<String, Object>>) result);
            } else {
                throw new DatabaseReceiverException("Unrecognized result: " + result.toString());
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to poll for messages from the database in channel \"" + ChannelController.getInstance().getDeployedChannelById(getChannelId()).getName() + "\"", e);
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), null, e.getCause()));
            return;
        } finally {
            if (result instanceof ResultSet) {
                DbUtils.closeQuietly((ResultSet) result);
            }

            try {
                delegate.afterPoll();
            } catch (DatabaseReceiverException e) {
                logger.error("Error in channel \"" + ChannelController.getInstance().getDeployedChannelById(getChannelId()).getName() + "\": " + e.getMessage(), ExceptionUtils.getRootCause(e));
                eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), null, e.getCause()));
            }

            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getSourceName(), ConnectionStatusEventType.IDLE));
        }
    }

    /**
     * For each record in the given ResultSet, convert it to XML and dispatch it as a raw message to
     * the channel. Then run the post-process if applicable.
     */
    @SuppressWarnings("unchecked")
    private void processResultSet(ResultSet resultSet) throws SQLException, InterruptedException, DatabaseReceiverException {
        BasicRowProcessor basicRowProcessor = new BasicRowProcessor();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        // loop through the ResultSet rows and convert them into hash maps for processing
        while (resultSet.next()) {
            if (isTerminated()) {
                return;
            }

            processRecord((Map<String, Object>) basicRowProcessor.toMap(resultSet));
        }
    }

    /**
     * For each record in the given list, convert it to XML and dispatch it as a raw message to the
     * channel. Then run the post-process if applicable.
     */
    private void processResultList(List<Map<String, Object>> resultList) throws InterruptedException, DatabaseReceiverException {
        for (Object object : resultList) {
            if (isTerminated()) {
                return;
            }

            if (object instanceof Map) {
                Map<String, Object> caseInsensitiveMap = new BasicRowProcessor.CaseInsensitiveHashMap();
                caseInsensitiveMap.putAll((Map<String, Object>) object);
                processRecord(caseInsensitiveMap);
            } else {
                String errorMessage = "Received invalid list entry in channel \"" + ChannelController.getInstance().getDeployedChannelById(getChannelId()).getName() + "\", expected Map<String, Object>: " + object.toString();
                logger.error(errorMessage);
                eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), errorMessage, null));
            }
        }
    }

    /**
     * Convert the given resultMap into XML and dispatch it as a raw message to the channel. Then
     * run the post-process if applicable.
     */
    private void processRecord(Map<String, Object> resultMap) throws InterruptedException, DatabaseReceiverException {
        DispatchResult dispatchResult = null;

        try {
            dispatchResult = dispatchRawMessage(new RawMessage(resultMapToXml(resultMap)));
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            String errorMessage = "Failed to process row retrieved from the database in channel \"" + ChannelController.getInstance().getDeployedChannelById(getChannelId()).getName() + "\"";
            logger.error(errorMessage, e);
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), ErrorEventType.SOURCE_CONNECTOR, getSourceName(), connectorProperties.getName(), errorMessage, e));
        } finally {
            finishDispatch(dispatchResult);
        }

        // if the message was persisted (dispatchResult != null), then run the on-update SQL
        if (dispatchResult != null) {
            if (dispatchResult.getProcessedMessage() != null) {
                delegate.runPostProcess(resultMap, dispatchResult.getProcessedMessage().getMergedConnectorMessage());
            } else {
                delegate.runPostProcess(resultMap, null);
            }
        }
    }

    /**
     * Convert a resultMap representing a message into XML.
     */
    private String resultMapToXml(Map<String, Object> resultMap) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = document.createElement("result");
        document.appendChild(root);

        for (Entry<String, Object> entry : resultMap.entrySet()) {
            String value = objectToString(entry.getValue());

            if (value != null) {
                Element child = document.createElement(entry.getKey());
                child.appendChild(document.createTextNode(value));
                root.appendChild(child);
            }
        }

        return new DocumentSerializer().toXML(document);
    }

    /**
     * Convert an object into a string for insertion in the XML
     */
    private String objectToString(Object object) throws Exception {
        if (object == null) {
            return null;
        }

        if (object instanceof byte[]) {
            return new String((byte[]) object);
        }

        if (object instanceof Clob) {
            return clobToString((Clob) object);
        }

        if (object instanceof Blob) {
            Blob blob = (Blob) object;
            return new String(blob.getBytes(1, (int) blob.length()));
        }

        return object.toString();
    }

    private String clobToString(Clob clob) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        Reader reader = clob.getCharacterStream();
        BufferedReader bufferedReader = new BufferedReader(reader);
        int c;

        try {
            while ((c = bufferedReader.read()) != -1) {
                stringBuilder.append((char) c);
            }

            return stringBuilder.toString();
        } finally {
            IOUtils.closeQuietly(bufferedReader);
            IOUtils.closeQuietly(reader);
        }
    }
}
