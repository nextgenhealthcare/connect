/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.connectors.doc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.VariableFilenameParser;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.util.Utility;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.lowagie.text.html.HtmlParser;
import com.lowagie.text.pdf.PdfEncryptor;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.rtf.RtfWriter2;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.ControllerFactory;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;

public class DocumentMessageDispatcher extends AbstractMessageDispatcher {
	private DocumentConnector connector;
	private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
	private AlertController alertController = ControllerFactory.getFactory().createAlertController();
	private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
	private ConnectorType connectorType = ConnectorType.WRITER;

	public DocumentMessageDispatcher(DocumentConnector connector) {
		super(connector);
		this.connector = connector;
		monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
	}

	public void doDispatch(UMOEvent event) throws Exception {
		monitoringController.updateStatus(connector, connectorType, Event.BUSY);
		TemplateValueReplacer replacer = new TemplateValueReplacer();
		String endpoint = event.getEndpoint().getEndpointURI().getAddress();
		MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);

		if (messageObject == null) {
			return;
		}

		try {
			String filename = (String) event.getProperty(DocumentConnector.PROPERTY_FILENAME);

			if (filename == null) {
				String pattern = (String) event.getProperty(DocumentConnector.PROPERTY_OUTPUT_PATTERN);

				if (pattern == null) {
					pattern = connector.getOutputPattern();
				}

				filename = generateFilename(event, pattern, messageObject);
			}

			if (filename == null) {
				throw new IOException("Filename is null");
			}

			String template = replacer.replaceValues(connector.getTemplate(), messageObject);
			File file = Utility.createFile(generateFilename(event, endpoint, messageObject) + "/" + filename);
			logger.info("Writing document to: " + file.getAbsolutePath());
			writeDocument(template, file, messageObject);

			// update the message status to sent
			messageObjectController.setSuccess(messageObject, "Document successfully written: " + filename, null);
		} catch (Exception e) {
			alertController.sendAlerts(((DocumentConnector) connector).getChannelId(), Constants.ERROR_401, "Error writing document", e);
			messageObjectController.setError(messageObject, Constants.ERROR_401, "Error writing document", e, null);
			connector.handleException(e);
		} finally {
			monitoringController.updateStatus(connector, connectorType, Event.DONE);
		}
	}

	private void writeDocument(String template, File file, MessageObject messageObject) throws Exception {
		// add tags to the template to create a valid HTML document
		StringBuilder contents = new StringBuilder();
		if (template.lastIndexOf("<html") < 0) {
			contents.append("<html>");
			if (template.lastIndexOf("<body") < 0) {
				contents.append("<body>");
				contents.append(template);
				contents.append("</body>");
			} else {
				contents.append(template);
			}
			contents.append("</html>");
		} else {
			contents.append(template);
		}

		if (connector.getDocumentType().toLowerCase().equals("pdf")) {
			FileOutputStream renderFos = null;

			try {
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				org.w3c.dom.Document document = builder.parse(new StringBufferInputStream(contents.toString()));

				ITextRenderer renderer = new ITextRenderer();
				renderer.setDocument(document, null);
				renderFos = new FileOutputStream(file);
				renderer.layout();
				renderer.createPDF(renderFos, true);
			} catch (Throwable e) {
			    throw new Exception(e);
			} finally {
				if (renderFos != null) {
					renderFos.close();
				}
			}

			if (connector.isEncrypt() && (connector.getPassword() != null)) {
				FileInputStream encryptFis = null;
				FileOutputStream encryptFos = null;

				try {
					encryptFis = new FileInputStream(file);
					PdfReader reader = new PdfReader(encryptFis);
					encryptFos = new FileOutputStream(file);
					PdfEncryptor.encrypt(reader, encryptFos, true, connector.getPassword(), null, PdfWriter.AllowPrinting | PdfWriter.AllowCopy);
				} catch (Exception e) {
					throw e;
				} finally {
					if (encryptFis != null) {
						encryptFis.close();
					}

					if (encryptFos != null) {
						encryptFos.close();
					}
				}
			}
		} else if (connector.getDocumentType().toLowerCase().equals("rtf")) {
			com.lowagie.text.Document document = null;

			try {
				document = new com.lowagie.text.Document();
				ByteArrayInputStream bais = new ByteArrayInputStream(contents.toString().getBytes());
				RtfWriter2.getInstance(document, new FileOutputStream(file));
				document.open();
				HtmlParser parser = new HtmlParser();
				parser.go(document, bais);
			} finally {
				if (document != null) {
					document.close();
				}
			}
		}
	}

	public UMOMessage doSend(UMOEvent event) throws Exception {
		doDispatch(event);
		return event.getMessage();
	}

	public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
		return null;
	}

	public Object getDelegateSession() throws UMOException {
		return null;
	}

	private String generateFilename(UMOEvent event, String pattern, MessageObject messageObject) {
		if (connector.getFilenameParser() instanceof VariableFilenameParser) {
			VariableFilenameParser filenameParser = (VariableFilenameParser) connector.getFilenameParser();
			filenameParser.setMessageObject(messageObject);
			return filenameParser.getFilename(event.getMessage(), pattern);
		} else {
			return connector.getFilenameParser().getFilename(event.getMessage(), pattern);
		}
	}

	public void doDispose() {

	}
}
