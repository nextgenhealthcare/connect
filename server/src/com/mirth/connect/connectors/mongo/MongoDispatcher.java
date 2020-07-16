/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.mongo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.bson.Document;
import org.bson.codecs.BigDecimalCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.jsr310.LocalDateCodec;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.json.JsonWriterSettings;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.event.ConnectionStatusEventType;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.DestinationConnector;
import com.mirth.connect.donkey.server.event.ConnectionStatusEvent;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EventController;
import com.mirth.connect.server.util.TemplateValueReplacer;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.ListCollectionsIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.event.ServerHeartbeatFailedEvent;
import com.mongodb.event.ServerHeartbeatStartedEvent;
import com.mongodb.event.ServerHeartbeatSucceededEvent;
import com.mongodb.event.ServerMonitorListener;

public class MongoDispatcher extends DestinationConnector implements ServerMonitorListener {

	private Logger logger = Logger.getLogger(this.getClass());
	private MongoDispatcherProperties connectorProperties;
	private EventController eventController = ControllerFactory.getFactory().createEventController();
	private TemplateValueReplacer replacer = new TemplateValueReplacer();
	MongoDatabase DB;
	MongoClient mongo = null;
	JsonWriterSettings jsonSettings;
	private int dbPort = 27017;
	private String dbHost = "localhost";

	@Override
	public void onDeploy() throws ConnectorTaskException {
		this.connectorProperties = (MongoDispatcherProperties) getConnectorProperties();

		eventController.dispatchEvent(new ConnectionStatusEvent(getChannelId(), getMetaDataId(), getDestinationName(),
				ConnectionStatusEventType.IDLE));
	}

	@Override
	public void onUndeploy() throws ConnectorTaskException {
	}

	@Override
	public void onStart() throws ConnectorTaskException {
		this.dbInit();
	}

	@Override
	public void onStop() throws ConnectorTaskException {
		this.mongo.close();		
	}

	@Override
	public void onHalt() throws ConnectorTaskException {
	}

	@Override
	public void replaceConnectorProperties(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
		MongoDispatcherProperties props = (MongoDispatcherProperties) connectorProperties;

	}

	final Response success = new Response(Status.SENT, "OK");
	Response fail = new Response(Status.ERROR, "Error");

	@Override
	public Response send(ConnectorProperties connectorProperties, ConnectorMessage connectorMessage) {
		try {
			MongoDispatcherProperties mProp = (MongoDispatcherProperties) connectorProperties;
			String transformedConent = connectorMessage.getTransformed().getContent();
			MongoCollection<Document> col = DB.getCollection(this.connectorProperties.getCollectionName());
			Document pojSon = Document.parse(transformedConent);
			col.insertOne(pojSon);
			return success;
		} catch (Exception e) {
			fail.setError(e.getMessage());
			return fail;
		}
	}

	public boolean dbInit() {

		com.mongodb.MongoClientOptions.Builder opts = MongoClientOptions.builder().connectTimeout(7000)
				.addServerMonitorListener(this);

		String usr = connectorProperties.getDbUserName();
		String pass = connectorProperties.getDbPassword();
		String dbName = connectorProperties.getDbName();
		this.dbPort = connectorProperties.getDbPort();
		this.dbHost = connectorProperties.getHostAddress();
		MongoCredential credential = MongoCredential.createCredential(usr, dbName, pass.toCharArray());

		mongo = new MongoClient(new ServerAddress(dbHost, dbPort), credential, opts.build());

		CodecRegistry pojoCodecRegistry = org.bson.codecs.configuration.CodecRegistries.fromRegistries(

				CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
						CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())),
				CodecRegistries.fromCodecs(new LocalDateCodec()), CodecRegistries.fromCodecs(new BigDecimalCodec())

		);

		jsonSettings = JsonWriterSettings.builder()
				.dateTimeConverter((value, writer) -> writer.writeString(value.longValue() + ""))
				.int64Converter((value, writer) -> writer.writeNumber(value.toString())).build();

		DB = mongo.getDatabase(dbName).withCodecRegistry(pojoCodecRegistry);

		DB.getCollection("dummy"); // just to be sure there is at least one
									// collection
		ListCollectionsIterable<Document> loc = DB.listCollections();
		boolean result = loc.first() != null;
		return result;
	}

	@Override
	public void serverHearbeatStarted(ServerHeartbeatStartedEvent arg0) {
		logger.log(Level.INFO, "mongo Heart Beat started conn Id:" + arg0.getConnectionId());

	}

	@Override
	public void serverHeartbeatFailed(ServerHeartbeatFailedEvent arg0) {
		logger.log(Level.ERROR, "mongo Heart Beat failed conn Id:" + arg0.getConnectionId());

	}

	@Override
	public void serverHeartbeatSucceeded(ServerHeartbeatSucceededEvent arg0) {
		logger.log(Level.ALL, "mongo Heart Beat success conn Id:" + arg0.getConnectionId());
	}

}