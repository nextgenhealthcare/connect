/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.event.ErrorEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.donkey.server.event.ErrorEvent;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.server.MirthJavascriptTransformerException;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.CompiledScriptCache;
import com.mirth.connect.server.util.javascript.JavaScriptScopeUtil;
import com.mirth.connect.server.util.javascript.JavaScriptTask;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.mirth.connect.userutil.ImmutableConnectorMessage;
import com.mirth.connect.util.ErrorMessageBuilder;

public class UdpDispatcher extends DestinationConnector {
    private Logger logger = Logger.getLogger(this.getClass());
    private EventController eventController = ControllerFactory.getFactory().createEventController();    
    private UdpDispatcherProperties connectorProperties;
 
    private DatagramSocket socket=null;
    private InetAddress  address;
 
    private byte[] buf;

    @Override
    public void onDeploy() throws ConnectorTaskException {
        this.connectorProperties = (UdpDispatcherProperties) getConnectorProperties();       
        eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
    }

    @Override
    public void onUndeploy() throws ConnectorTaskException {
    	try {
			if(socket!=null) {
				socket.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			socket=null;
		}
    }

    @Override
    public void onStart() throws ConnectorTaskException {
    	
        try {
			socket = new DatagramSocket();
			address = InetAddress.getByName(connectorProperties.getAddress());		
		} catch (Exception e) {
			e.printStackTrace();
		}
        
    }

    @Override
    public void onStop() throws ConnectorTaskException {
    	try {
			if(socket!=null) {
				socket.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			socket=null;
		}
    }

    @Override
    public void onHalt() throws ConnectorTaskException {
    	socket.close();    	
    }

    @Override
    public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage message) {}

    @Override
    public Response send(ConnectorProperties connectorProperties, ConnectorMessage msg) throws InterruptedException {
       UdpDispatcherProperties udpDispPopsParam = (UdpDispatcherProperties) this.connectorProperties;
        try {         
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.SENDING));
            if(this.connectorProperties.isMessageByteArray())
            {
            	buf=(byte[])msg.getChannelMap().get("message");
            }	
            else {
            	buf = msg.getEncoded().getContent().getBytes();
            }
            DatagramPacket packet 
              = new DatagramPacket(buf, buf.length, address, this.connectorProperties.getPort());
            socket.send(packet);
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String received = new String(
              packet.getData(), 0, packet.getLength());           
            
            //TODO:Execute UDP package send
            Response response = new Response(received);
            if(this.connectorProperties.isMessageByteArray()) {
            	msg.getChannelMap().put("response",packet.getData());
            }
            response.setMessage("UDP Message sent");
            response.setStatus(Status.SENT);
//            response.setValidate(udpDispPopsParam.getDestinationConnectorProperties().isValidateResponse());

            return response;
        } catch (Exception e) {
            logger.error("Error sending message (" + connectorProperties.getName() + " \"" + getDestinationName() + "\" on channel " + getChannelId() + ").", e);
            eventController.dispatchEvent(new ErrorEvent(getChannelId(), getMetaDataId(), msg.getMessageId(), ErrorEventType.DESTINATION_CONNECTOR, getDestinationName(), connectorProperties.getName(), "Error executing script", e));
            return new Response(Status.ERROR, null, ErrorMessageBuilder.buildErrorResponse("Error executing script", e), ErrorMessageBuilder.buildErrorMessage(connectorProperties.getName(), "Error executing script", e));
        } finally {
            eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(), ConnectionStatusEventType.IDLE));
        }
    }

 
}
