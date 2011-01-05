/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.mule.adaptors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mule.umo.MessagingException;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.model.converters.DelimitedSerializer;
import com.mirth.connect.model.converters.IXMLSerializer;
import com.mirth.connect.model.converters.SerializerFactory;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.JavaScriptUtil;

public class DelimitedAdaptor extends Adaptor implements BatchAdaptor {
	private Logger logger = Logger.getLogger(this.getClass());

	protected void populateMessage(boolean emptyFilterAndTransformer) throws AdaptorException {

		messageObject.setRawDataProtocol(MessageObject.Protocol.DELIMITED);
		messageObject.setTransformedDataProtocol(MessageObject.Protocol.XML);
		messageObject.setEncodedDataProtocol(MessageObject.Protocol.DELIMITED);
		
		try {
			String message = serializer.toXML(source);
			messageObject.setTransformedData(message);
			populateMetadataFromXML(message);
		} catch (Exception e) {
			handleException(e);
		}

		if (emptyFilterAndTransformer) {
			messageObject.setEncodedData(source);
		}
	}

	@Override
	public IXMLSerializer<String> getSerializer(Map properties) {
		return SerializerFactory.getSerializer(MessageObject.Protocol.DELIMITED, properties);
	}

	public void processBatch(Reader src, Map properties, BatchMessageProcessor dest, UMOEndpoint endpoint)
		throws MessagingException, UMOException, IOException
	{
	    String channelId = null;
		try {
			String batchScriptId = (String) properties.get("batchScriptId");
			// TODO: This should be "batch" or something
			channelId = batchScriptId;
			StringBuilder batchScript = new StringBuilder();
			batchScript.append("function doBatchScript() {\n");
			batchScript.append(ControllerFactory.getFactory().createScriptController().getScript(channelId, batchScriptId));
			batchScript.append("\n}\n");
			batchScript.append("return doBatchScript();\n");
			JavaScriptUtil.getInstance().compileAndAddScript(batchScriptId, batchScript.toString(), null, false, true, false);
		} catch (Exception e) {
			logger.error(e);
		}
		
		DelimitedSerializer serializer = new DelimitedSerializer(properties);
		BufferedReader in = new BufferedReader(src);
		String message;
		boolean skipHeader = true;
		while ((message = serializer.getMessage(in, skipHeader, channelId)) != null) {
			dest.processBatchMessage(message);
			skipHeader = false;
		}
	}
}
