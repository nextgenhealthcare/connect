/*
 * $Header: /home/projects/mule/scm/mule/providers/jdbc/src/java/org/mule/providers/jdbc/JdbcMessageReceiver.java,v 1.10 2005/10/23 15:21:21 holger Exp $
 * $Revision: 1.10 $
 * $Date: 2005/10/23 15:21:21 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.mule.providers.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.mule.impl.MuleMessage;
import org.mule.providers.ConnectException;
import org.mule.providers.TransactedPollingMessageReceiver;
import org.mule.transaction.TransactionCoordination;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.UMOTransaction;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;

/**
 * @author Guillaume Nodet
 * @version $Revision: 1.10 $
 */
public class JdbcMessageReceiver extends TransactedPollingMessageReceiver
{

    private JdbcConnector connector;
    private String readStmt;
    private String ackStmt;
    private List readParams;
    private List ackParams;

    public JdbcMessageReceiver(UMOConnector connector,
                               UMOComponent component,
                               UMOEndpoint endpoint,
                               String readStmt,
                               String ackStmt) throws InitialisationException
    {
        super(connector, component, endpoint, new Long(((JdbcConnector) connector).getPollingFrequency()));

        this.receiveMessagesInTransaction = false;
        this.connector = (JdbcConnector) connector;

        this.readParams = new ArrayList();
        this.readStmt = JdbcUtils.parseStatement(readStmt, this.readParams);
        this.ackParams = new ArrayList();
        this.ackStmt = JdbcUtils.parseStatement(ackStmt, this.ackParams);
    }

    public void doConnect() throws Exception
    {
    	Connection con = null;
        try {
            con = this.connector.getConnection();
        } catch (Exception e) {
            throw new ConnectException(e, this);
        } finally {
        	JdbcUtils.close(con);
        }
    }

    public void doDisconnect() throws ConnectException
    {
        // noop
    }

    public void processMessage(Object message) throws Exception
    {        
        Connection con = null;
        UMOTransaction tx = TransactionCoordination.getInstance().getTransaction();
        Exception ackException=null;
        try {            
            try{
                if (connector.isUseAck() && this.ackStmt != null) {
                    con = this.connector.getConnection();
                    Object[] ackParams = JdbcUtils.getParams(getEndpointURI(), this.ackParams, message);
                    int nbRows = new QueryRunner().update(con, this.ackStmt, ackParams);
                    if (nbRows != 1) {
                        logger.warn("Row count for ack should be 1 and not " + nbRows);
                    }
                }
            }catch(Exception ue){
                logger.error("Error in the ACK sentence of the JDBC connection, but the message is being sent anyway"+ue);
                ackException=ue;
            }
            UMOMessageAdapter msgAdapter = this.connector.getMessageAdapter(message);
            UMOMessage umoMessage = new MuleMessage(msgAdapter);
            routeMessage(umoMessage, tx, tx != null || endpoint.isSynchronous());
            if (ackException!=null) throw ackException;
        }catch(ConnectException ce){
                throw new Exception(((ConnectException)ce).getCause());
        } finally {
            if (tx == null) {
                if (con!=null) JdbcUtils.close(con);
            }
        }
    }

    public List getMessages() throws Exception
    {
        Connection con = null;
        try {
        	try {
        		con = this.connector.getConnection();
        	} catch (SQLException e) {
        		throw new ConnectException(e, this);
        	}
            Object[] readParams = JdbcUtils.getParams(getEndpointURI(), this.readParams, null);
            Object results = new QueryRunner().query(con, this.readStmt, readParams, new MapListHandler());
            return (List) results;
        } finally {
            JdbcUtils.close(con);
        }
    }

}
